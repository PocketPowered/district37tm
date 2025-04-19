import SwiftUI
import ComposeApp
import Firebase

@main
struct iOSApp: App {

    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate

    init() {
        AppModuleKt.initializeKoin(context: nil)
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}