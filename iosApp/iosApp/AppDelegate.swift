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
        // Initialize Firebase
        FirebaseApp.configure()

        // Initialize Koin
        AppModuleKt.initializeKoin(context: nil)

        // Setup push notifications
        UNUserNotificationCenter.current().delegate = self
        Messaging.messaging().delegate = self

        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { granted, error in
            print("📲 Push permission granted: \(granted)")
        }

        application.registerForRemoteNotifications()

        Messaging.messaging().token { token, error in
            if let token = token {
                print("📬 Manual FCM token: \(token)")
            } else if let error = error {
                print("❌ Error fetching FCM token: \(error.localizedDescription)")
            }
        }

        return true
    }

    func application(
        _ application: UIApplication,
        didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
    ) {
        Messaging.messaging().apnsToken = deviceToken
    }

    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        print("✅ FCM token received via delegate: \(fcmToken ?? "nil")")
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        completionHandler([.banner, .sound])
    }
}
