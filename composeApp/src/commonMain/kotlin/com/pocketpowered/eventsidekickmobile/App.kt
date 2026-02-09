package com.district37.toastmasters

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavController
import com.district37.toastmasters.auth.AuthViewModel
import com.district37.toastmasters.auth.models.AuthState
import com.district37.toastmasters.infra.UserPreferencesManager
import com.district37.toastmasters.infra.calendar.CalendarAutoSyncService
import com.district37.toastmasters.infra.calendar.CalendarReconciliationService
import com.district37.toastmasters.infra.calendar.CalendarSyncManager
import com.district37.toastmasters.infra.calendar.ReinstallReconciliationResult
import com.district37.toastmasters.navigation.MainScaffold
import com.district37.toastmasters.theme.MinimalistDarkColors
import com.district37.toastmasters.theme.MinimalistLightColors
import com.district37.toastmasters.util.Logger
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * Composition local for NavController access within each tab
 */
val LocalNavController = staticCompositionLocalOf<NavController> {
    error("No NavController provided")
}

/**
 * Main App composable for EventSidekick
 */
@Composable
fun App() {
    val authViewModel: AuthViewModel = koinViewModel()
    val authState by authViewModel.authState.collectAsState()
    val reconciliationService: CalendarReconciliationService = koinInject()
    val calendarAutoSyncService: CalendarAutoSyncService = koinInject()
    val calendarSyncManager: CalendarSyncManager = koinInject()
    val userPreferencesManager: UserPreferencesManager = koinInject()

    // Start calendar auto-sync service to listen for engagement updates
    LaunchedEffect(Unit) {
        calendarAutoSyncService.start()
    }

    // Load synced events from server when user is authenticated
    // This restores sync state after app reinstall
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            try {
                calendarSyncManager.loadSyncedEvents()

                // Check if this is first launch after install/reinstall
                if (userPreferencesManager.isFirstLaunchAfterInstall()) {
                    Logger.d("App", "First launch after install detected, running reinstall reconciliation")
                    val shouldMarkComplete = when (val result = reconciliationService.reconcileOnReinstall()) {
                        is ReinstallReconciliationResult.Success -> {
                            Logger.d("App", "Reinstall reconciliation: relinked=${result.relinkedCount}, needsResync=${result.needsResyncCount}, errors=${result.errorCount}")
                            if (result.hasIssues) {
                                Logger.d("App", "Some calendar events may need to be re-synced")
                            }
                            true // Reconciliation ran successfully
                        }
                        is ReinstallReconciliationResult.NoRecords -> {
                            Logger.d("App", "No sync records on server, nothing to reconcile")
                            true // No records to reconcile, mark as complete
                        }
                        is ReinstallReconciliationResult.PermissionDenied -> {
                            Logger.d("App", "Calendar permission denied, will retry when permission granted")
                            false // Don't mark complete - retry when permission is granted
                        }
                        is ReinstallReconciliationResult.Error -> {
                            Logger.e("App", "Reinstall reconciliation error: ${result.message}")
                            false // Don't mark complete - retry later
                        }
                    }
                    if (shouldMarkComplete) {
                        userPreferencesManager.markFirstLaunchCompleted()
                    }
                }
            } catch (e: Exception) {
                Logger.e("App", "Failed to load synced events: ${e.message}")
            }
        }
    }

    // Run calendar reconciliation once on app launch (silently in background)
    LaunchedEffect(Unit) {
        try {
            val result = reconciliationService.reconcileAll()
            if (result.hasChanges) {
                Logger.d("App", "Calendar reconciliation: ${result.deleted} deleted, ${result.updated} updated")
            }
        } catch (e: Exception) {
            Logger.e("App", "Calendar reconciliation failed: ${e.message}")
        }
    }

    // Determine if dark theme should be used based on user preference
    val useDarkTheme = when (val state = authState) {
        is AuthState.Authenticated -> {
            when (state.user.preferences?.theme) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme() // "system" or null
            }
        }
        else -> isSystemInDarkTheme() // Default to system when not authenticated
    }

    val colorScheme = if (useDarkTheme) MinimalistDarkColors else MinimalistLightColors

    MaterialTheme(colorScheme = colorScheme) {
        MainScaffold()
    }
}
