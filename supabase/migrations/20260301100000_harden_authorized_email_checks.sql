-- Harden admin authorization checks against whitespace/case discrepancies.
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
    where lower(trim(au.email)) = lower(trim(coalesce(auth.jwt() ->> 'email', '')))
  );
$function$;

revoke all on function public.is_authorized_admin() from public;
grant execute on function public.is_authorized_admin() to anon, authenticated;

create or replace function public.is_authorized_email(p_email text)
returns boolean
language sql
stable
security definer
set search_path = public
as $function$
  select exists (
    select 1
    from public.authorized_users au
    where lower(trim(au.email)) = lower(trim(coalesce(p_email, '')))
  );
$function$;

revoke all on function public.is_authorized_email(text) from public;
grant execute on function public.is_authorized_email(text) to authenticated;
