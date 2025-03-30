package com.wongislandd.nexus.weblink

import android.content.Context
import android.content.Intent

actual class WebLinkRouterImpl actual constructor(private val context: Any?) : WebLinkRouter {
    override fun openWebLink(url: String) {
        val realContext = requireNotNull(context as Context) {
            "Context must be provided"
        }
        val intent = Intent(realContext, WebLinkRedirectActivity::class.java).apply {
            putExtra(WebLinkRedirectActivity.EXTRA_URL, url)
        }
        realContext.startActivity(intent)
    }
}