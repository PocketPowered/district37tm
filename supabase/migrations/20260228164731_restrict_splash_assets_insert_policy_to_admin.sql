begin;

drop policy if exists splash_assets_public_insert on storage.objects;
drop policy if exists splash_assets_admin_insert on storage.objects;

create policy splash_assets_admin_insert
on storage.objects
for insert
to authenticated
with check (
  bucket_id = 'splash-assets'
  and public.is_authorized_admin()
);

commit;
