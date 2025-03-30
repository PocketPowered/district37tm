package com.wongislandd.nexus.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import co.touchlab.kermit.Logger

@Composable
fun NavHostControllerProvider(
    navHostController: NavHostController = rememberNavController(),
    content: @Composable NavHostController.() -> Unit
) {
    CompositionLocalProvider(LocalNavHostController provides navHostController) {
        navHostController.addOnDestinationChangedListener { _, destination, args ->
            Logger.i("Navigating to ${destination}, args: $args")
        }
        content(navHostController)
    }
}