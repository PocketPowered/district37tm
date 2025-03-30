package com.wongislandd.nexus.weblink

import org.koin.dsl.module

fun webLinkModule(appContext: Any?) = module {
    single<WebLinkRouter> { WebLinkRouterImpl(appContext) }
}