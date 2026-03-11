do $$
declare
  cron_secret text;
  header_payload jsonb;
  job_command text;
  existing_job record;
begin
  if not exists (select 1 from pg_extension where extname = 'pg_cron')
    or not exists (select 1 from pg_extension where extname = 'pg_net') then
    raise exception 'pg_cron and pg_net must be enabled before scheduling event reminders';
  end if;

  select value
    into cron_secret
  from public.system_runtime_settings
  where key = 'event_reminder_cron_secret'
  limit 1;

  if cron_secret is null or length(cron_secret) = 0 then
    raise exception 'event_reminder_cron_secret is missing from system_runtime_settings';
  end if;

  header_payload := jsonb_build_object(
    'Content-Type', 'application/json',
    'x-event-reminder-secret', cron_secret
  );

  job_command := format(
    $cmd$select net.http_post(url := %L, headers := %L::jsonb, body := '{}'::jsonb);$cmd$,
    'https://yarbshxeeufpgquawcuy.supabase.co/functions/v1/send-event-reminders-v2',
    header_payload::text
  );

  for existing_job in
    select jobid
    from cron.job
    where jobname = 'event-reminders-every-minute'
  loop
    perform cron.unschedule(existing_job.jobid);
  end loop;

  perform cron.schedule(
    'event-reminders-every-minute',
    '* * * * *',
    job_command
  );
end
$$;
