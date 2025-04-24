import UIKit
import Firebase
import UserNotifications
import ComposeApp

class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate, MessagingDelegate {
    private lazy var notificationRepository: NotificationRepository = {
        return KoinBridge.shared.getNotificationsRepository()
    }()
    
    private var storedFCMToken: String?
    private var hasAPNSToken = false
    private var hasSubscribedToTopics = false

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
        hasAPNSToken = true
        
        // If we already have the FCM token, subscribe to topics
        if let fcmToken = storedFCMToken {
            subscribeToTopics(fcmToken: fcmToken)
        }
    }

    func application(
        _ application: UIApplication,
        didFailToRegisterForRemoteNotificationsWithError error: Error
    ) {
        print("❌ Failed to register for remote notifications: \(error.localizedDescription)")
    }

    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        print("✅ FCM token received: \(fcmToken ?? "nil")")
        
        if let token = fcmToken {
            storedFCMToken = token
            
            // If we already have the APNS token, subscribe to topics
            if hasAPNSToken {
                subscribeToTopics(fcmToken: token)
            }
        }
    }
    
    private func subscribeToTopics(fcmToken: String) {
        // If we've already successfully subscribed, don't do it again
        guard !hasSubscribedToTopics else {
            print("ℹ️ Already subscribed to topics, skipping")
            return
        }
        
        print("📡 Subscribing to topics with FCM token: \(fcmToken)")
        Messaging.messaging().subscribe(toTopic: "GENERAL") { error in
            if let error = error {
                print("❌ Failed to subscribe to topic: \(error)")
            } else {
                print("✅ Successfully subscribed to GENERAL topic")
                self.hasSubscribedToTopics = true
            }
        }
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        // Handle the notification data
        let userInfo = notification.request.content.userInfo
        
        // Show the notification
        completionHandler([.banner, .sound, .badge])
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        completionHandler()
    }
    
    func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable: Any]) {
        print("📩 Received remote notification: \(userInfo)")
        handleNotificationData(userInfo)
    }
    
    private func handleNotificationData(_ userInfo: [AnyHashable: Any]) {
        // Extract data from the notification
        let data = userInfo as? [String: Any] ?? [:]
        let title = data["title"] as? String ?? "Notification"
        let body = data["body"] as? String ?? ""
        let type = data["type"] as? String ?? "notification"
        let relatedEventId = data["relatedEventId"] as? String
        
        print("📬 Received notification - Title: \(title), Body: \(body), Type: \(type), EventId: \(relatedEventId ?? "nil")")
        
        // Insert notification into repository
        do {
            try notificationRepository.insertNotification(header: title, description: body)
            print("✅ Successfully inserted notification into database")
        } catch {
            print("❌ Failed to insert notification: \(error.localizedDescription)")
        }
        
        // Create and show local notification
        let content = UNMutableNotificationContent()
        content.title = title
        content.body = body
        content.sound = .default
        
        // Add any additional data
        if let eventId = relatedEventId {
            content.userInfo = ["relatedEventId": eventId]
        }
        
        // Create request
        let request = UNNotificationRequest(
            identifier: UUID().uuidString,
            content: content,
            trigger: nil // Show immediately
        )
        
        // Add request to notification center
        UNUserNotificationCenter.current().add(request) { error in
            if let error = error {
                print("❌ Failed to show notification: \(error.localizedDescription)")
            } else {
                print("✅ Successfully showed notification")
            }
        }
    }
}
