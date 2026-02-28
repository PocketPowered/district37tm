# Supabase Direct Migration Plan (Completed)

## Goal
Remove Firebase/Firestore as the application data source and have clients communicate directly with Supabase for app data, while preserving push notifications.

## Completion Status
- Status: **Completed**
- Legacy Ktor backend: **Removed**
- Legacy Azure deployment workflow: **Removed**
- Backward compatibility with legacy REST API: **Not maintained by design**

## Final Runtime Architecture
- Supabase Postgres is the source of truth for events, dates, locations, resources, authorized users, push tokens, notifications, and delivery logs.
- Clients read/write directly to Supabase (no Ktor API dependency).
- Notifications are sent via Supabase Edge Function `send-notification`.
- Edge Function calls FCM HTTP v1 for delivery.

## Notifications (Final)
1. Keep FCM for Android delivery.
2. Store device tokens in Supabase table `push_tokens`.
3. Admin app inserts a `notifications` record and invokes `send-notification`.
4. Edge Function uses Firebase service account credentials (Supabase secrets) to call FCM.
5. Delivery results are stored in `notification_deliveries`.
6. Broadcast topic standard is `GENERAL`.

## Database (Final)
- `conference_dates` (`date_key` bigint PK)
- `events`
- `locations`
- `resources` (`resource_type` = `general` | `first_timer` | `splash`)
- `authorized_users`
- `push_tokens`
- `notifications`
- `notification_deliveries`
- RPC: `search_locations(query_text text)`

## Operational Notes
- Google OAuth provider is configured in Supabase Auth for admin login.
- Supabase Edge Function secrets must include Firebase service account JSON and Firebase project ID.
- RLS should be continuously reviewed as app/admin capabilities evolve.
