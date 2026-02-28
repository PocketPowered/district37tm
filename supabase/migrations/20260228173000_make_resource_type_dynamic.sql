-- Allow dynamic resource categories (buckets) instead of a fixed enum-like list.
alter table public.resources
  drop constraint if exists resources_type_check;

alter table public.resources
  add constraint resources_type_check
  check (
    resource_type = lower(trim(resource_type))
    and resource_type ~ '^[a-z0-9]+(?:_[a-z0-9]+)*$'
  );

create index if not exists resources_resource_type_idx
  on public.resources (resource_type);
