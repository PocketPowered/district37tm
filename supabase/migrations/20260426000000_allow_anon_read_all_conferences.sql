-- Allow anonymous users to read all conferences (not just the active one).
-- Conference metadata (name, dates) is not sensitive, and the dev override
-- feature needs to enumerate non-active conferences from the client.
drop policy if exists conferences_select_public on public.conferences;
create policy conferences_select_public
on public.conferences
for select
using (true);
