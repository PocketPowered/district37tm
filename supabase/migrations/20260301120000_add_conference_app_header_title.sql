alter table public.conferences
  add column if not exists app_header_title text;

alter table public.conferences
  drop constraint if exists conferences_app_header_title_check;

alter table public.conferences
  add constraint conferences_app_header_title_check
  check (app_header_title is null or length(trim(app_header_title)) > 0);

update public.conferences
set app_header_title = coalesce(nullif(trim(schedule_title), ''), trim(name))
where app_header_title is null;
