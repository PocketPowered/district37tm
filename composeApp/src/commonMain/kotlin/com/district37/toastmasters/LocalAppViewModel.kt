package com.district37.toastmasters

import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppViewModel = staticCompositionLocalOf<AppViewModel> {
    error("No AppViewModel provided")
}