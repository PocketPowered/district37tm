begin;

alter table public.resources
  drop constraint if exists resources_type_check;

alter table public.resources
  add constraint resources_type_check
  check (resource_type = any (array['general'::text, 'first_timer'::text, 'splash'::text]));

insert into public.resources (resource_type, display_name, url, description)
select
  'splash',
  'Startup Splash Override',
  null,
  'Set url to a public https image to override the default app splash screen on future launches.'
where not exists (
  select 1
  from public.resources
  where resource_type = 'splash'
);

commit;
