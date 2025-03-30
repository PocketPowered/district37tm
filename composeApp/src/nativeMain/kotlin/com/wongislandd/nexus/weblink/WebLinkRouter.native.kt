package com.wongislandd.nexus.weblink

import co.touchlab.kermit.Logger
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual class WebLinkRouterImpl actual constructor(context: Any?) : WebLinkRouter {
    override fun openWebLink(url: String) {
        NSURL.URLWithString(url)?.let {
            UIApplication.sharedApplication.openURL(it)
        } ?: Logger.e("Invalid URL: $url")

    }
}