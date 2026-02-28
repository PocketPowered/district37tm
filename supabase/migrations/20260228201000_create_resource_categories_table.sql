create table if not exists public.resource_categories (
  key text primary key,
  display_name text not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint resource_categories_key_check
    check (
      key = lower(trim(key))
      and key ~ '^[a-z0-9]+(?:_[a-z0-9]+)*$'
    ),
  constraint resource_categories_display_name_check
    check (length(trim(display_name)) > 0)
);

create index if not exists resource_categories_display_name_idx
  on public.resource_categories (display_name);

insert into public.resource_categories (key, display_name)
select distinct
  resource_type,
  initcap(replace(resource_type, '_', ' '))
from public.resources
where resource_type is not null
  and trim(resource_type) <> ''
  and resource_type <> 'splash'
on conflict (key) do nothing;

alter table public.resource_categories enable row level security;

drop policy if exists resource_categories_select_admin on public.resource_categories;
create policy resource_categories_select_admin
on public.resource_categories
for select
using (public.is_authorized_admin());

drop policy if exists resource_categories_insert_admin on public.resource_categories;
create policy resource_categories_insert_admin
on public.resource_categories
for insert
with check (public.is_authorized_admin());

drop policy if exists resource_categories_update_admin on public.resource_categories;
create policy resource_categories_update_admin
on public.resource_categories
for update
using (public.is_authorized_admin())
with check (public.is_authorized_admin());

drop policy if exists resource_categories_delete_admin on public.resource_categories;
create policy resource_categories_delete_admin
on public.resource_categories
for delete
using (public.is_authorized_admin());
