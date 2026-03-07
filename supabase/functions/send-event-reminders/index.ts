import { createClient } from "jsr:@supabase/supabase-js@2";

type EventReminderRequest = { topic?: string };

type EventRow = {
  id: number;
  title: string | null;
  location_info: string | null;
  start_time: number | null;
  end_time: number | null;
  conference_id: number | null;
  notify_before: unknown;
  notification_lead_minutes: unknown;
  notification_channel: unknown;
};

type ReminderRow = {
  id: number;
  event_id: number;
  is_enabled: boolean | null;
  lead_minutes: number | null;
  channel: string | null;
  body_template: string | null;
};

type ParsedReminder = {
  key: string;
  leadMinutes: number;
  channel: string | null;
  bodyTemplate: string | null;
};

type ParsedEvent = {
  id: number;
  title: string;
  locationInfo: string;
  startTime: number;
  conferenceId: number | null;
};

type SupabaseClient = ReturnType<typeof createClient>;

const DEFAULT_LEAD_MINUTES = 15;
const MAX_LEAD_MINUTES = 24 * 60;
const MAX_TOPIC_LENGTH = 900;
const TOPIC_PATTERN = /^[A-Za-z0-9\-_.~%]+$/;
const SCAN_WINDOW_PAST_MS = 90_000;
const SCAN_WINDOW_FUTURE_MS = 30_000;
const EVENT_LOOKAHEAD_MS = 36 * 60 * 60 * 1000;
const DEFAULT_REMINDER_TIMEZONE = Deno.env.get("EVENT_REMINDER_TIMEZONE")?.trim() || "America/New_York";

const CRON_SECRET_SETTING_KEY = "event_reminder_cron_secret";
const SECRET_HEADER_NAME = "x-event-reminder-secret";

const DEFAULT_TEMPLATE = '"{{event_name}}" will be starting soon at {{start_time}} in {{location}}.';

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type, x-event-reminder-secret",
};

const json = (status: number, payload: unknown) =>
  new Response(JSON.stringify(payload), {
    status,
    headers: { ...corsHeaders, "Content-Type": "application/json" },
  });

const parseNonNegativeInt = (value: unknown, fallback: number) => {
  if (typeof value === "number" && Number.isFinite(value)) {
    const parsed = Math.floor(value);
    if (parsed >= 0 && parsed <= MAX_LEAD_MINUTES) return parsed;
  }
  if (typeof value === "string") {
    const parsed = Number.parseInt(value, 10);
    if (Number.isFinite(parsed) && parsed >= 0 && parsed <= MAX_LEAD_MINUTES) return parsed;
  }
  return fallback;
};

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

const parseEvent = (raw: EventRow): ParsedEvent | null => {
  const startTime = typeof raw.start_time === "number" && Number.isFinite(raw.start_time) ? raw.start_time : NaN;
  if (!Number.isFinite(startTime) || startTime <= 0) return null;

  return {
    id: raw.id,
    title: typeof raw.title === "string" && raw.title.trim() ? raw.title : "Upcoming event",
    locationInfo: typeof raw.location_info === "string" ? raw.location_info.trim() : "",
    startTime,
    conferenceId: typeof raw.conference_id === "number" && Number.isFinite(raw.conference_id) ? raw.conference_id : null,
  };
};

const parseReminder = (raw: ReminderRow): ParsedReminder | null => {
  if (raw.is_enabled === false) return null;

  return {
    key: `reminder:${raw.id}`,
    leadMinutes: parseNonNegativeInt(raw.lead_minutes, DEFAULT_LEAD_MINUTES),
    channel: normalizeTopic(raw.channel),
    bodyTemplate: typeof raw.body_template === "string" && raw.body_template.trim() ? raw.body_template.trim() : null,
  };
};

const parseLegacyReminder = (raw: EventRow): ParsedReminder | null => {
  if (!(raw.notify_before === true || raw.notify_before === "true")) return null;

  return {
    key: "legacy",
    leadMinutes: parseNonNegativeInt(raw.notification_lead_minutes, DEFAULT_LEAD_MINUTES),
    channel: normalizeTopic(raw.notification_channel),
    bodyTemplate: null,
  };
};

const formatReminderStartTime = (startTimeMs: number): string => {
  try {
    return new Intl.DateTimeFormat("en-US", {
      hour: "numeric",
      minute: "2-digit",
      hour12: true,
      timeZone: DEFAULT_REMINDER_TIMEZONE,
    }).format(new Date(startTimeMs));
  } catch {
    return new Date(startTimeMs).toISOString();
  }
};

const applyTemplate = (
  template: string,
  payload: { eventName: string; startTime: string; location: string; conferenceName: string },
): string => {
  return template
    .replace(/\{\{\s*event_name\s*\}\}/gi, payload.eventName)
    .replace(/\{\{\s*start_time\s*\}\}/gi, payload.startTime)
    .replace(/\{\{\s*location\s*\}\}/gi, payload.location)
    .replace(/\{\{\s*conference_name\s*\}\}/gi, payload.conferenceName);
};

const buildReminderBody = (
  template: string | null,
  payload: { eventName: string; startTime: string; location: string; conferenceName: string },
): string => {
  if (!template) {
    const locationSuffix = payload.location ? ` in ${payload.location}` : "";
    return `"${payload.eventName}" will be starting soon at ${payload.startTime}${locationSuffix}.`;
  }

  return applyTemplate(template || DEFAULT_TEMPLATE, payload);
};

const getExpectedCronSecrets = async (adminClient: SupabaseClient): Promise<string[]> => {
  const secrets = new Set<string>();

  const envSecret = Deno.env.get("EVENT_REMINDER_CRON_SECRET")?.trim();
  if (envSecret) secrets.add(envSecret);

  const { data, error } = await adminClient
    .from("system_runtime_settings")
    .select("value")
    .eq("key", CRON_SECRET_SETTING_KEY)
    .maybeSingle();

  if (!error) {
    const dbSecret = typeof data?.value === "string" ? data.value.trim() : "";
    if (dbSecret) secrets.add(dbSecret);
  }

  return Array.from(secrets);
};

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return new Response(null, { headers: corsHeaders });

  try {
    const supabaseUrl = Deno.env.get("SUPABASE_URL");
    const serviceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
    const anonKey = Deno.env.get("SUPABASE_ANON_KEY");

    if (!supabaseUrl || !serviceRoleKey || !anonKey) {
      return json(500, { error: "Missing Supabase secrets for edge function runtime" });
    }

    const authHeader = req.headers.get("Authorization");
    const requestSecret = req.headers.get(SECRET_HEADER_NAME)?.trim() || null;

    const adminClient = createClient(supabaseUrl, serviceRoleKey);
    let callerEmail = "system:event-reminder";

    const expectedCronSecrets = await getExpectedCronSecrets(adminClient);
    const isServiceRoleBearer =
      Boolean(authHeader?.startsWith("Bearer ")) && authHeader?.slice(7).trim() === serviceRoleKey;
    const hasValidCronSecret =
      Boolean(requestSecret) && expectedCronSecrets.some((secret) => secret === requestSecret);

    if (!isServiceRoleBearer && !hasValidCronSecret) {
      if (!authHeader) {
        return json(401, { error: "Missing Authorization header or x-event-reminder-secret" });
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
    const defaultTopic = normalizeTopic(requestBody.topic) || "GENERAL";

    const nowMs = Date.now();
    const { data: events, error: eventsError } = await adminClient
      .from("events")
      .select(
        "id, title, location_info, start_time, end_time, conference_id, notify_before, notification_lead_minutes, notification_channel",
      )
      .gte("end_time", nowMs - 5 * 60_000)
      .lte("start_time", nowMs + EVENT_LOOKAHEAD_MS)
      .order("start_time", { ascending: true });

    if (eventsError) {
      return json(500, { error: "Failed to load events", details: eventsError.message });
    }

    const parsedEvents = new Map<number, ParsedEvent>();
    const eventIds: number[] = [];

    for (const event of (events || []) as EventRow[]) {
      const parsed = parseEvent(event);
      if (!parsed) continue;
      if (parsed.startTime <= nowMs) continue;
      parsedEvents.set(parsed.id, parsed);
      eventIds.push(parsed.id);
    }

    if (eventIds.length === 0) {
      return json(200, { ok: true, message: "No upcoming events to evaluate" });
    }

    const { data: reminderRows, error: remindersError } = await adminClient
      .from("event_notification_reminders")
      .select("id, event_id, is_enabled, lead_minutes, channel, body_template")
      .in("event_id", eventIds)
      .order("sort_order", { ascending: true })
      .order("id", { ascending: true });

    if (remindersError) {
      return json(500, { error: "Failed to load event reminder settings", details: remindersError.message });
    }

    const reminderMap = new Map<number, ParsedReminder[]>();

    for (const row of (reminderRows || []) as ReminderRow[]) {
      const parsed = parseReminder(row);
      if (!parsed) continue;

      const existing = reminderMap.get(row.event_id) || [];
      existing.push(parsed);
      reminderMap.set(row.event_id, existing);
    }

    for (const rawEvent of (events || []) as EventRow[]) {
      if (!parsedEvents.has(rawEvent.id)) continue;
      if (reminderMap.has(rawEvent.id)) continue;

      const legacyReminder = parseLegacyReminder(rawEvent);
      if (!legacyReminder) continue;

      reminderMap.set(rawEvent.id, [legacyReminder]);
    }

    const conferenceIds = Array.from(
      new Set(
        Array.from(parsedEvents.values())
          .map((event) => event.conferenceId)
          .filter((conferenceId): conferenceId is number => typeof conferenceId === "number" && Number.isFinite(conferenceId)),
      ),
    );

    const conferenceNameById = new Map<number, string>();
    if (conferenceIds.length > 0) {
      const { data: conferences } = await adminClient.from("conferences").select("id, name").in("id", conferenceIds);

      for (const conference of conferences || []) {
        const conferenceId = typeof conference.id === "number" ? conference.id : Number.NaN;
        const conferenceName = typeof conference.name === "string" ? conference.name.trim() : "";
        if (Number.isFinite(conferenceId) && conferenceName) {
          conferenceNameById.set(conferenceId, conferenceName);
        }
      }
    }

    let dueReminderCount = 0;
    let queuedCount = 0;
    let duplicateCount = 0;

    for (const [eventId, event] of parsedEvents.entries()) {
      const reminders = reminderMap.get(eventId) || [];
      if (!reminders.length) continue;

      for (const reminder of reminders) {
        const triggerAt = event.startTime - reminder.leadMinutes * 60_000;
        if (triggerAt < nowMs - SCAN_WINDOW_PAST_MS || triggerAt > nowMs + SCAN_WINDOW_FUTURE_MS) continue;

        dueReminderCount += 1;

        const conferenceName =
          (typeof event.conferenceId === "number" && conferenceNameById.get(event.conferenceId)) || "Upcoming Event";
        const startTimeLabel = formatReminderStartTime(event.startTime);
        const body = buildReminderBody(reminder.bodyTemplate, {
          eventName: event.title,
          startTime: startTimeLabel,
          location: event.locationInfo,
          conferenceName,
        });

        const dedupeKey = `event:${event.id}:${reminder.key}:start:${event.startTime}:lead:${reminder.leadMinutes}`;
        const topic = reminder.channel || defaultTopic;

        const { data: insertedRows, error: insertError } = await adminClient
          .from("notifications")
          .upsert(
            {
              title: conferenceName,
              body,
              topic,
              created_by: callerEmail,
              status: "queued",
              scheduled_for: new Date(Math.max(triggerAt, nowMs)).toISOString(),
              source: "event_reminder",
              source_dedupe_key: dedupeKey,
            },
            { onConflict: "source_dedupe_key", ignoreDuplicates: true },
          )
          .select("id");

        if (insertError) {
          return json(500, { error: "Failed to enqueue reminder notification", details: insertError.message });
        }

        if (insertedRows && insertedRows.length > 0) {
          queuedCount += 1;
        } else {
          duplicateCount += 1;
        }
      }
    }

    return json(200, {
      ok: true,
      scannedEvents: parsedEvents.size,
      dueReminders: dueReminderCount,
      queuedCount,
      duplicateCount,
      defaultTopic,
    });
  } catch (error) {
    const message = error instanceof Error ? error.message : "Unknown error";
    return json(500, { error: message });
  }
});
