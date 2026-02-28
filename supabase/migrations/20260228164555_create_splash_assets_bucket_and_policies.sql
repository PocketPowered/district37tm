begin;

insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values (
  'splash-assets',
  'splash-assets',
  true,
  5242880,
  array['image/png', 'image/jpeg', 'image/webp']
)
on conflict (id) do update
set public = excluded.public,
    file_size_limit = excluded.file_size_limit,
    allowed_mime_types = excluded.allowed_mime_types;

drop policy if exists splash_assets_public_read on storage.objects;
create policy splash_assets_public_read
on storage.objects
for select
to anon, authenticated
using (bucket_id = 'splash-assets');

drop policy if exists splash_assets_public_insert on storage.objects;
create policy splash_assets_public_insert
on storage.objects
for insert
to anon, authenticated
with check (bucket_id = 'splash-assets');

commit;
