package com.wongislandd.nexus.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import co.touchlab.kermit.Logger
import com.district37.toastmasters.LocalAppViewModel

@Composable
fun NavHostControllerProvider(
    navHostController: NavHostController = rememberNavController(),
    content: @Composable NavHostController.() -> Unit
) {
    val appViewModel = LocalAppViewModel.current
    CompositionLocalProvider(LocalNavHostController provides navHostController) {
        navHostController.addOnDestinationChangedListener { _, destination, args ->
            Logger.i("Navigating to ${destination}, args: $args")
            // TODO Remove this route tracking based on string, or at least tighten it
            destination.route?.also {
                appViewModel.navigationSlice.confirmNavigationToRoute(it)
            }
        }
        content(navHostController)
    }
}