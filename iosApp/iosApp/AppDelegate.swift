import UIKit
import Firebase
import UserNotifications
import ComposeApp

class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate, MessagingDelegate {
    private lazy var notificationRepository: NotificationRepository = {
        return KoinBridge.shared.getNotificationsRepository()
    }()

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        print("🚀 App starting up...")
        
        // Initialize Firebase
        FirebaseApp.configure()
        print("🔥 Firebase configured")

        // Initialize Koin
        AppModuleKt.initializeKoin(context: application)
        print("🔄 Koin initialized")

        // Setup push notifications
        UNUserNotificationCenter.current().delegate = self
        Messaging.messaging().delegate = self
        print("🔔 Notification delegates set")

        // Check current authorization status
        UNUserNotificationCenter.current().getNotificationSettings { settings in
            print("📱 Current notification settings: \(settings.authorizationStatus.rawValue)")
            
            switch settings.authorizationStatus {
            case .authorized:
                print("✅ Already authorized for notifications")
                DispatchQueue.main.async {
                    application.registerForRemoteNotifications()
                }
            case .denied:
                print("❌ Notifications denied by user")
            case .notDetermined:
                print("❓ Notification permission not determined")
                UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { granted, error in
                    if let error = error {
                        print("❌ Push permission error: \(error.localizedDescription)")
                    } else {
                        print("📲 Push permission granted: \(granted)")
                        if granted {
                            DispatchQueue.main.async {
                                application.registerForRemoteNotifications()
                            }
                        }
                    }
                }
            case .provisional:
                print("⚠️ Provisional authorization granted")
                DispatchQueue.main.async {
                    application.registerForRemoteNotifications()
                }
            case .ephemeral:
                print("ℹ️ Ephemeral authorization granted")
                DispatchQueue.main.async {
                    application.registerForRemoteNotifications()
                }
            @unknown default:
                print("❓ Unknown authorization status")
            }
        }

        return true
    }

    func application(
        _ application: UIApplication,
        didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
    ) {
        print("📱 Received APNS token")
        Messaging.messaging().apnsToken = deviceToken
        print("📱 APNS token set in Firebase")
    }

    func application(
        _ application: UIApplication,
        didFailToRegisterForRemoteNotificationsWithError error: Error
    ) {
        print("❌ Failed to register for remote notifications: \(error.localizedDescription)")
    }

    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        print("✅ FCM token received: \(fcmToken ?? "nil")")
        
        // Subscribe to topics once we have the token
        Messaging.messaging().subscribe(toTopic: "GENERAL") { error in
            if let error = error {
                print("❌ Failed to subscribe to topic: \(error)")
            } else {
                print("✅ Successfully subscribed to GENERAL topic")
            }
        }
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        completionHandler([.banner, .sound, .badge])
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        completionHandler()
    }
}
