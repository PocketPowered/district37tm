package com.wongislandd.nexus.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.navigation.NavController
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
        DisposableEffect(navHostController) {
            val listener = NavController.OnDestinationChangedListener { _, destination, args ->
                Logger.i("Navigating to ${destination}, args: $args")
                destination.route?.also {
                    appViewModel.navigationSlice.confirmNavigationToRoute(it)
                }
            }
            navHostController.addOnDestinationChangedListener(listener)
            onDispose {
                navHostController.removeOnDestinationChangedListener(listener)
            }
        }
        content(navHostController)
    }
}
