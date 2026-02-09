package com.district37.toastmasters.infra

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.district37.toastmasters.util.Logger
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Singleton to bridge deep link callbacks from Activity to coroutines
 */
object OAuthCallbackHandler {
    private var pendingCallback: ((Result<String>) -> Unit)? = null

    fun setCallback(callback: (Result<String>) -> Unit) {
        pendingCallback = callback
    }

    fun handleCallback(uri: Uri) {
        val callback = pendingCallback
        pendingCallback = null

        if (callback != null) {
            callback(Result.success(uri.toString()))
        } else {
            Logger.e("OAuthCallbackHandler", "Received callback but no handler registered")
        }
    }

    fun handleError(error: Exception) {
        val callback = pendingCallback
        pendingCallback = null
        callback?.invoke(Result.failure(error))
    }

    fun cancel() {
        val callback = pendingCallback
        pendingCallback = null
        callback?.invoke(Result.failure(Exception("Login cancelled")))
    }
}

/**
 * Android implementation of OAuthBrowser using Chrome Custom Tabs.
 * Opens OAuth URL in Custom Tab and listens for deep link callback.
 */
actual class OAuthBrowser(private val context: Context) {

    private val TAG = "OAuthBrowser"

    actual suspend fun launchOAuth(url: String): Result<String> {
        return suspendCancellableCoroutine { continuation ->
            try {
                // Set up callback handler
                OAuthCallbackHandler.setCallback { result ->
                    if (continuation.isActive) {
                        continuation.resume(result)
                    }
                }

                // Launch Custom Tab with OAuth URL
                val customTabsIntent = CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .build()

                // Add FLAG_ACTIVITY_NEW_TASK since we're launching from a non-Activity context
                customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                Logger.d(TAG, "Launching OAuth URL in Custom Tab...")
                customTabsIntent.launchUrl(context, Uri.parse(url))

            } catch (e: Exception) {
                Logger.e(TAG, "Failed to launch Custom Tab: ${e.message}")
                if (continuation.isActive) {
                    continuation.resume(Result.failure(e))
                }
            }

            continuation.invokeOnCancellation {
                OAuthCallbackHandler.cancel()
            }
        }
    }
}
