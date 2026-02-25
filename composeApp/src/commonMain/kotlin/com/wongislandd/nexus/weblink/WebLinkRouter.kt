package com.wongislandd.nexus.weblink

interface WebLinkRouter {
    fun openWebLink(url: String)
}

expect class WebLinkRouterImpl(context: Any? = null) : WebLinkRouter {
    override fun openWebLink(url: String)
}
