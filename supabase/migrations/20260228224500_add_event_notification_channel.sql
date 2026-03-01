alter table public.events
  add column if not exists notification_channel text;
