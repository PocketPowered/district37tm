import { GoogleAuth } from "npm:google-auth-library@9.15.1";
import { createClient } from "jsr:@supabase/supabase-js@2";

type NotificationRequest = {
  notificationId?: string;
  title?: string;
  body?: string;
  topic?: string;
  scheduledFor?: string | number;
};

type NotificationRow = {
  id: string;
  title: string;
  body: string;
  topic: string;
  status: string;
  scheduled_for: string | null;
};

const MAX_TOPIC_LENGTH = 900;
const TOPIC_PATTERN = /^[A-Za-z0-9\-_.~%]+$/;

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

const json = (status: number, payload: unknown) =>
  new Response(JSON.stringify(payload), {
    status,
    headers: {
      ...corsHeaders,
      "Content-Type": "application/json",
    },
  });

const normalizeTopic = (value: unknown): string | null => {
  if (typeof value !== "string") return null;

  const trimmed = value.trim();
  if (!trimmed) return null;

  const withoutPrefix = trimmed.startsWith("/topics/") ? trimmed.slice("/topics/".length) : trimmed;
  if (!withoutPrefix) return null;
  if (withoutPrefix.length > MAX_TOPIC_LENGTH) return null;
  if (!TOPIC_PATTERN.test(withoutPrefix)) return null;

  return withoutPrefix;
};

const parseScheduledFor = (value: unknown): Date | null => {
  if (value == null) return null;

  if (typeof value === "number" && Number.isFinite(value)) {
    const parsed = new Date(value);
    return Number.isNaN(parsed.getTime()) ? null : parsed;
  }

  if (typeof value === "string" && value.trim()) {
    const parsed = new Date(value);
    return Number.isNaN(parsed.getTime()) ? null : parsed;
  }

  return null;
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
        notification: {
          title,
          body,
        },
        data: {
          title,
          body,
        },
        android: {
          priority: "high",
          notification: {
            sound: "default",
          },
        },
        apns: {
          headers: {
            "apns-priority": "10",
            "apns-push-type": "alert",
          },
          payload: {
            aps: {
              alert: {
                title,
                body,
              },
              sound: "default",
            },
          },
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
    const firebaseServiceAccountJson = Deno.env.get("FIREBASE_SERVICE_ACCOUNT_JSON");
    const firebaseProjectId = Deno.env.get("FIREBASE_PROJECT_ID");

    if (!supabaseUrl || !serviceRoleKey) {
      return json(500, { error: "Missing Supabase secrets for edge function runtime" });
    }
    if (!firebaseServiceAccountJson || !firebaseProjectId) {
      return json(500, { error: "Missing Firebase secrets (FIREBASE_SERVICE_ACCOUNT_JSON / FIREBASE_PROJECT_ID)" });
    }

    const authHeader = req.headers.get("Authorization");
    if (!authHeader) {
      return json(401, { error: "Missing Authorization header" });
    }
    const bearerToken = authHeader.startsWith("Bearer ") ? authHeader.slice(7).trim() : "";
    if (!bearerToken) {
      return json(401, { error: "Invalid Authorization header" });
    }

    const adminClient = createClient(supabaseUrl, serviceRoleKey);

    const {
      data: { user },
      error: userError,
    } = await adminClient.auth.getUser(bearerToken);
    if (userError || !user?.email) {
      return json(401, { error: "Unauthorized user" });
    }

    const { data: allowed, error: allowedError } = await adminClient
      .from("authorized_users")
      .select("email")
      .eq("email", user.email.toLowerCase())
      .maybeSingle();
    if (allowedError || !allowed) {
      return json(403, { error: "Caller is not an authorized admin" });
    }

    const requestBody = (await req.json()) as NotificationRequest;
    const scheduledFor = parseScheduledFor(requestBody.scheduledFor);
    const normalizedTopic = normalizeTopic(requestBody.topic) || "GENERAL";

    let notification: NotificationRow;

    if (requestBody.notificationId) {
      const { data: existing, error: notificationError } = await adminClient
        .from("notifications")
        .select("id, title, body, topic, status, scheduled_for")
        .eq("id", requestBody.notificationId)
        .single();

      if (notificationError || !existing) {
        return json(404, { error: "Notification not found" });
      }

      notification = existing as NotificationRow;
    } else {
      const title = requestBody.title?.trim();
      const body = requestBody.body?.trim();

      if (!title || !body) {
        return json(400, { error: "Provide notificationId or both title/body" });
      }

      const scheduledForIso = scheduledFor?.toISOString() ?? new Date().toISOString();

      const { data: inserted, error: insertError } = await adminClient
        .from("notifications")
        .insert({
          title,
          body,
          topic: normalizedTopic,
          created_by: user.email.toLowerCase(),
          status: "queued",
          scheduled_for: scheduledForIso,
          source: "manual",
        })
        .select("id, title, body, topic, status, scheduled_for")
        .single();

      if (insertError || !inserted) {
        return json(500, { error: "Failed to create notification record" });
      }

      notification = inserted as NotificationRow;
    }

    if (scheduledFor && scheduledFor.getTime() > Date.now() + 1000) {
      return json(200, {
        ok: true,
        scheduled: true,
        notificationId: notification.id,
        topic: notification.topic,
        scheduledFor: notification.scheduled_for,
      });
    }

    const credentials = JSON.parse(firebaseServiceAccountJson);
    const auth = new GoogleAuth({
      credentials,
      scopes: ["https://www.googleapis.com/auth/firebase.messaging"],
    });
    const authClient = await auth.getClient();
    const accessToken = await authClient.getAccessToken();

    if (!accessToken.token) {
      throw new Error("Could not obtain Firebase access token");
    }

    const fcmResponse = await sendFcmMessage(
      notification.topic || "GENERAL",
      firebaseProjectId,
      accessToken.token,
      notification.title || "",
      notification.body || "",
    );

    const responseText = await fcmResponse.text();
    const parsedResponse = (() => {
      try {
        return JSON.parse(responseText);
      } catch {
        return { raw: responseText };
      }
    })();

    await adminClient.from("notification_deliveries").insert({
      notification_id: notification.id,
      token: notification.topic,
      success: fcmResponse.ok,
      response: parsedResponse,
    });

    await adminClient
      .from("notifications")
      .update({
        status: fcmResponse.ok ? "sent" : "failed",
        sent_at: fcmResponse.ok ? new Date().toISOString() : null,
      })
      .eq("id", notification.id);

    if (!fcmResponse.ok) {
      return json(502, { error: "FCM request failed", details: parsedResponse });
    }

    return json(200, {
      ok: true,
      scheduled: false,
      notificationId: notification.id,
      topic: notification.topic,
      response: parsedResponse,
    });
  } catch (error) {
    const message = error instanceof Error ? error.message : "Unknown error";
    return json(500, { error: message });
  }
});
