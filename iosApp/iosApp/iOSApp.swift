import SwiftUI
import ComposeApp
import Firebase // Add this line

@main
struct iOSApp: App {

    init() {
        AppModuleKt.initializeKoin(context: nil)
        FirebaseApp.configure()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}