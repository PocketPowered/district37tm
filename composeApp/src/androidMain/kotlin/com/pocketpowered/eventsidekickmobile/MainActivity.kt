package com.district37.toastmasters

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.district37.toastmasters.infra.OAUTH_CALLBACK_HOST
import com.district37.toastmasters.infra.OAUTH_CALLBACK_SCHEME
import com.district37.toastmasters.infra.OAuthCallbackHandler
import com.district37.toastmasters.navigation.DeeplinkHandler
import com.district37.toastmasters.util.Logger
import org.koin.android.ext.android.inject

/**
 * Main Activity for the EventSidekick Android app
 */
class MainActivity : ComponentActivity() {
    private val deeplinkHandler: DeeplinkHandler by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge with dark status bar icons
        enableEdgeToEdge()

        // Handle OAuth deep link if this is a callback
        handleDeepLink(intent)

        setContent {
            App()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    /**
     * Handle deep link callback (OAuth and entity sharing)
     */
    private fun handleDeepLink(intent: Intent?) {
        intent?.data?.let { uri ->
            val url = uri.toString()
            Logger.d("MainActivity", "Deeplink received: $url")

            // Check if it's the legacy OAuth callback (specific host check)
            if (uri.scheme == OAUTH_CALLBACK_SCHEME && uri.host == OAUTH_CALLBACK_HOST) {
                OAuthCallbackHandler.handleCallback(uri)
            } else {
                // Handle all other deeplinks through DeeplinkHandler
                deeplinkHandler.handleDeeplink(url)
            }
        }
    }
}
