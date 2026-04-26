package com.district37.toastmasters.devsettings

import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val devSettingsModule = module {
    viewModelOf(::DevSettingsViewModel)
}
