# Supabase Direct Migration Plan

## Goal
Remove Firebase/Firestore as the application data source and have clients communicate directly with Supabase for app data, while preserving push notifications.

## Current State
- Data and admin authz are stored in Firebase Firestore.
- Admin auth is Firebase Auth (Google).
- Mobile and admin clients fetch data through the Ktor server.
- Android push delivery uses FCM.

## Target State
- Supabase Postgres is the source of truth for events, dates, locations, resources, authorized users, and push tokens.
- Clients read/write data directly to Supabase (no Ktor API dependency for these paths).
- Notifications are sent through a Supabase Edge Function that calls FCM HTTP v1.
- Firebase Firestore is removed from runtime data flow.

## Notifications Plan
1. Keep FCM for delivery to Android/iOS devices.
2. Store device tokens in Supabase table `push_tokens`.
3. Admin app inserts a `notifications` record and invokes `send-notification` Edge Function.
4. Edge Function uses Firebase service account credentials (stored as Supabase secrets) to call FCM.
5. Delivery results are stored in `notification_deliveries`.

## Database Plan
Create/maintain these public tables:
- `conference_dates` (`date_key` bigint PK)
- `events` (event payload with time fields, agenda/additional links JSON, FK to dates)
- `locations`
- `resources` (with `resource_type` = `general` | `first_timer`)
- `authorized_users` (admin allowlist by email)
- `push_tokens`
- `notifications`
- `notification_deliveries`

Create helper RPC:
- `search_locations(query_text text)` for tokenized case-insensitive search.

## Client Migration Plan
### Admin Portal (`event-manager`)
1. Replace Firebase Auth with Supabase Auth (Google OAuth).
2. Replace Firestore allowlist checks with `authorized_users` table queries.
3. Replace REST API service calls with direct Supabase table operations.
4. Replace `/notifications` API call with Edge Function invoke.

### Mobile App (`composeApp`)
1. Repoint networking to Supabase REST (`/rest/v1`) with publishable key.
2. Replace server endpoint calls in repositories with PostgREST queries/RPC.
3. Register FCM tokens by writing to `push_tokens`.
4. Keep local SQLDelight tables for favorites/received notifications.

## Rollout Sequence
1. Add Supabase schema + RLS + helper RPC.
2. Migrate admin portal reads/writes.
3. Migrate mobile reads/writes.
4. Add Edge Function notification sender.
5. Validate parity against existing app behavior.
6. Decommission server Firebase/Firestore paths.

## Open Items
- Google OAuth provider must be configured in Supabase Auth.
- Supabase Edge Function secrets required:
  - Firebase service account JSON or generated OAuth access token inputs.
  - Firebase project ID.
- RLS policy hardening after parity verification.
