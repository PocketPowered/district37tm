import UIKit
import Firebase
import UserNotifications
import ComposeApp

class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate, MessagingDelegate {
    private lazy var notificationRepository: NotificationRepository = {
        KoinBridge.shared.getNotificationsRepository()
    }()

    private var storedFCMToken: String?
    private var hasAPNSToken = false
    private var hasSubscribedToGeneralTopic = false

    private let backgroundQueue = DispatchQueue(
        label: "com.district37.toastmasters.background",
        qos: .userInitiated
    )

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        print("🚀 App starting up...")

        FirebaseApp.configure()
        print("🔥 Firebase configured")

        AppModuleKt.initializeKoin(context: application)
        print("🔄 Koin initialized")

        UNUserNotificationCenter.current().delegate = self
        Messaging.messaging().delegate = self
        print("🔔 Notification delegates set")

        registerForRemoteNotificationsIfAuthorized(application)

        return true
    }

    func applicationDidBecomeActive(_ application: UIApplication) {
        registerForRemoteNotificationsIfAuthorized(application)
    }

    private func registerForRemoteNotificationsIfAuthorized(_ application: UIApplication) {
        UNUserNotificationCenter.current().getNotificationSettings { settings in
            print("📱 Current notification settings: \(settings.authorizationStatus.rawValue)")

            switch settings.authorizationStatus {
            case .authorized, .provisional, .ephemeral:
                DispatchQueue.main.async {
                    if !self.hasAPNSToken {
                        application.registerForRemoteNotifications()
                    }
                }
            case .notDetermined:
                print("ℹ️ Notification permission has not been requested yet")
            case .denied:
                print("❌ Notifications denied by user")
            @unknown default:
                print("❓ Unknown authorization status")
            }
        }
    }

    func application(
        _ application: UIApplication,
        didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
    ) {
        print("📱 Received APNS token")
        DispatchQueue.main.async {
            Messaging.messaging().apnsToken = deviceToken
            self.hasAPNSToken = true
            if let token = self.storedFCMToken {
                self.subscribeToGeneralTopic(fcmToken: token)
            }
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
        guard let token = fcmToken else { return }
        storedFCMToken = token
        if hasAPNSToken {
            subscribeToGeneralTopic(fcmToken: token)
        }
    }

    private func subscribeToGeneralTopic(fcmToken: String) {
        guard !hasSubscribedToGeneralTopic else {
            return
        }
        print("📡 Subscribing iOS to GENERAL topic with FCM token: \(fcmToken)")
        backgroundQueue.async { [weak self] in
            Messaging.messaging().subscribe(toTopic: "GENERAL") { error in
                if let error = error {
                    print("❌ Failed to subscribe to GENERAL topic: \(error.localizedDescription)")
                } else {
                    print("✅ iOS subscribed to GENERAL topic")
                    self?.hasSubscribedToGeneralTopic = true
                }
            }
        }
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        backgroundQueue.async { [weak self] in
            self?.handleNotificationData(notification.request.content.userInfo)
        }
        completionHandler([.banner, .sound, .badge])
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        backgroundQueue.async { [weak self] in
            self?.handleNotificationData(response.notification.request.content.userInfo)
        }
        completionHandler()
    }

    func application(
        _ application: UIApplication,
        didReceiveRemoteNotification userInfo: [AnyHashable: Any],
        fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void
    ) {
        print("📩 Received remote notification: \(userInfo)")
        backgroundQueue.async { [weak self, completionHandler] in
            self?.handleNotificationData(userInfo)
            completionHandler(.newData)
        }
    }

    private func handleNotificationData(_ userInfo: [AnyHashable: Any]) {
        let payload = extractPayload(userInfo)
        let title = payload.title
        let body = payload.body
        let relatedEventId = payload.relatedEventId

        print("📬 Received notification - Title: \(title), Body: \(body), EventId: \(relatedEventId ?? "nil")")

        guard !title.isEmpty || !body.isEmpty else {
            print("ℹ️ Notification payload missing title/body; skipping persistence")
            return
        }

        do {
            try notificationRepository.insertNotification(header: title, description: body)
            print("✅ Successfully inserted notification into database")
        } catch {
            print("❌ Failed to insert notification: \(error.localizedDescription)")
        }
    }

    private func extractPayload(_ userInfo: [AnyHashable: Any]) -> (title: String, body: String, relatedEventId: String?) {
        let data = userInfo as? [String: Any] ?? [:]

        if let aps = data["aps"] as? [String: Any] {
            if let alert = aps["alert"] as? [String: Any] {
                let title = alert["title"] as? String ?? ""
                let body = alert["body"] as? String ?? ""
                let relatedEventId = data["relatedEventId"] as? String
                return (title, body, relatedEventId)
            }
            if let body = aps["alert"] as? String {
                let title = data["title"] as? String ?? "Notification"
                let relatedEventId = data["relatedEventId"] as? String
                return (title, body, relatedEventId)
            }
        }

        let title = data["title"] as? String
            ?? data["gcm.notification.title"] as? String
            ?? "Notification"
        let body = data["body"] as? String
            ?? data["gcm.notification.body"] as? String
            ?? ""
        let relatedEventId = data["relatedEventId"] as? String

        return (title, body, relatedEventId)
    }
}
