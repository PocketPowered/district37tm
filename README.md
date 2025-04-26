# District 37 Toastmasters Conference App

A comprehensive conference management system built with Kotlin Multiplatform, featuring a mobile app, admin portal, and real-time server capabilities.

## About Toastmasters International

Toastmasters International is a global nonprofit organization dedicated to empowering individuals to become more effective communicators and leaders. Founded in 1924, Toastmasters has transformed nearly eight million lives through its unique learning environment.

## Project Overview

This project is designed to enhance the experience of Toastmasters conferences by providing attendees with real-time information, schedules, and notifications, while giving administrators powerful tools to manage the event.

## Architecture

The project consists of three main components:

1. **Mobile App (Kotlin Multiplatform)**
   - Built with Compose Multiplatform
   - Available for both Android and iOS
   - Real-time updates and notifications
   - Conference agenda, maps and session information

2. **Admin Portal (Web Application)**
   - Built in React
   - Firebase authentication
   - Real-time data management allowing for an interactive app
   - Broadcast notifications to all attendees
   - Comprehensive event management tools
   - Access the admin portal at: [District 37 Admin Portal](https://pocketpowered.github.io/district37tm/)

3. **Backend Server (Ktor)**
   - Firebase Firestore integration for dynamic data
   - RESTful API endpoints
   - Secure authentication and authorization


## Get the apps

The app is in internal testing stages, follow the guide [here](https://docs.google.com/document/d/1taTai4nhkGwwaH9xMal5vetnlXpHap1l5n4b4lfemkk/edit?tab=t.0#heading=h.d5k3dqe9oty8) to gain access.

## Project Structure

```
├── composeApp/          # Kotlin Multiplatform mobile app
│   ├── commonMain/     # Shared code across platforms
│   ├── androidMain/    # Android-specific code
│   └── iosMain/        # iOS-specific code
├── iosApp/             # iOS application entry point
├── server/             # Ktor backend server
├── event-manager/             # Admin portal web application
└── shared/             # Shared code between clients and server
```

## Features

### Mobile App
- Real-time conference schedule updates
- Push notifications for important announcements
- Session information and speaker details
- Interactive maps and venue information
- Personal schedule management
- Networking features for attendees

### Admin Portal
- Real-time data management
- Broadcast notifications to all attendees
- Schedule management and updates
- User management and permissions
- Analytics and reporting
- Emergency announcements

### Backend
- Firebase Firestore integration for dynamic data
- Secure authentication
- WebSocket support for live updates
- RESTful API endpoints
- Data synchronization across all platforms

## Getting Started

### Prerequisites
- Kotlin 1.9.x
- Android Studio (for Android development)
- Xcode (for iOS development)
- Node.js (for admin portal)
- Firebase account and configuration

### Setup Instructions

1. **Mobile App Development**
   ```bash
   # Clone the repository
   git clone [repository-url]
   
   # Open in Android Studio
   # Build and run for Android
   
   # For iOS
   cd iosApp
   pod install
   # Open in Xcode and run
   ```

2. **Admin Portal**
   ```bash
   cd client
   npm install --legacy-peer-deps
   npm run dev
   ```

3. **Backend Server**
   ```bash
   cd server
   ./gradlew run
   ```

## Firebase Configuration

1. Create a Firebase project
2. Add your configuration files:
   - `google-services.json` for Android
   - `GoogleService-Info.plist` for iOS
   - Firebase Admin SDK credentials for the server

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
