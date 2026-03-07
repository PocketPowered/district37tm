import { GoogleAuth } from "npm:google-auth-library@9.15.1";
import { createClient } from "jsr:@supabase/supabase-js@2";

type ProcessRequest = { limit?: number };

type QueuedNotification = {
  id: string;
  title: string | null;
  body: string | null;
  topic: string | null;
  scheduled_for: string | null;
};

type SupabaseClient = ReturnType<typeof createClient>;

const DEFAULT_BATCH_LIMIT = 50;
const MAX_BATCH_LIMIT = 200;
const CRON_SECRET_SETTING_KEY = "event_reminder_cron_secret";
const SECRET_HEADER_NAME = "x-event-reminder-secret";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type, x-event-reminder-secret",
};

const json = (status: number, payload: unknown) =>
  new Response(JSON.stringify(payload), {
    status,
    headers: {
      ...corsHeaders,
      "Content-Type": "application/json",
    },
  });

const normalizeLimit = (value: unknown): number => {
  if (typeof value === "number" && Number.isFinite(value)) {
    return Math.min(MAX_BATCH_LIMIT, Math.max(1, Math.floor(value)));
  }
  if (typeof value === "string") {
    const parsed = Number.parseInt(value, 10);
    if (Number.isFinite(parsed)) {
      return Math.min(MAX_BATCH_LIMIT, Math.max(1, Math.floor(parsed)));
    }
  }
  return DEFAULT_BATCH_LIMIT;
};

const getExpectedCronSecrets = async (adminClient: SupabaseClient): Promise<string[]> => {
  const secrets = new Set<string>();

  const envSecret = Deno.env.get("EVENT_REMINDER_CRON_SECRET")?.trim();
  if (envSecret) {
    secrets.add(envSecret);
  }

  const { data, error } = await adminClient
    .from("system_runtime_settings")
    .select("value")
    .eq("key", CRON_SECRET_SETTING_KEY)
    .maybeSingle();
  if (!error) {
    const dbSecret = typeof data?.value === "string" ? data.value.trim() : "";
    if (dbSecret) {
      secrets.add(dbSecret);
    }
  }

  return Array.from(secrets);
};

const sendFcmMessage = async (
  topic: string,
  firebaseProjectId: string,
  accessToken: string,
  title: string,
  body: string,
) =>
  await fetch(`https://fcm.googleapis.com/v1/projects/${firebaseProjectId}/messages:send`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${accessToken}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      message: {
        topic,
        notification: { title, body },
        data: { title, body },
        android: { priority: "high", notification: { sound: "default" } },
        apns: {
          headers: { "apns-priority": "10", "apns-push-type": "alert" },
          payload: { aps: { alert: { title, body }, sound: "default" } },
        },
      },
    }),
  });

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response(null, { headers: corsHeaders });
  }

  try {
    const supabaseUrl = Deno.env.get("SUPABASE_URL");
    const serviceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
    const anonKey = Deno.env.get("SUPABASE_ANON_KEY");
    const firebaseServiceAccountJson = Deno.env.get("FIREBASE_SERVICE_ACCOUNT_JSON");
    const firebaseProjectId = Deno.env.get("FIREBASE_PROJECT_ID");

    if (!supabaseUrl || !serviceRoleKey || !anonKey) {
      return json(500, { error: "Missing Supabase secrets for edge function runtime" });
    }
    if (!firebaseServiceAccountJson || !firebaseProjectId) {
      return json(500, { error: "Missing Firebase secrets (FIREBASE_SERVICE_ACCOUNT_JSON / FIREBASE_PROJECT_ID)" });
    }

    const authHeader = req.headers.get("Authorization");
    const requestSecret = req.headers.get(SECRET_HEADER_NAME)?.trim() || null;

    const adminClient = createClient(supabaseUrl, serviceRoleKey);

    const expectedCronSecrets = await getExpectedCronSecrets(adminClient);
    const isServiceRoleBearer =
      Boolean(authHeader?.startsWith("Bearer ")) && authHeader?.slice(7).trim() === serviceRoleKey;
    const hasValidCronSecret =
      Boolean(requestSecret) && expectedCronSecrets.some((secret) => secret === requestSecret);

    if (!isServiceRoleBearer && !hasValidCronSecret) {
      if (!authHeader) {
        return json(401, {
          error: "Missing Authorization header or x-event-reminder-secret",
        });
      }

      const callerClient = createClient(supabaseUrl, anonKey, {
        global: { headers: { Authorization: authHeader } },
      });
      const {
        data: { user },
        error: userError,
      } = await callerClient.auth.getUser();
      if (userError || !user?.email) return json(401, { error: "Unauthorized user" });

      const { data: allowed, error: allowedError } = await adminClient
        .from("authorized_users")
        .select("email")
        .eq("email", user.email.toLowerCase())
        .maybeSingle();
      if (allowedError || !allowed) return json(403, { error: "Caller is not an authorized admin" });
    }

    const requestBody = (await req.json().catch(() => ({}))) as ProcessRequest;
    const limit = normalizeLimit(requestBody.limit);

    const nowIso = new Date().toISOString();
    const { data: dueRows, error: dueError } = await adminClient
      .from("notifications")
      .select("id, title, body, topic, scheduled_for")
      .eq("status", "queued")
      .lte("scheduled_for", nowIso)
      .order("scheduled_for", { ascending: true })
      .limit(limit);

    if (dueError) {
      return json(500, { error: "Failed to load queued notifications", details: dueError.message });
    }

    if (!dueRows || dueRows.length === 0) {
      return json(200, { ok: true, message: "No due queued notifications", processed: 0 });
    }

    const auth = new GoogleAuth({
      credentials: JSON.parse(firebaseServiceAccountJson),
      scopes: ["https://www.googleapis.com/auth/firebase.messaging"],
    });
    const authClient = await auth.getClient();
    const accessToken = await authClient.getAccessToken();

    if (!accessToken.token) {
      return json(500, { error: "Could not obtain Firebase access token" });
    }

    let processed = 0;
    let sentCount = 0;
    let failedCount = 0;
    let skippedCount = 0;

    for (const dueRow of dueRows as QueuedNotification[]) {
      const { data: claimedRows, error: claimError } = await adminClient
        .from("notifications")
        .update({ status: "processing" })
        .eq("id", dueRow.id)
        .eq("status", "queued")
        .select("id, title, body, topic")
        .limit(1);

      if (claimError) {
        failedCount += 1;
        continue;
      }

      if (!claimedRows || claimedRows.length === 0) {
        skippedCount += 1;
        continue;
      }

      const claimed = claimedRows[0] as { id: string; title: string | null; body: string | null; topic: string | null };
      const title = (claimed.title || "").trim();
      const body = (claimed.body || "").trim();
      const topic = (claimed.topic || "GENERAL").trim() || "GENERAL";

      if (!title || !body) {
        await adminClient
          .from("notifications")
          .update({ status: "failed" })
          .eq("id", claimed.id);
        failedCount += 1;
        continue;
      }

      const fcmResponse = await sendFcmMessage(topic, firebaseProjectId, accessToken.token, title, body);
      const responseText = await fcmResponse.text();
      const parsedResponse = (() => {
        try {
          return JSON.parse(responseText);
        } catch {
          return { raw: responseText };
        }
      })();

      await adminClient.from("notification_deliveries").insert({
        notification_id: claimed.id,
        token: topic,
        success: fcmResponse.ok,
        response: parsedResponse,
      });

      await adminClient
        .from("notifications")
        .update({
          status: fcmResponse.ok ? "sent" : "failed",
          sent_at: fcmResponse.ok ? new Date().toISOString() : null,
        })
        .eq("id", claimed.id);

      processed += 1;
      if (fcmResponse.ok) sentCount += 1;
      else failedCount += 1;
    }

    return json(200, {
      ok: true,
      dueCount: dueRows.length,
      processed,
      sentCount,
      failedCount,
      skippedCount,
    });
  } catch (error) {
    const message = error instanceof Error ? error.message : "Unknown error";
    return json(500, { error: message });
  }
});
