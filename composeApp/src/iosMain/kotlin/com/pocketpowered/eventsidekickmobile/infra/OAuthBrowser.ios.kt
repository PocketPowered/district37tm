package com.district37.toastmasters.infra

import com.district37.toastmasters.util.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AuthenticationServices.ASPresentationAnchor
import platform.AuthenticationServices.ASWebAuthenticationPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASWebAuthenticationSession
import platform.AuthenticationServices.ASWebAuthenticationSessionCompletionHandler
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.UIWindowSceneSessionRoleApplication
import platform.darwin.NSObject
import kotlin.coroutines.resume

/**
 * Provides the presentation anchor (key window) for ASWebAuthenticationSession.
 * Must extend NSObject for Objective-C interop.
 */
private class PresentationContextProvider : NSObject(), ASWebAuthenticationPresentationContextProvidingProtocol {
    override fun presentationAnchorForWebAuthenticationSession(
        session: ASWebAuthenticationSession
    ): ASPresentationAnchor {
        // Get the key window from the active window scene (iOS 13+)
        val windowScene = UIApplication.sharedApplication.connectedScenes
            .filterIsInstance<UIWindowScene>()
            .firstOrNull { it.activationState == platform.UIKit.UISceneActivationStateForegroundActive }

        return windowScene?.windows?.firstOrNull { (it as? UIWindow)?.isKeyWindow() == true } as? ASPresentationAnchor
            ?: windowScene?.windows?.firstOrNull() as? ASPresentationAnchor
            ?: UIApplication.sharedApplication.windows.firstOrNull() as? ASPresentationAnchor
            ?: throw IllegalStateException("No window available for presentation")
    }
}

/**
 * iOS implementation of OAuthBrowser using ASWebAuthenticationSession.
 * Provides secure OAuth flow with automatic callback handling.
 */
@OptIn(ExperimentalForeignApi::class)
actual class OAuthBrowser {

    private val TAG = "OAuthBrowser"
    private val presentationContextProvider = PresentationContextProvider()

    actual suspend fun launchOAuth(url: String): Result<String> {
        return suspendCancellableCoroutine { continuation ->
            // Track if continuation has been resumed to prevent double-resumption
            // This can happen if session.start() fails but completionHandler still fires
            val resumed = kotlin.concurrent.AtomicReference(false)

            fun tryResume(result: Result<String>) {
                if (resumed.compareAndSet(false, true)) {
                    continuation.resume(result)
                }
            }

            val nsUrl = NSURL.URLWithString(url)
            if (nsUrl == null) {
                Logger.e(TAG, "Invalid OAuth URL: $url")
                tryResume(Result.failure(Exception("Invalid OAuth URL")))
                return@suspendCancellableCoroutine
            }

            val session = ASWebAuthenticationSession(
                uRL = nsUrl,
                callbackURLScheme = OAUTH_CALLBACK_SCHEME,
                completionHandler = { callbackUrl: NSURL?, error: NSError? ->
                    when {
                        callbackUrl != null -> {
                            val urlString = callbackUrl.absoluteString
                            if (urlString != null) {
                                Logger.d(TAG, "OAuth callback received")
                                tryResume(Result.success(urlString))
                            } else {
                                Logger.e(TAG, "Callback URL has no absolute string")
                                tryResume(Result.failure(Exception("Invalid callback URL")))
                            }
                        }
                        error != null -> {
                            // Check if user cancelled
                            if (error.code == 1L) { // ASWebAuthenticationSessionErrorCode.canceledLogin
                                Logger.d(TAG, "User cancelled OAuth")
                                tryResume(Result.failure(Exception("Login cancelled")))
                            } else {
                                Logger.e(TAG, "OAuth error: ${error.localizedDescription}")
                                tryResume(Result.failure(Exception(error.localizedDescription ?: "OAuth failed")))
                            }
                        }
                        else -> {
                            Logger.e(TAG, "OAuth completed with no URL or error")
                            tryResume(Result.failure(Exception("OAuth failed")))
                        }
                    }
                }
            )

            // Share cookies with Safari for persistent login sessions
            session.prefersEphemeralWebBrowserSession = false

            // Set the presentation context provider (required on iOS 13+)
            session.presentationContextProvider = presentationContextProvider

            Logger.d(TAG, "Starting ASWebAuthenticationSession...")
            val started = session.start()

            if (!started) {
                Logger.e(TAG, "Failed to start ASWebAuthenticationSession")
                tryResume(Result.failure(Exception("Failed to start authentication")))
            }

            continuation.invokeOnCancellation {
                session.cancel()
            }
        }
    }
}
