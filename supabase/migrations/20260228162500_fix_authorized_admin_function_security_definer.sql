-- Fix recursive RLS evaluation on authorized_users policies.
-- is_authorized_admin() must run with definer privileges.
create or replace function public.is_authorized_admin()
returns boolean
language sql
stable
security definer
set search_path = public
as $function$
  select exists (
    select 1
    from public.authorized_users au
    where lower(au.email) = lower(coalesce(auth.jwt() ->> 'email', ''))
  );
$function$;

revoke all on function public.is_authorized_admin() from public;
grant execute on function public.is_authorized_admin() to anon, authenticated;
