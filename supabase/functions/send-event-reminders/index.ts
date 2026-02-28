import { GoogleAuth } from "npm:google-auth-library@9.15.1";
import { createClient } from "jsr:@supabase/supabase-js@2";

type EventReminderRequest = { topic?: string };
type EventRow = {
  id: number;
  title: string | null;
  location_info: string | null;
  start_time: number | null;
  end_time: number | null;
  notify_before: unknown;
  notification_lead_minutes: unknown;
};
type ParsedEventReminder = { title: string; locationInfo: string; startTime: number; leadMinutes: number };

type SupabaseClient = ReturnType<typeof createClient>;

const DEFAULT_LEAD_MINUTES = 15;
const MAX_LEAD_MINUTES = 24 * 60;
const SCAN_WINDOW_PAST_MS = 90_000;
const SCAN_WINDOW_FUTURE_MS = 30_000;
const EVENT_LOOKAHEAD_MS = 36 * 60 * 60 * 1000;

const CRON_SECRET_SETTING_KEY = "event_reminder_cron_secret";
const SECRET_HEADER_NAME = "x-event-reminder-secret";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type, x-event-reminder-secret",
};

const json = (status: number, payload: unknown) =>
  new Response(JSON.stringify(payload), {
    status,
    headers: { ...corsHeaders, "Content-Type": "application/json" },
  });

const parsePositiveInt = (value: unknown, fallback: number) => {
  if (typeof value === "number" && Number.isFinite(value)) {
    const parsed = Math.floor(value);
    if (parsed > 0 && parsed <= MAX_LEAD_MINUTES) return parsed;
  }
  if (typeof value === "string") {
    const parsed = Number.parseInt(value, 10);
    if (Number.isFinite(parsed) && parsed > 0 && parsed <= MAX_LEAD_MINUTES) return parsed;
  }
  return fallback;
};

const parseEventReminder = (raw: EventRow): ParsedEventReminder | null => {
  if (!(raw.notify_before === true || raw.notify_before === "true")) return null;

  const startTime = typeof raw.start_time === "number" && Number.isFinite(raw.start_time) ? raw.start_time : NaN;
  if (!Number.isFinite(startTime) || startTime <= 0) return null;

  return {
    title: typeof raw.title === "string" && raw.title.trim() ? raw.title : "Upcoming event",
    locationInfo: typeof raw.location_info === "string" ? raw.location_info : "",
    startTime,
    leadMinutes: parsePositiveInt(raw.notification_lead_minutes, DEFAULT_LEAD_MINUTES),
  };
};

const isUniqueViolation = (error: unknown): boolean =>
  Boolean(error && typeof error === "object" && "code" in error && (error as { code?: string }).code === "23505");

const getExpectedCronSecret = async (adminClient: SupabaseClient): Promise<string | null> => {
  const envSecret = Deno.env.get("EVENT_REMINDER_CRON_SECRET")?.trim();
  if (envSecret) return envSecret;

  const { data } = await adminClient
    .from("system_runtime_settings")
    .select("value")
    .eq("key", CRON_SECRET_SETTING_KEY)
    .maybeSingle();
  const dbSecret = typeof data?.value === "string" ? data.value.trim() : "";
  if (dbSecret) return dbSecret;

  return null;
};

const sendFcmMessage = async (
  token: string,
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
        topic: token,
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
  if (req.method === "OPTIONS") return new Response(null, { headers: corsHeaders });

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
    const requestSecret = req.headers.get(SECRET_HEADER_NAME);

    const adminClient = createClient(supabaseUrl, serviceRoleKey);
    let callerEmail = "system:event-reminder";

    const expectedCronSecret = await getExpectedCronSecret(adminClient);
    const isServiceRoleBearer =
      Boolean(authHeader?.startsWith("Bearer ")) && authHeader?.slice(7).trim() === serviceRoleKey;
    const hasValidCronSecret = Boolean(expectedCronSecret) && requestSecret === expectedCronSecret;

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

      callerEmail = user.email.toLowerCase();
    }

    const requestBody = (await req.json().catch(() => ({}))) as EventReminderRequest;
    const topic = requestBody.topic?.trim() || "GENERAL";

    const nowMs = Date.now();
    const { data: events, error: eventsError } = await adminClient
      .from("events")
      .select("id, title, location_info, start_time, end_time, notify_before, notification_lead_minutes")
      .gte("end_time", nowMs - 5 * 60_000)
      .lte("start_time", nowMs + EVENT_LOOKAHEAD_MS)
      .order("start_time", { ascending: true });
    if (eventsError) return json(500, { error: "Failed to load events", details: eventsError.message });

    const dueReminders: Array<{ eventId: number; reminder: ParsedEventReminder }> = [];
    for (const event of (events || []) as EventRow[]) {
      const reminder = parseEventReminder(event);
      if (!reminder || reminder.startTime <= nowMs) continue;

      const triggerAt = reminder.startTime - reminder.leadMinutes * 60_000;
      if (triggerAt < nowMs - SCAN_WINDOW_PAST_MS || triggerAt > nowMs + SCAN_WINDOW_FUTURE_MS) continue;

      dueReminders.push({ eventId: event.id, reminder });
    }

    if (dueReminders.length === 0) {
      return json(200, { ok: true, message: "No due event reminders", scannedEvents: events?.length || 0 });
    }

    const auth = new GoogleAuth({
      credentials: JSON.parse(firebaseServiceAccountJson),
      scopes: ["https://www.googleapis.com/auth/firebase.messaging"],
    });
    const authClient = await auth.getClient();
    const accessToken = await authClient.getAccessToken();
    if (!accessToken.token) return json(500, { error: "Could not obtain Firebase access token" });

    let sentCount = 0;
    let failedCount = 0;
    let duplicateCount = 0;

    for (const due of dueReminders) {
      const { data: reminderDelivery, error: reminderInsertError } = await adminClient
        .from("event_reminder_deliveries")
        .insert({
          event_id: due.eventId,
          event_start_time: due.reminder.startTime,
          lead_minutes: due.reminder.leadMinutes,
          event_title: due.reminder.title,
          status: "queued",
        })
        .select("id")
        .single();

      if (reminderInsertError) {
        if (isUniqueViolation(reminderInsertError)) {
          duplicateCount += 1;
          continue;
        }
        failedCount += 1;
        continue;
      }

      const minuteLabel = due.reminder.leadMinutes === 1 ? "minute" : "minutes";
      const title = `${due.reminder.title} starts in ${due.reminder.leadMinutes} ${minuteLabel}`;
      const body = due.reminder.locationInfo.trim()
        ? `${due.reminder.title} is about to begin at ${due.reminder.locationInfo}.`
        : `${due.reminder.title} is about to begin.`;

      const { data: notificationRecord, error: notificationInsertError } = await adminClient
        .from("notifications")
        .insert({ title, body, topic, created_by: callerEmail, status: "queued" })
        .select("id")
        .single();

      if (notificationInsertError || !notificationRecord) {
        failedCount += 1;
        await adminClient
          .from("event_reminder_deliveries")
          .update({
            status: "failed",
            response: { error: notificationInsertError?.message || "Failed to create notification record" },
          })
          .eq("id", reminderDelivery.id);
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
        notification_id: notificationRecord.id,
        token: topic,
        success: fcmResponse.ok,
        response: parsedResponse,
      });
      await adminClient.from("notifications").update({ status: fcmResponse.ok ? "sent" : "failed" }).eq("id", notificationRecord.id);
      await adminClient
        .from("event_reminder_deliveries")
        .update({ status: fcmResponse.ok ? "sent" : "failed", notification_id: notificationRecord.id, response: parsedResponse })
        .eq("id", reminderDelivery.id);

      if (fcmResponse.ok) sentCount += 1;
      else failedCount += 1;
    }

    return json(200, {
      ok: true,
      scannedEvents: events?.length || 0,
      dueReminders: dueReminders.length,
      sentCount,
      failedCount,
      duplicateCount,
      topic,
    });
  } catch (error) {
    const message = error instanceof Error ? error.message : "Unknown error";
    return json(500, { error: message });
  }
});
