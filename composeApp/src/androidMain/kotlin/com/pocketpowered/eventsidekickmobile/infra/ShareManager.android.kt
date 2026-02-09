package com.district37.toastmasters.infra

import android.content.Context
import android.content.Intent
import com.district37.toastmasters.util.Logger

/**
 * Android implementation of ShareManager using the native share sheet
 */
actual class ShareManager(private val context: Context) {
    private val TAG = "ShareManager"

    /**
     * Share a URL using Android's native share functionality
     * @param url The URL to share
     * @param title Optional title for the share dialog
     */
    actual fun share(url: String, title: String?) {
        try {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, url)
                title?.let { putExtra(Intent.EXTRA_TITLE, it) }
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, title ?: "Share")
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(shareIntent)

            Logger.d(TAG, "Launched share dialog for URL: $url")
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to share URL: ${e.message}")
        }
    }
}
