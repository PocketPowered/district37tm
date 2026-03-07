begin;

insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values (
  'admin-images',
  'admin-images',
  true,
  5242880,
  array['image/png', 'image/jpeg', 'image/webp']
)
on conflict (id) do update
set public = excluded.public,
    file_size_limit = excluded.file_size_limit,
    allowed_mime_types = excluded.allowed_mime_types;

drop policy if exists admin_images_public_read on storage.objects;
create policy admin_images_public_read
on storage.objects
for select
to anon, authenticated
using (bucket_id = 'admin-images');

drop policy if exists admin_images_admin_insert on storage.objects;
create policy admin_images_admin_insert
on storage.objects
for insert
to authenticated
with check (
  bucket_id = 'admin-images'
  and public.is_authorized_admin()
);

drop policy if exists admin_images_admin_update on storage.objects;
create policy admin_images_admin_update
on storage.objects
for update
to authenticated
using (
  bucket_id = 'admin-images'
  and public.is_authorized_admin()
)
with check (
  bucket_id = 'admin-images'
  and public.is_authorized_admin()
);

drop policy if exists admin_images_admin_delete on storage.objects;
create policy admin_images_admin_delete
on storage.objects
for delete
to authenticated
using (
  bucket_id = 'admin-images'
  and public.is_authorized_admin()
);

commit;
