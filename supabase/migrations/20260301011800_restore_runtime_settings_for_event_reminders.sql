create table if not exists public.system_runtime_settings (
  key text primary key,
  value text not null
);

alter table public.system_runtime_settings enable row level security;

insert into public.system_runtime_settings (key, value)
select
  'event_reminder_cron_secret',
  (regexp_match(command, $re$"x-event-reminder-secret": "([^"]+)"$re$))[1]
from cron.job
where jobname = 'event-reminders-every-minute'
  and (regexp_match(command, $re$"x-event-reminder-secret": "([^"]+)"$re$))[1] is not null
limit 1
on conflict (key) do update
set value = excluded.value;
