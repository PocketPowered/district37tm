create extension if not exists pgcrypto with schema extensions;

create table if not exists public.system_runtime_settings (
  key text primary key,
  value text not null,
  updated_at timestamptz not null default timezone('utc', now())
);

alter table public.system_runtime_settings enable row level security;

insert into public.system_runtime_settings(key, value)
values ('agenda_reminder_cron_secret', encode(extensions.gen_random_bytes(32), 'hex'))
on conflict (key) do nothing;

do $$
declare
  cron_secret text;
  header_payload jsonb;
  job_command text;
  existing_job record;
begin
  if not exists(select 1 from pg_extension where extname = 'pg_cron')
    or not exists(select 1 from pg_extension where extname = 'pg_net') then
    raise exception 'pg_cron and pg_net must be enabled before scheduling agenda reminders';
  end if;

  select value
    into cron_secret
  from public.system_runtime_settings
  where key = 'agenda_reminder_cron_secret'
  limit 1;

  if cron_secret is null or length(cron_secret) = 0 then
    raise exception 'agenda_reminder_cron_secret is missing from system_runtime_settings';
  end if;

  header_payload := jsonb_build_object(
    'Content-Type', 'application/json',
    'x-agenda-reminder-secret', cron_secret
  );

  job_command := format(
    $cmd$select net.http_post(url := %L, headers := %L::jsonb, body := '{}'::jsonb);$cmd$,
    'https://yarbshxeeufpgquawcuy.supabase.co/functions/v1/send-agenda-item-reminders',
    header_payload::text
  );

  for existing_job in
    select jobid
    from cron.job
    where jobname = 'agenda-item-reminders-every-minute'
  loop
    perform cron.unschedule(existing_job.jobid);
  end loop;

  perform cron.schedule(
    'agenda-item-reminders-every-minute',
    '* * * * *',
    job_command
  );
end
$$;
