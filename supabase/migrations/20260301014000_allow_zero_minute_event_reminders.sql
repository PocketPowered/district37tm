alter table public.events
  drop constraint if exists events_notification_lead_minutes_check;

alter table public.events
  add constraint events_notification_lead_minutes_check
  check (notification_lead_minutes >= 0 and notification_lead_minutes <= 1440);

alter table public.event_reminder_deliveries
  drop constraint if exists event_reminder_deliveries_lead_minutes_check;

alter table public.event_reminder_deliveries
  add constraint event_reminder_deliveries_lead_minutes_check
  check (lead_minutes >= 0 and lead_minutes <= 1440);
