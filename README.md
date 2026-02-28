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
- Handles APNS/FCM registration and subscribes users to the `GENERAL` topic

### Admin portal (`event-manager`)
- React + MUI + Supabase JS
- Supabase Google OAuth + `authorized_users` allowlist
- CRUD for conference dates, events, locations, and resources
- Sends broadcast notifications through Supabase Edge Function

### Supabase backend (`supabase`)
- Postgres tables for conference/domain data and notification logs
- Edge Function: `send-notification`
- Edge Function forwards notification payloads to FCM HTTP v1

## Push Notification Flow
1. Android clients subscribe to topic `GENERAL`.
2. Admin creates a notification in the portal.
3. Admin invokes Supabase Edge Function `send-notification`.
4. Edge Function sends data message to FCM topic `GENERAL`.
5. Android app receives and persists/displays the notification.

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
