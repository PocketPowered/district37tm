# District 37 Toastmasters Conference App

A conference management platform with:
- a Kotlin Multiplatform mobile app (`composeApp` + `iosApp`)
- a React admin portal (`event-manager`)
- a Supabase backend (Postgres + Auth + Edge Functions)

## Architecture (Current)

### Mobile app (`composeApp`)
- Kotlin Multiplatform + Compose Multiplatform
- Reads conference data from Supabase (GraphQL + PostgREST)
- Stores local favorites/received notifications via SQLDelight
- Uses Firebase Cloud Messaging (FCM) for Android push delivery

### iOS host app (`iosApp`)
- Native iOS shell for the shared KMP UI
- Handles APNS/FCM registration and subscribes users to targeting topics

### Admin portal (`event-manager`)
- React + MUI + Supabase JS
- Supabase Google OAuth + `authorized_users` allowlist
- CRUD for conference dates, events, locations, and resources
- Sends broadcast notifications through Supabase Edge Function
- Supports sending to environment/version/custom topics
- Supports event-level reminder toggles with configurable lead minutes

### Supabase backend (`supabase`)
- Postgres tables for conference/domain data and notification logs
- Edge Function: `send-notification`
- Edge Function: `send-event-reminders` (automatic event reminder dispatch)
- Edge Function forwards notification payloads to FCM HTTP v1

## Push Notification Flow
1. Clients subscribe to topic `GENERAL`, plus:
   - environment topic: `APP_ENV_DEBUG` or `APP_ENV_PROD`
   - version topic: `APP_VERSION_<normalized-version>` (example: `APP_VERSION_8_0`)
2. Admin creates a notification in the portal.
3. Admin invokes Supabase Edge Function (`send-notification-v2` with fallback to `send-notification`).
4. Edge Function sends data message to the selected FCM topic.
5. Android app receives and persists/displays the notification.

## Event Reminder Flow
1. Admin enables `Send reminder before this event` and sets lead minutes.
2. Scheduled job invokes Supabase Edge Function `send-event-reminders` every minute.
3. Edge Function finds due event reminders, deduplicates per event/start-time, and sends to topic `GENERAL`.
4. Delivery and reminder-attempt records are stored in Supabase.

## Project Structure

```text
├── composeApp/      # Kotlin Multiplatform attendee app
├── iosApp/          # iOS host app for shared UI
├── event-manager/   # React admin portal
├── supabase/        # Supabase functions/config
└── shared/          # Shared KMP utilities/models
```

## Getting Started

### Prerequisites
- Kotlin/Gradle toolchain (for mobile)
- Android Studio (Android)
- Xcode (iOS)
- Node.js + npm (admin portal)
- Supabase project with required schema/auth setup
- Firebase project for FCM delivery credentials

### Mobile
```bash
./gradlew :composeApp:assembleDebug
```

### iOS
Open `iosApp/iosApp.xcodeproj` in Xcode and run.

### Admin Portal
```bash
cd event-manager
npm ci --legacy-peer-deps
npm start
```

## Notes
- Legacy Ktor/Azure server was decommissioned.
- Backward compatibility with legacy REST server endpoints is intentionally not maintained.
