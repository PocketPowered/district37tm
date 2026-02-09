package com.district37.toastmasters.infra

import com.district37.toastmasters.util.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSString
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.UISceneActivationStateForegroundActive

/**
 * iOS implementation of ShareManager using UIActivityViewController
 */
actual class ShareManager {
    private val TAG = "ShareManager"

    /**
     * Share a URL using iOS's native share functionality
     * @param url The URL to share
     * @param title Optional title (not used on iOS)
     */
    @OptIn(ExperimentalForeignApi::class)
    actual fun share(url: String, title: String?) {
        try {
            // Create activity items with the URL
            val activityItems = listOf(url as NSString)

            // Create activity view controller
            val activityViewController = UIActivityViewController(
                activityItems = activityItems,
                applicationActivities = null
            )

            // Get the active window scene and root view controller
            val windowScene = UIApplication.sharedApplication.connectedScenes
                .firstOrNull { scene ->
                    scene is UIWindowScene &&
                    scene.activationState == UISceneActivationStateForegroundActive
                } as? UIWindowScene

            val window = windowScene?.windows?.firstOrNull() as? UIWindow
            val rootViewController = window?.rootViewController

            // Present the share sheet
            rootViewController?.presentViewController(
                viewControllerToPresent = activityViewController,
                animated = true,
                completion = null
            )

            Logger.d(TAG, "Presented share dialog for URL: $url")
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to share URL: ${e.message}")
        }
    }
}
