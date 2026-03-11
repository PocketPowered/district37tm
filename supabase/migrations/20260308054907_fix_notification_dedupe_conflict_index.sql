drop index if exists public.notifications_source_dedupe_key_unique;

create unique index if not exists notifications_source_dedupe_key_unique
  on public.notifications(source_dedupe_key);
