package com.district37.toastmasters.di

import com.district37.toastmasters.RequestContextProvider
import com.district37.toastmasters.RequestContextProviderImpl
import org.koin.core.module.dsl.scopedOf
import org.koin.dsl.module
import org.koin.ktor.plugin.RequestScope

/**
 * For services that require request context
 */
val requestModule = module {
    scope<RequestScope> {
        scopedOf(::RequestContextProviderImpl)
        scoped<RequestContextProvider> { get<RequestContextProviderImpl>() }
    }
}