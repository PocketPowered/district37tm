-- Allow authenticated users to view pending access requests.
-- Approve/deny remains restricted by review_access_request() + update policy.

drop policy if exists access_requests_select_own_or_admin on public.access_requests;
drop policy if exists access_requests_select_pending_or_own_or_admin on public.access_requests;

create policy access_requests_select_pending_or_own_or_admin
on public.access_requests
for select
to authenticated
using (
  status = 'pending'
  or public.is_authorized_admin()
  or lower(trim(email)) = lower(trim(coalesce(auth.jwt() ->> 'email', '')))
);
