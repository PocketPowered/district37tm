package com.district37.toastmasters.splash

import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val splashModule = module {
    viewModelOf(::SplashViewModel)
} 