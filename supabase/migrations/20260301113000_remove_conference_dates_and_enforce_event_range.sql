-- Fast-path conference scoping refactor:
-- 1) Move all core content under conferences.
-- 2) Remove legacy conference_dates table.
-- 3) Enforce that event date_key stays within conference range.

begin;

alter table public.events add column if not exists conference_id bigint;
alter table public.locations add column if not exists conference_id bigint;
alter table public.resources add column if not exists conference_id bigint;
alter table public.resource_categories add column if not exists conference_id bigint;

with target_conference as (
  select id
  from public.conferences
  where is_active = true
  order by updated_at desc, id desc
  limit 1
), fallback_conference as (
  select id
  from public.conferences
  order by created_at asc, id asc
  limit 1
), resolved_conference as (
  select coalesce(
    (select id from target_conference),
    (select id from fallback_conference)
  ) as id
)
update public.events e
set conference_id = rc.id
from resolved_conference rc
where e.conference_id is null;

with target_conference as (
  select id
  from public.conferences
  where is_active = true
  order by updated_at desc, id desc
  limit 1
), fallback_conference as (
  select id
  from public.conferences
  order by created_at asc, id asc
  limit 1
), resolved_conference as (
  select coalesce(
    (select id from target_conference),
    (select id from fallback_conference)
  ) as id
)
update public.locations l
set conference_id = rc.id
from resolved_conference rc
where l.conference_id is null;

with target_conference as (
  select id
  from public.conferences
  where is_active = true
  order by updated_at desc, id desc
  limit 1
), fallback_conference as (
  select id
  from public.conferences
  order by created_at asc, id asc
  limit 1
), resolved_conference as (
  select coalesce(
    (select id from target_conference),
    (select id from fallback_conference)
  ) as id
)
update public.resources r
set conference_id = rc.id
from resolved_conference rc
where r.conference_id is null;

with target_conference as (
  select id
  from public.conferences
  where is_active = true
  order by updated_at desc, id desc
  limit 1
), fallback_conference as (
  select id
  from public.conferences
  order by created_at asc, id asc
  limit 1
), resolved_conference as (
  select coalesce(
    (select id from target_conference),
    (select id from fallback_conference)
  ) as id
)
update public.resource_categories rcats
set conference_id = rc.id
from resolved_conference rc
where rcats.conference_id is null;

do $$
begin
  if exists (
    select 1
    from public.events
    where conference_id is null
  ) then
    raise exception 'Unable to backfill events.conference_id; ensure at least one conference exists';
  end if;

  if exists (
    select 1
    from public.locations
    where conference_id is null
  ) then
    raise exception 'Unable to backfill locations.conference_id; ensure at least one conference exists';
  end if;

  if exists (
    select 1
    from public.resources
    where conference_id is null
  ) then
    raise exception 'Unable to backfill resources.conference_id; ensure at least one conference exists';
  end if;

  if exists (
    select 1
    from public.resource_categories
    where conference_id is null
  ) then
    raise exception 'Unable to backfill resource_categories.conference_id; ensure at least one conference exists';
  end if;
end
$$;

alter table public.events alter column conference_id set not null;
alter table public.locations alter column conference_id set not null;
alter table public.resources alter column conference_id set not null;
alter table public.resource_categories alter column conference_id set not null;

alter table public.events
  drop constraint if exists events_conference_id_fkey,
  add constraint events_conference_id_fkey
    foreign key (conference_id)
    references public.conferences(id)
    on delete cascade;

alter table public.locations
  drop constraint if exists locations_conference_id_fkey,
  add constraint locations_conference_id_fkey
    foreign key (conference_id)
    references public.conferences(id)
    on delete cascade;

alter table public.resources
  drop constraint if exists resources_conference_id_fkey,
  add constraint resources_conference_id_fkey
    foreign key (conference_id)
    references public.conferences(id)
    on delete cascade;

alter table public.resource_categories
  drop constraint if exists resource_categories_conference_id_fkey,
  add constraint resource_categories_conference_id_fkey
    foreign key (conference_id)
    references public.conferences(id)
    on delete cascade;

alter table public.resource_categories
  drop constraint if exists resource_categories_pkey,
  add constraint resource_categories_pkey primary key (conference_id, key);

drop index if exists public.resource_categories_display_name_idx;
create index if not exists resource_categories_conference_display_name_idx
  on public.resource_categories (conference_id, display_name);

drop index if exists public.resources_resource_type_idx;
create index if not exists resources_conference_resource_type_idx
  on public.resources (conference_id, resource_type);

create index if not exists events_conference_date_time_idx
  on public.events (conference_id, date_key, start_time);

create index if not exists locations_conference_name_idx
  on public.locations (conference_id, location_name);

alter table public.events drop constraint if exists events_date_key_fkey;
drop table if exists public.conference_dates cascade;

create or replace function public.enforce_event_date_within_conference_range()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
  conference_start date;
  conference_end date;
  event_day date;
begin
  if new.conference_id is null then
    raise exception 'events.conference_id is required';
  end if;

  if new.date_key is null then
    raise exception 'events.date_key is required';
  end if;

  select c.start_date, c.end_date
    into conference_start, conference_end
  from public.conferences c
  where c.id = new.conference_id;

  if not found then
    raise exception 'Conference % does not exist', new.conference_id;
  end if;

  if conference_start is null or conference_end is null then
    return new;
  end if;

  event_day := (to_timestamp(new.date_key::double precision / 1000.0) at time zone 'UTC')::date;

  if event_day < conference_start or event_day > conference_end then
    raise exception
      'Event date % must be within conference date range % to %',
      event_day,
      conference_start,
      conference_end;
  end if;

  return new;
end;
$$;

drop trigger if exists enforce_event_date_within_conference_range on public.events;
create trigger enforce_event_date_within_conference_range
before insert or update of conference_id, date_key
on public.events
for each row
execute function public.enforce_event_date_within_conference_range();

commit;
