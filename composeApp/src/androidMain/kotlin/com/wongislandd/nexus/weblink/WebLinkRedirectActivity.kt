package com.wongislandd.nexus.weblink

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity

class WebLinkRedirectActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve the URL from the intent's bundle
        val url = intent?.getStringExtra(EXTRA_URL)

        if (url != null) {
            // Create an implicit intent to open the URL in a browser
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(browserIntent)
        }

        // Close the activity after launching the browser
        finish()
    }

    companion object {
        const val EXTRA_URL = "extra_url"
    }
}