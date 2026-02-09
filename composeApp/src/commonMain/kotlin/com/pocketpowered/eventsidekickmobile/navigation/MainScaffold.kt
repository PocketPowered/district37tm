package com.district37.toastmasters.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import com.district37.toastmasters.components.navigation.FloatingBottomNav
import com.district37.toastmasters.components.navigation.FloatingTopBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.district37.toastmasters.LocalNavController
import com.district37.toastmasters.auth.AuthViewModel
import com.district37.toastmasters.auth.models.AuthState
import com.district37.toastmasters.infra.DeveloperSettingsManager
import com.district37.toastmasters.infra.PinnedEventManager
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    authViewModel: AuthViewModel = koinViewModel(),
    deeplinkHandler: DeeplinkHandler = koinInject(),
    developerSettingsManager: DeveloperSettingsManager = koinInject(),
    pinnedEventManager: PinnedEventManager = koinInject()
) {
    // Single NavController for the entire app
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    // Check if user is authenticated for enabling/disabling Create tab
    val authState by authViewModel.authState.collectAsState()
    val isAuthenticated = authState is AuthState.Authenticated
    val user = (authState as? AuthState.Authenticated)?.user
    val userAvatarUrl = user?.effectiveAvatarUrl
    val needsOnboarding = user?.needsOnboarding ?: false

    // Check if developer wants to test onboarding
    var shouldTestOnboarding by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        shouldTestOnboarding = developerSettingsManager.getShouldShowOnboardingOnNextLaunch()
    }

    // Create TopAppBar controller early so it's available for onboarding
    val topAppBarController = remember { TopAppBarController() }

    // Show onboarding if:
    // 1. User is authenticated but hasn't completed onboarding (real flow)
    // 2. Developer has set the test flag (test flow)
    if (isAuthenticated && (needsOnboarding || shouldTestOnboarding)) {
        CompositionLocalProvider(
            LocalTopAppBarController provides topAppBarController
        ) {
            com.district37.toastmasters.features.onboarding.OnboardingWizardScreen(
                onOnboardingComplete = {
                    // Clear the test flag if it was a test
                    if (shouldTestOnboarding) {
                        scope.launch {
                            developerSettingsManager.clearOnboardingTestFlag()
                        }
                        shouldTestOnboarding = false
                    }
                    // For real onboarding, auth state will be refreshed automatically by OnboardingWizardViewModel
                }
            )
        }
        return
    }

    // Observe pending deeplinks
    val pendingDeeplink by deeplinkHandler.pendingDeeplink.collectAsState()

    // Handle pinned event navigation on cold start
    // This happens AFTER auth check but respects deeplinks (which take priority)
    var pinnedEventHandled by remember { mutableStateOf(false) }

    LaunchedEffect(authState, pendingDeeplink, pinnedEventHandled) {
        // Only navigate once, when authenticated, no pending deeplink, and not already handled
        if (!pinnedEventHandled &&
            authState is AuthState.Authenticated &&
            pendingDeeplink == null) {

            val pinnedEventId = pinnedEventManager.getPinnedEventId()
            if (pinnedEventId != null) {
                // Navigate to the pinned event via DeeplinkHandler
                deeplinkHandler.handleDeeplink("eventsidekick://event/$pinnedEventId")
            }
            pinnedEventHandled = true
        }

        // Also mark as handled if auth check completed but user is not authenticated
        if (!pinnedEventHandled && authState is AuthState.Unauthenticated) {
            pinnedEventHandled = true
        }
    }

    // Read current configuration from controller
    val config by topAppBarController.config
    val onBackClick by topAppBarController.onBackClick
    val bottomNavInsets = if (config.hideBottomNav) {
        BottomNavInsets(bottomNavContentHeight = 0.dp, bottomNavVerticalPadding = 0.dp, safetyMargin = 0.dp)
    } else {
        BottomNavInsets()
    }

    // Determine current tab from navigation state using TabContext utility
    val currentTab = remember(navBackStackEntry) {
        navController.currentTabContext()
    }

    // Convert TabContext to BottomNavTab for UI
    val selectedBottomNavTab = when (currentTab) {
        TabContext.EXPLORE -> BottomNavTab.Explore
        TabContext.CREATE -> BottomNavTab.Create
        TabContext.ACCOUNT -> BottomNavTab.Account
    }

    // Box-based layout with floating elements
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Provide composition locals for all descendants
        CompositionLocalProvider(
            LocalTopAppBarController provides topAppBarController,
            LocalTopAppBarInsets provides topAppBarController.insets.value,
            LocalBottomNavInsets provides bottomNavInsets,
            LocalNavController provides navController
        ) {
            // Single unified NavGraph for all navigation
            UnifiedNavGraph(
                navController = navController,
                startDestination = ExploreRoute,
                deeplinkHandler = deeplinkHandler
            )

            // Top fade gradient and floating top bar (only if not hidden)
            if (!config.hideTopBar) {
                // Top fade gradient (content fades as it scrolls under floating top bar)
                val defaultBackgroundColor = MaterialTheme.colorScheme.background
                val backgroundColor = config.gradientColor ?: defaultBackgroundColor
                val fadeGradientHeight = TopAppBarInsets().fadeGradientHeight
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(fadeGradientHeight)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.00f to backgroundColor,
                                    0.20f to backgroundColor,
                                    0.35f to backgroundColor.copy(alpha = 0.98f),
                                    0.45f to backgroundColor.copy(alpha = 0.95f),
                                    0.55f to backgroundColor.copy(alpha = 0.85f),
                                    0.65f to backgroundColor.copy(alpha = 0.65f),
                                    0.75f to backgroundColor.copy(alpha = 0.40f),
                                    0.85f to backgroundColor.copy(alpha = 0.20f),
                                    0.92f to backgroundColor.copy(alpha = 0.08f),
                                    1.00f to backgroundColor.copy(alpha = 0.0f)
                                )
                            )
                        )
                )

                // Floating top bar (overlays content)
                FloatingTopBar(
                    config = config,
                    onBackClick = onBackClick,
                    userAvatarUrl = userAvatarUrl,
                    onAvatarClick = {
                        // Navigate to Account tab when avatar is clicked
                        navController.navigate(AccountHomeRoute) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }

            // Floating bottom nav (overlays content)
            if (!config.hideBottomNav) {
                FloatingBottomNav(
                    selectedTab = selectedBottomNavTab,
                    onTabSelected = { tab ->
                        val targetContext = when (tab) {
                            BottomNavTab.Explore -> TabContext.EXPLORE
                            BottomNavTab.Create -> TabContext.CREATE
                            BottomNavTab.Account -> TabContext.ACCOUNT
                        }

                        if (currentTab == targetContext) {
                            // Already on this tab - reset to root
                            navController.resetToTabRoot(targetContext)
                        } else {
                            // Navigate to tab's root destination
                            val rootRoute: Any = when (tab) {
                                BottomNavTab.Explore -> ExploreRoute
                                BottomNavTab.Create -> CreateHubRoute
                                BottomNavTab.Account -> AccountHomeRoute
                            }
                            navController.navigate(rootRoute) {
                                // Pop up to start to avoid deep back stacks
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    isAuthenticated = isAuthenticated,
                    userAvatarUrl = userAvatarUrl,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}
