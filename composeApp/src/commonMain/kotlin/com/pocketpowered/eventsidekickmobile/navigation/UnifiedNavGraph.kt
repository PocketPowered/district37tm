package com.district37.toastmasters.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.district37.toastmasters.auth.AuthViewModel
import com.district37.toastmasters.auth.models.AuthState
import com.district37.toastmasters.features.account.AttendingEventsScreen
import com.district37.toastmasters.features.account.AttendingEventsViewModel
import com.district37.toastmasters.features.account.EditProfileScreen
import com.district37.toastmasters.features.account.EditProfileViewModel
import com.district37.toastmasters.features.account.FriendsListScreen
import com.district37.toastmasters.features.account.FriendsListViewModel
import com.district37.toastmasters.features.account.LoginScreen
import com.district37.toastmasters.features.account.SavedEventsScreen
import com.district37.toastmasters.features.account.SavedEventsViewModel
import com.district37.toastmasters.features.account.AppearanceSettingsScreen
import com.district37.toastmasters.features.account.CalendarSelectionScreen
import com.district37.toastmasters.features.account.CalendarSettingsScreen
import com.district37.toastmasters.features.account.SettingsScreen
import com.district37.toastmasters.features.account.SyncedItemsScreen
import com.district37.toastmasters.features.account.SettingsViewModel
import com.district37.toastmasters.features.account.UserProfileScreen
import com.district37.toastmasters.features.account.UserProfileViewModel
import com.district37.toastmasters.features.collaborators.manage.ManageCollaboratorsScreen
import com.district37.toastmasters.features.collaborators.picker.UserPickerScreen
import com.district37.toastmasters.features.create.CreateHubScreen
import com.district37.toastmasters.features.create.event.CreateEventWizardScreen
import com.district37.toastmasters.features.create.organization.CreateOrganizationWizardScreen
import com.district37.toastmasters.features.create.performer.CreatePerformerWizardScreen
import com.district37.toastmasters.features.create.picker.LocationPickerScreen
import com.district37.toastmasters.features.create.picker.PerformerPickerScreen
import com.district37.toastmasters.features.create.picker.VenuePickerScreen
import com.district37.toastmasters.features.create.venue.CreateVenueWizardScreen
import com.district37.toastmasters.features.create.viewall.ArchivedEventsScreen
import com.district37.toastmasters.features.create.viewall.MyEditableEventsScreen
import com.district37.toastmasters.features.create.viewall.MyEditablePerformersScreen
import com.district37.toastmasters.features.create.viewall.MyEditableVenuesScreen
import com.district37.toastmasters.features.developer.DeveloperScreen
import com.district37.toastmasters.features.edit.event.EditEventScreen
import com.district37.toastmasters.features.edit.location.EditLocationScreen
import com.district37.toastmasters.features.edit.organization.EditOrganizationScreen
import com.district37.toastmasters.features.edit.performer.EditPerformerScreen
import com.district37.toastmasters.features.edit.venue.EditVenueScreen
import com.district37.toastmasters.features.events.AllEventsScreen
import com.district37.toastmasters.features.events.EventDetailScreen
import com.district37.toastmasters.features.events.EventListScreen
import com.district37.toastmasters.features.explore.ExploreScreen
import com.district37.toastmasters.features.locations.LocationDetailScreen
import com.district37.toastmasters.features.messaging.ChatScreen
import com.district37.toastmasters.features.messaging.ConversationsScreen
import com.district37.toastmasters.features.messaging.NewConversationScreen
import com.district37.toastmasters.features.notifications.NotificationCenterScreen
import com.district37.toastmasters.features.organizations.EventsByOrganization
import com.district37.toastmasters.features.organizations.OrganizationDetailScreen
import com.district37.toastmasters.features.organizations.members.ManageOrganizationMembersScreen
import com.district37.toastmasters.features.organizations.members.OrganizationMemberPickerScreen
import com.district37.toastmasters.features.performers.EventsByPerformer
import com.district37.toastmasters.features.performers.PerformerDetailScreen
import com.district37.toastmasters.features.schedules.AgendaItemDetailScreen
import com.district37.toastmasters.features.schedules.create.CreateAgendaItemScreen
import com.district37.toastmasters.features.schedules.edit.EditAgendaItemScreen
import com.district37.toastmasters.features.schedules.manage.ManageAgendaItemsScreen
import com.district37.toastmasters.features.search.SearchScreen
import com.district37.toastmasters.features.users.ViewOtherUserProfileScreen
import com.district37.toastmasters.features.venues.EventsByVenue
import com.district37.toastmasters.features.venues.VenueDetailScreen
import com.district37.toastmasters.graphql.type.EventType
import com.district37.toastmasters.infra.ServerConfig
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Unified navigation graph containing all app destinations.
 * Consolidates ExploreNavGraph, CreateNavGraph, and AccountNavGraph into a single NavHost.
 *
 * This enables seamless cross-tab navigation without deeplink workarounds.
 */
@Composable
fun UnifiedNavGraph(
    navController: NavHostController,
    startDestination: Any = ExploreRoute,
    deeplinkHandler: DeeplinkHandler = koinInject(),
    profileTabNavigationState: ProfileTabNavigationState = koinInject()
) {
    val authViewModel: AuthViewModel = koinViewModel()
    val pendingDeeplink by deeplinkHandler.pendingDeeplink.collectAsState()

    // Process pending deeplinks
    LaunchedEffect(pendingDeeplink) {
        if (pendingDeeplink != null) {
            val navigator = DeeplinkNavigator(navController, deeplinkHandler, profileTabNavigationState)
            navigator.handlePendingDeeplink()
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ==================== EXPLORE TAB ROOT ====================
        composable<ExploreRoute> {
            ExploreScreen()
        }

        // ==================== CREATE TAB ROOT ====================
        composable<CreateHubRoute> { backStackEntry ->
            val shouldRefresh = backStackEntry.savedStateHandle.get<Boolean>("refresh") == true

            CreateHubScreen(
                shouldRefresh = shouldRefresh,
                onRefreshHandled = {
                    backStackEntry.savedStateHandle.remove<Boolean>("refresh")
                }
            )
        }

        // ==================== ACCOUNT TAB ROOT ====================
        composable<AccountHomeRoute> {
            AccountScreen(
                authViewModel = authViewModel,
                navController = navController
            )
        }

        // ==================== EXPLORE DESTINATIONS ====================
        composable<NotificationCenterRoute> {
            NotificationCenterScreen(
                onNotificationClick = { notification ->
                    notification.deeplink?.let { deeplink ->
                        deeplinkHandler.handleDeeplink(deeplink)
                    }
                }
            )
        }

        composable<SearchRoute> {
            SearchScreen(
                onBackClick = { navController.popBackStack() },
                onUserClick = { userId ->
                    navController.navigate(UserProfileNavigationArgs(userId))
                }
            )
        }

        composable<EventListRoute> {
            EventListScreen()
        }

        composable<AllEventsRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<AllEventsRoute>()
            val eventType = args.eventTypeName?.let { name ->
                EventType.knownEntries.find { it.rawValue == name }
            }
            AllEventsScreen(
                eventType = eventType,
                onBackClick = { navController.popBackStack() }
            )
        }

        // ==================== SHARED DETAIL DESTINATIONS ====================
        // These destinations are defined ONCE and accessible from any tab

        composable<EventDetailNavigationArgs> { backStackEntry ->
            val args = backStackEntry.toRoute<EventDetailNavigationArgs>()
            val shouldRefresh = backStackEntry.savedStateHandle.get<Boolean>("refresh") == true

            EventDetailScreen(
                eventId = args.eventId,
                onBackClick = { navController.popBackStack() },
                shouldRefresh = shouldRefresh,
                onRefreshHandled = {
                    backStackEntry.savedStateHandle.remove<Boolean>("refresh")
                }
            )
        }

        composable<VenueDetailNavigationArgs> { backStackEntry ->
            val args = backStackEntry.toRoute<VenueDetailNavigationArgs>()
            val shouldRefresh = backStackEntry.savedStateHandle.get<Boolean>("refresh") == true

            VenueDetailScreen(
                venueId = args.venueId,
                onBackClick = { navController.popBackStack() },
                shouldRefresh = shouldRefresh,
                onRefreshHandled = {
                    backStackEntry.savedStateHandle.remove<Boolean>("refresh")
                }
            )
        }

        composable<PerformerDetailNavigationArgs> { backStackEntry ->
            val args = backStackEntry.toRoute<PerformerDetailNavigationArgs>()
            val shouldRefresh = backStackEntry.savedStateHandle.get<Boolean>("refresh") == true

            PerformerDetailScreen(
                performerId = args.performerId,
                onBackClick = { navController.popBackStack() },
                shouldRefresh = shouldRefresh,
                onRefreshHandled = {
                    backStackEntry.savedStateHandle.remove<Boolean>("refresh")
                }
            )
        }

        composable<OrganizationDetailNavigationArgs> { backStackEntry ->
            val args = backStackEntry.toRoute<OrganizationDetailNavigationArgs>()
            val shouldRefresh = backStackEntry.savedStateHandle.get<Boolean>("refresh") == true

            OrganizationDetailScreen(
                organizationId = args.organizationId,
                onBackClick = { navController.popBackStack() },
                shouldRefresh = shouldRefresh,
                onRefreshHandled = {
                    backStackEntry.savedStateHandle.remove<Boolean>("refresh")
                }
            )
        }

        composable<AgendaItemDetailNavigationArgs> { backStackEntry ->
            val args = backStackEntry.toRoute<AgendaItemDetailNavigationArgs>()
            AgendaItemDetailScreen(
                agendaItemId = args.agendaItemId,
                showEvent = args.showEvent,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<LocationDetailNavigationArgs> { backStackEntry ->
            val args = backStackEntry.toRoute<LocationDetailNavigationArgs>()
            LocationDetailScreen(
                locationId = args.locationId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<UserProfileNavigationArgs> { backStackEntry ->
            val args = backStackEntry.toRoute<UserProfileNavigationArgs>()
            ViewOtherUserProfileScreen(
                userId = args.userId,
                onBackClick = { navController.popBackStack() }
            )
        }

        // ==================== LIST DESTINATIONS ====================

        composable<VenueEventsNavigationArgs> { backStackEntry ->
            val args = backStackEntry.toRoute<VenueEventsNavigationArgs>()
            EventsByVenue(
                venueId = args.venueId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<PerformerEventsNavigationArgs> { backStackEntry ->
            val args = backStackEntry.toRoute<PerformerEventsNavigationArgs>()
            EventsByPerformer(
                performerId = args.performerId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<OrganizationEventsNavigationArgs> { backStackEntry ->
            val args = backStackEntry.toRoute<OrganizationEventsNavigationArgs>()
            EventsByOrganization(
                organizationId = args.organizationId,
                onBackClick = { navController.popBackStack() }
            )
        }

        // ==================== EDIT DESTINATIONS ====================

        composable<EditEventRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<EditEventRoute>()
            // Determine which root to pop back to based on current tab context
            val currentTab = navController.currentTabContext()
            val popBackToRoute = when (currentTab) {
                TabContext.CREATE -> CreateHubRoute::class
                else -> ExploreRoute::class
            }

            EditEventScreen(
                eventId = args.eventId,
                onBackClick = { navController.popBackStack() },
                onEventUpdated = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
                    navController.popBackStack()
                },
                onEventDeleted = {
                    navController.popBackStack(popBackToRoute, inclusive = false)
                }
            )
        }

        composable<EditVenueRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<EditVenueRoute>()
            EditVenueScreen(
                venueId = args.venueId,
                onBackClick = { navController.popBackStack() },
                onVenueUpdated = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
                    navController.popBackStack()
                }
            )
        }

        composable<EditPerformerRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<EditPerformerRoute>()
            EditPerformerScreen(
                performerId = args.performerId,
                onBackClick = { navController.popBackStack() },
                onPerformerUpdated = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
                    navController.popBackStack()
                }
            )
        }

        composable<EditLocationRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<EditLocationRoute>()
            EditLocationScreen(
                locationId = args.locationId,
                onBackClick = { navController.popBackStack() },
                onLocationUpdated = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
                    navController.popBackStack()
                }
            )
        }

        composable<EditOrganizationRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<EditOrganizationRoute>()
            // Determine which root to pop back to based on current tab context
            val currentTab = navController.currentTabContext()
            val popBackToRoute = when (currentTab) {
                TabContext.CREATE -> CreateHubRoute::class
                else -> ExploreRoute::class
            }

            EditOrganizationScreen(
                organizationId = args.organizationId,
                onBackClick = { navController.popBackStack() },
                onOrganizationUpdated = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
                    navController.popBackStack()
                },
                onOrganizationDeleted = {
                    navController.popBackStack(popBackToRoute, inclusive = false)
                }
            )
        }

        // ==================== CREATE FLOW DESTINATIONS ====================

        composable<CreateEventRoute> { backStackEntry ->
            val createdVenueId = backStackEntry.savedStateHandle.get<Int>("created_venue_id")

            CreateEventWizardScreen(
                onEventCreated = { eventId ->
                    backStackEntry.savedStateHandle.remove<Int>("created_venue_id")
                    navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
                    navController.popBackStack()
                },
                onCancel = {
                    navController.popBackStack()
                },
                onCreateVenue = {
                    navController.navigate(CreateVenueRoute)
                },
                createdVenueId = createdVenueId
            )
        }

        composable<CreateVenueRoute> {
            CreateVenueWizardScreen(
                onVenueCreated = { venueId ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("created_venue_id", venueId)
                    navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() }
            )
        }

        composable<CreatePerformerRoute> {
            CreatePerformerWizardScreen(
                onPerformerCreated = { performerId ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("created_performer_id", performerId)
                    navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() }
            )
        }

        composable<CreateOrganizationRoute> {
            CreateOrganizationWizardScreen(
                onOrganizationCreated = { organizationId ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() }
            )
        }

        // ==================== VIEW ALL EDITABLE ENTITIES ====================

        composable<MyEditableEventsRoute> {
            MyEditableEventsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<MyEditableVenuesRoute> {
            MyEditableVenuesScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<MyEditablePerformersRoute> {
            MyEditablePerformersScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<ArchivedEventsRoute> {
            ArchivedEventsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // ==================== PICKER DESTINATIONS ====================

        composable<VenuePickerRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<VenuePickerRoute>()
            VenuePickerScreen(
                onVenueSelected = { venue ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("selected_venue_id", venue.id)
                    navController.previousBackStackEntry?.savedStateHandle?.set("selected_venue_name", venue.name)
                    navController.popBackStack()
                },
                onCreateNew = {
                    navController.navigate(CreateVenueRoute)
                },
                onDismiss = { navController.popBackStack() }
            )
        }

        composable<PerformerPickerRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<PerformerPickerRoute>()
            PerformerPickerScreen(
                onPerformerSelected = { performer ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("selected_performer_id", performer.id)
                    navController.previousBackStackEntry?.savedStateHandle?.set("selected_performer_name", performer.name)
                    navController.popBackStack()
                },
                onCreateNew = {
                    navController.navigate(CreatePerformerRoute)
                },
                onDismiss = { navController.popBackStack() }
            )
        }

        composable<LocationPickerRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<LocationPickerRoute>()
            LocationPickerScreen(
                venueId = args.venueId,
                onLocationSelected = { location ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("selected_location_id", location.id)
                    navController.previousBackStackEntry?.savedStateHandle?.set("selected_location_name", location.name)
                    navController.popBackStack()
                },
                onClearLocation = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("clear_location", true)
                },
                onDismiss = { navController.popBackStack() }
            )
        }

        // ==================== AGENDA ITEM MANAGEMENT ====================

        composable<ManageAgendaItemsRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<ManageAgendaItemsRoute>()
            val shouldRefresh = backStackEntry.savedStateHandle.get<Boolean>("refresh") == true

            ManageAgendaItemsScreen(
                eventId = args.eventId,
                onBackClick = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
                    navController.popBackStack()
                },
                onCreateAgendaItem = { navController.navigate(CreateAgendaItemRoute(args.eventId)) },
                onEditAgendaItem = { agendaItemId -> navController.navigate(EditAgendaItemRoute(agendaItemId)) },
                shouldRefresh = shouldRefresh,
                onRefreshHandled = {
                    backStackEntry.savedStateHandle.remove<Boolean>("refresh")
                }
            )
        }

        composable<CreateAgendaItemRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<CreateAgendaItemRoute>()
            val selectedPerformerId = backStackEntry.savedStateHandle.get<Int>("selected_performer_id")
            val selectedPerformerName = backStackEntry.savedStateHandle.get<String>("selected_performer_name")
            val selectedLocationId = backStackEntry.savedStateHandle.get<Int>("selected_location_id")
            val selectedLocationName = backStackEntry.savedStateHandle.get<String>("selected_location_name")
            val clearLocation = backStackEntry.savedStateHandle.get<Boolean>("clear_location") == true

            CreateAgendaItemScreen(
                eventId = args.eventId,
                onBackClick = { navController.popBackStack() },
                onAgendaItemCreated = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
                    navController.popBackStack()
                },
                onAddPerformer = {
                    navController.navigate(PerformerPickerRoute())
                },
                onSelectLocation = { venueId ->
                    navController.navigate(LocationPickerRoute(venueId = venueId))
                },
                selectedPerformerId = selectedPerformerId,
                selectedPerformerName = selectedPerformerName,
                selectedLocationId = selectedLocationId,
                selectedLocationName = selectedLocationName,
                clearLocation = clearLocation,
                onPerformerHandled = {
                    backStackEntry.savedStateHandle.remove<Int>("selected_performer_id")
                    backStackEntry.savedStateHandle.remove<String>("selected_performer_name")
                },
                onLocationHandled = {
                    backStackEntry.savedStateHandle.remove<Int>("selected_location_id")
                    backStackEntry.savedStateHandle.remove<String>("selected_location_name")
                },
                onClearLocationHandled = {
                    backStackEntry.savedStateHandle.remove<Boolean>("clear_location")
                }
            )
        }

        composable<EditAgendaItemRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<EditAgendaItemRoute>()
            val selectedPerformerId = backStackEntry.savedStateHandle.get<Int>("selected_performer_id")
            val selectedPerformerName = backStackEntry.savedStateHandle.get<String>("selected_performer_name")
            val selectedLocationId = backStackEntry.savedStateHandle.get<Int>("selected_location_id")
            val selectedLocationName = backStackEntry.savedStateHandle.get<String>("selected_location_name")
            val clearLocation = backStackEntry.savedStateHandle.get<Boolean>("clear_location") == true

            EditAgendaItemScreen(
                agendaItemId = args.agendaItemId,
                onBackClick = { navController.popBackStack() },
                onAgendaItemUpdated = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
                    navController.popBackStack()
                },
                onAddPerformer = {
                    navController.navigate(PerformerPickerRoute())
                },
                onSelectLocation = { venueId ->
                    navController.navigate(LocationPickerRoute(venueId = venueId))
                },
                selectedPerformerId = selectedPerformerId,
                selectedPerformerName = selectedPerformerName,
                selectedLocationId = selectedLocationId,
                selectedLocationName = selectedLocationName,
                clearLocation = clearLocation,
                onPerformerHandled = {
                    backStackEntry.savedStateHandle.remove<Int>("selected_performer_id")
                    backStackEntry.savedStateHandle.remove<String>("selected_performer_name")
                },
                onLocationHandled = {
                    backStackEntry.savedStateHandle.remove<Int>("selected_location_id")
                    backStackEntry.savedStateHandle.remove<String>("selected_location_name")
                },
                onClearLocationHandled = {
                    backStackEntry.savedStateHandle.remove<Boolean>("clear_location")
                }
            )
        }

        // ==================== COLLABORATOR MANAGEMENT ====================

        composable<ManageCollaboratorsRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<ManageCollaboratorsRoute>()
            ManageCollaboratorsScreen(
                entityType = args.entityType,
                entityId = args.entityId,
                entityName = args.entityName,
                onAddCollaborator = {
                    navController.navigate(
                        UserPickerRoute(
                            entityType = args.entityType,
                            entityId = args.entityId,
                            entityName = args.entityName
                        )
                    )
                },
                onDismiss = { navController.popBackStack() }
            )
        }

        composable<UserPickerRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<UserPickerRoute>()
            UserPickerScreen(
                entityType = args.entityType,
                entityId = args.entityId,
                entityName = args.entityName,
                onUserInvited = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
                    navController.popBackStack()
                },
                onDismiss = { navController.popBackStack() }
            )
        }

        // ==================== ORGANIZATION MEMBER MANAGEMENT ====================

        composable<ManageOrganizationMembersRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<ManageOrganizationMembersRoute>()
            val shouldRefresh = backStackEntry.savedStateHandle.get<Boolean>("refresh") == true

            ManageOrganizationMembersScreen(
                organizationId = args.organizationId,
                organizationName = args.organizationName,
                onBackClick = { navController.popBackStack() },
                onInviteMember = {
                    navController.navigate(
                        OrganizationMemberPickerRoute(
                            organizationId = args.organizationId,
                            organizationName = args.organizationName
                        )
                    )
                },
                shouldRefresh = shouldRefresh,
                onRefreshHandled = {
                    backStackEntry.savedStateHandle.remove<Boolean>("refresh")
                }
            )
        }

        composable<OrganizationMemberPickerRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<OrganizationMemberPickerRoute>()
            OrganizationMemberPickerScreen(
                organizationId = args.organizationId,
                organizationName = args.organizationName,
                onUserInvited = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("refresh", true)
                    navController.popBackStack()
                },
                onDismiss = { navController.popBackStack() }
            )
        }

        // ==================== ACCOUNT-SPECIFIC DESTINATIONS ====================

        composable<EditProfileRoute> {
            val authState by authViewModel.authState.collectAsState()

            when (val state = authState) {
                is AuthState.Authenticated -> {
                    val editProfileViewModel: EditProfileViewModel = koinViewModel { parametersOf(state.user) }
                    EditProfileScreen(
                        viewModel = editProfileViewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
                else -> {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }
        }

        composable<SettingsRoute> {
            val authState by authViewModel.authState.collectAsState()

            when (authState) {
                is AuthState.Authenticated -> {
                    val settingsViewModel: SettingsViewModel = koinViewModel()
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onNavigateToAppearance = {
                            navController.navigate(AppearanceSettingsRoute)
                        },
                        onNavigateToCalendar = {
                            navController.navigate(CalendarSettingsRoute)
                        }
                    )
                }
                else -> {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }
        }

        composable<AppearanceSettingsRoute> {
            val authState by authViewModel.authState.collectAsState()

            when (authState) {
                is AuthState.Authenticated -> {
                    val settingsViewModel: SettingsViewModel = koinViewModel()
                    AppearanceSettingsScreen(
                        viewModel = settingsViewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
                else -> {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }
        }

        composable<CalendarSettingsRoute> {
            val authState by authViewModel.authState.collectAsState()

            when (authState) {
                is AuthState.Authenticated -> {
                    val settingsViewModel: SettingsViewModel = koinViewModel()
                    CalendarSettingsScreen(
                        viewModel = settingsViewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onNavigateToCalendarSelection = {
                            navController.navigate(CalendarSelectionRoute)
                        },
                        onNavigateToSyncedItems = {
                            navController.navigate(SyncedItemsRoute)
                        }
                    )
                }
                else -> {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }
        }

        composable<CalendarSelectionRoute> {
            val authState by authViewModel.authState.collectAsState()

            when (authState) {
                is AuthState.Authenticated -> {
                    val settingsViewModel: SettingsViewModel = koinViewModel()
                    CalendarSelectionScreen(
                        viewModel = settingsViewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
                else -> {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }
        }

        composable<SyncedItemsRoute> {
            val authState by authViewModel.authState.collectAsState()

            when (authState) {
                is AuthState.Authenticated -> {
                    val settingsViewModel: SettingsViewModel = koinViewModel()
                    SyncedItemsScreen(
                        viewModel = settingsViewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onNavigateToEvent = { eventId ->
                            navController.navigate(EventDetailNavigationArgs(eventId))
                        },
                        onNavigateToAgendaItem = { agendaItemId ->
                            navController.navigate(AgendaItemDetailNavigationArgs(agendaItemId))
                        }
                    )
                }
                else -> {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }
        }

        composable<SubscribedEventsRoute> {
            val authState by authViewModel.authState.collectAsState()

            when (authState) {
                is AuthState.Authenticated -> {
                    val savedEventsViewModel: SavedEventsViewModel = koinViewModel()
                    SavedEventsScreen(
                        viewModel = savedEventsViewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
                else -> {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }
        }

        composable<AttendingEventsRoute> {
            val authState by authViewModel.authState.collectAsState()

            when (authState) {
                is AuthState.Authenticated -> {
                    val attendingEventsViewModel: AttendingEventsViewModel = koinViewModel()
                    AttendingEventsScreen(
                        viewModel = attendingEventsViewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
                else -> {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }
        }

        composable<FriendsListRoute> {
            val authState by authViewModel.authState.collectAsState()

            when (authState) {
                is AuthState.Authenticated -> {
                    val friendsListViewModel: FriendsListViewModel = koinViewModel()
                    FriendsListScreen(
                        viewModel = friendsListViewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onFriendClick = { friend ->
                            navController.navigate(UserProfileNavigationArgs(friend.id))
                        }
                    )
                }
                else -> {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }
        }

        composable<DeveloperRoute> {
            DeveloperScreen()
        }

        // ==================== MESSAGING DESTINATIONS ====================

        composable<ConversationsRoute> {
            val authState by authViewModel.authState.collectAsState()

            when (authState) {
                is AuthState.Authenticated -> {
                    ConversationsScreen()
                }
                else -> {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }
        }

        composable<NewConversationRoute> {
            val authState by authViewModel.authState.collectAsState()

            when (authState) {
                is AuthState.Authenticated -> {
                    NewConversationScreen()
                }
                else -> {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }
        }

        composable<ChatRoute> { backStackEntry ->
            val authState by authViewModel.authState.collectAsState()
            val args = backStackEntry.toRoute<ChatRoute>()

            when (authState) {
                is AuthState.Authenticated -> {
                    ChatScreen(
                        conversationId = args.conversationId,
                        initialDisplayName = args.displayName,
                        initialAvatarUrl = args.avatarUrl
                    )
                }
                else -> {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }
        }
    }
}

/**
 * Account home screen that shows login or user profile based on auth state.
 */
@Composable
private fun AccountScreen(
    authViewModel: AuthViewModel,
    navController: NavHostController
) {
    // Configure the root TopAppBar for this screen
    ConfigureTopAppBar(AppBarConfigs.rootScreen())

    val authState by authViewModel.authState.collectAsState()

    when (val state = authState) {
        is AuthState.Loading -> {
            Surface(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        is AuthState.Unauthenticated -> {
            LoginScreen(
                authViewModel = authViewModel,
                onLongPressLogo = {
                    if (ServerConfig.isDebugBuild) {
                        navController.navigate(DeveloperRoute)
                    }
                }
            )
        }
        is AuthState.Authenticated -> {
            val userProfileViewModel: UserProfileViewModel = koinViewModel()
            UserProfileScreen(
                user = state.user,
                profileViewModel = userProfileViewModel,
                onEditProfile = { navController.navigate(EditProfileRoute) },
                onSettings = { navController.navigate(SettingsRoute) },
                onMessages = { navController.navigate(ConversationsRoute) },
                onViewAllSubscribedEvents = { navController.navigate(SubscribedEventsRoute) },
                onViewAllAttendingEvents = { navController.navigate(AttendingEventsRoute) },
                onViewAllFriends = { navController.navigate(FriendsListRoute) },
                onLongPressAvatar = {
                    if (ServerConfig.isDebugBuild) {
                        navController.navigate(DeveloperRoute)
                    }
                },
                onViewUserProfile = { userId ->
                    navController.navigate(UserProfileNavigationArgs(userId))
                }
            )
        }
    }
}
