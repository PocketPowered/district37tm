package com.district37.toastmasters.di

import com.district37.toastmasters.auth.AuthInterceptor
import com.district37.toastmasters.auth.AuthViewModel
import com.district37.toastmasters.auth.data.AuthApiClient
import com.district37.toastmasters.auth.data.AuthRepository
import com.district37.toastmasters.auth.data.OtpAuthRepository
import com.district37.toastmasters.infra.ApolloClientConfig
import com.district37.toastmasters.infra.ServerConfig
import com.district37.toastmasters.data.repository.EventRepository
import com.district37.toastmasters.data.repository.VenueRepository
import com.district37.toastmasters.data.repository.PerformerRepository
import com.district37.toastmasters.data.repository.LocationRepository
import com.district37.toastmasters.data.repository.AgendaItemRepository
import com.district37.toastmasters.data.repository.UserEngagementRepository
import com.district37.toastmasters.data.repository.CreateHubRepository
import com.district37.toastmasters.data.repository.ImageRepository
import com.district37.toastmasters.data.repository.ImageUploadRepository
import com.district37.toastmasters.data.repository.SearchRepository
import com.district37.toastmasters.util.WizardImageHandler
import com.district37.toastmasters.data.repository.UserProfileRepository
import com.district37.toastmasters.data.repository.ActivityFeedRepository
import com.district37.toastmasters.data.repository.FriendsRepository
import com.district37.toastmasters.data.repository.FriendRsvpRepository
import com.district37.toastmasters.data.repository.CollaboratorsRepository
import com.district37.toastmasters.data.repository.MessagingRepository
import com.district37.toastmasters.data.repository.NotificationRepository
import com.district37.toastmasters.data.repository.OrganizationRepository
import com.district37.toastmasters.data.repository.OrganizationMemberInvitationRepository
import com.district37.toastmasters.data.repository.AgendaItemSyncRepository
import com.district37.toastmasters.data.repository.EventSyncRepository
import com.district37.toastmasters.features.account.AttendingEventsViewModel
import com.district37.toastmasters.features.account.EditProfileViewModel
import com.district37.toastmasters.features.account.FriendsListViewModel
import com.district37.toastmasters.features.account.SavedEventsViewModel
import com.district37.toastmasters.features.account.SettingsViewModel
import com.district37.toastmasters.features.account.UserProfileViewModel
import com.district37.toastmasters.features.onboarding.OnboardingWizardViewModel
import com.district37.toastmasters.models.User
import com.district37.toastmasters.components.events.EventsPagedDataSource
import com.district37.toastmasters.engagement.EngagementManager
import com.district37.toastmasters.features.explore.ExploreViewModel
import com.district37.toastmasters.features.events.EventListViewModel
import com.district37.toastmasters.features.events.EventDetailViewModel
import com.district37.toastmasters.features.venues.VenueDetailViewModel
import com.district37.toastmasters.features.venues.VenuePreviewViewModel
import com.district37.toastmasters.features.schedules.AgendaItemDetailViewModel
import com.district37.toastmasters.features.performers.PerformerPreviewViewModel
import com.district37.toastmasters.features.performers.PerformerDetailViewModel
import com.district37.toastmasters.features.locations.LocationPreviewViewModel
import com.district37.toastmasters.features.locations.LocationDetailViewModel
import com.district37.toastmasters.features.organizations.OrganizationDetailViewModel
import com.district37.toastmasters.features.create.event.CreateEventViewModel
import com.district37.toastmasters.features.create.event.CreateEventWizardViewModel
import com.district37.toastmasters.features.edit.event.EditEventViewModel
import com.district37.toastmasters.features.edit.location.EditLocationViewModel
import com.district37.toastmasters.features.edit.performer.EditPerformerViewModel
import com.district37.toastmasters.features.edit.venue.EditVenueViewModel
import com.district37.toastmasters.features.edit.organization.EditOrganizationViewModel
import com.district37.toastmasters.features.organizations.members.ManageOrganizationMembersViewModel
import com.district37.toastmasters.features.organizations.members.OrganizationMemberPickerViewModel
import com.district37.toastmasters.features.schedules.manage.ManageAgendaItemsViewModel
import com.district37.toastmasters.features.schedules.create.CreateAgendaItemViewModel
import com.district37.toastmasters.features.schedules.edit.EditAgendaItemViewModel
import com.district37.toastmasters.features.create.venue.CreateVenueViewModel
import com.district37.toastmasters.features.create.venue.CreateVenueWizardViewModel
import com.district37.toastmasters.features.create.performer.CreatePerformerViewModel
import com.district37.toastmasters.features.create.performer.CreatePerformerWizardViewModel
import com.district37.toastmasters.features.create.organization.CreateOrganizationWizardViewModel
import com.district37.toastmasters.features.create.picker.VenuePickerViewModel
import com.district37.toastmasters.features.create.picker.PerformerPickerViewModel
import com.district37.toastmasters.features.create.picker.LocationPickerViewModel
import com.district37.toastmasters.features.create.CreateHubViewModel
import com.district37.toastmasters.features.search.SearchViewModel
import com.district37.toastmasters.features.users.ViewOtherUserProfileViewModel
import com.district37.toastmasters.features.collaborators.picker.UserPickerViewModel
import com.district37.toastmasters.features.collaborators.manage.ManageCollaboratorsViewModel
import com.district37.toastmasters.features.messaging.ChatViewModel
import com.district37.toastmasters.features.messaging.ConversationsViewModel
import com.district37.toastmasters.features.messaging.NewConversationViewModel
import com.district37.toastmasters.messaging.UnreadMessagesManager
import com.district37.toastmasters.features.notifications.NotificationViewModel
import com.district37.toastmasters.navigation.DeeplinkHandler
import com.district37.toastmasters.navigation.ProfileTabNavigationState
import com.district37.toastmasters.infra.UserPreferencesManager
import com.district37.toastmasters.infra.createUserPreferencesManager
import com.district37.toastmasters.infra.EffectiveServerUrlProvider
import com.district37.toastmasters.infra.DeveloperSettingsManager
import com.district37.toastmasters.infra.location.GeolocationService
import com.district37.toastmasters.infra.location.createGeolocationService
import com.district37.toastmasters.infra.calendar.CalendarService
import com.district37.toastmasters.infra.calendar.CalendarSyncManager
import com.district37.toastmasters.infra.calendar.CalendarReconciliationService
import com.district37.toastmasters.infra.calendar.CalendarAutoSyncService
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/**
 * Koin DI module for the app
 */
val appModule = module {
    // Effective Server URL Provider - determines which server URL to use based on dev settings
    // Must be created early as it's used by multiple network clients
    single {
        val developerSettingsManager = get<DeveloperSettingsManager>()
        val useLocalhost = kotlinx.coroutines.runBlocking {
            developerSettingsManager.getUseLocalhostServer()
        }
        EffectiveServerUrlProvider(useLocalhost)
    }

    // Auth API Client - now uses EffectiveServerUrlProvider for base URL
    single { AuthApiClient(get()) }

    // Auth Repository - depends on AuthApiClient, TokenManager, and PushNotificationService (from platformAuthModule)
    // Uses lazy provider for NotificationRepository to avoid circular dependency
    single { AuthRepository(get(), get(), get(), { get<NotificationRepository>() }) }

    // OTP Auth Repository - handles email and phone OTP authentication
    singleOf(::OtpAuthRepository)

    // Auth Interceptor - depends on AuthRepository
    single { AuthInterceptor(get()) }

    // Apollo Client - uses EffectiveServerUrlProvider for server URL
    // In debug builds: can optionally use localhost server (requires app restart)
    // In release builds: connects directly to production
    // Includes auth interceptor for authenticated GraphQL requests
    // Includes token provider for WebSocket subscriptions
    single {
        val authRepository = get<AuthRepository>()
        val serverUrlProvider = get<EffectiveServerUrlProvider>()
        kotlinx.coroutines.runBlocking {
            ApolloClientConfig.createApolloClient(
                primaryUrl = serverUrlProvider.serverUrl,
                fallbackUrl = ServerConfig.fallbackUrl,
                enableFallback = ServerConfig.enableFallback,
                authInterceptor = get<AuthInterceptor>(),
                tokenProvider = { authRepository.getValidAccessToken() }
            )
        }
    }

    // Repositories
    singleOf(::EventRepository)
    singleOf(::VenueRepository)
    singleOf(::PerformerRepository)
    singleOf(::LocationRepository)
    singleOf(::AgendaItemRepository)
    singleOf(::UserEngagementRepository)
    singleOf(::CreateHubRepository)
    singleOf(::ImageRepository)
    single { ImageUploadRepository(get(), get()) }
    single { WizardImageHandler(get(), get()) }
    singleOf(::SearchRepository)
    singleOf(::UserProfileRepository)
    singleOf(::ActivityFeedRepository)
    singleOf(::FriendsRepository)
    singleOf(::FriendRsvpRepository)
    singleOf(::CollaboratorsRepository)
    singleOf(::OrganizationRepository)
    singleOf(::OrganizationMemberInvitationRepository)
    singleOf(::MessagingRepository)
    singleOf(::NotificationRepository)
    singleOf(::AgendaItemSyncRepository)
    singleOf(::EventSyncRepository)

    // Engagement Manager - singleton for centralized engagement state management
    single { EngagementManager(get(), get()) }

    // Unread Messages Manager - singleton for tracking unread message state
    single { UnreadMessagesManager(get()) }

    // Deeplink Handler - singleton for managing deeplink state
    singleOf(::DeeplinkHandler)

    // Profile Tab Navigation State - singleton for navigating to specific profile tab from deeplinks
    singleOf(::ProfileTabNavigationState)

    // User Preferences Manager - singleton for persisting user preferences
    single<UserPreferencesManager> { createUserPreferencesManager() }

    // Geolocation Service - singleton for device location access
    single<GeolocationService> { createGeolocationService() }

    // Calendar Sync Manager - orchestrates calendar sync between device and server
    single { CalendarSyncManager(get(), get(), get()) }

    // Calendar Reconciliation Service - backup sync mechanism that runs on app open
    // Also handles smart resync after app reinstall
    single { CalendarReconciliationService(get(), get(), get(), get(), get(), get()) }

    // Calendar Auto-Sync Service - listens to engagement updates and auto-syncs to calendar
    single { CalendarAutoSyncService(get(), get(), get(), get(), get()) }

    // Pagination Data Sources
    singleOf(::EventsPagedDataSource)

    // Auth ViewModel - singleton to prevent repeated auth state initialization on navigation
    // Also handles device token registration after login and OTP authentication
    single { AuthViewModel(get(), get(), get(), get(), get(), get(), get()) }

    // ViewModels - use viewModel DSL for Compose integration
    viewModel { ExploreViewModel(get(), get(), get()) }
    viewModel { EventListViewModel(get()) }
    viewModel { (eventId: Int) -> EventDetailViewModel(get(), get(), get(), get(), get(), get(), get(), eventId) }
    viewModel { (venueId: Int) -> VenueDetailViewModel(get(), get(), get(), venueId) }
    viewModel { (venueId: Int) -> VenuePreviewViewModel(get(), venueId) }
    viewModel { (agendaItemId: Int) -> AgendaItemDetailViewModel(get(), get(), get(), get(), get(), get(), agendaItemId) }
    viewModel { (performerId: Int) -> PerformerPreviewViewModel(get(), performerId) }
    viewModel { (performerId: Int) -> PerformerDetailViewModel(get(), get(), get(), performerId) }
    viewModel { (locationId: Int) -> LocationPreviewViewModel(get(), locationId) }
    viewModel { (locationId: Int) -> LocationDetailViewModel(get(), locationId) }
    viewModel { (organizationId: Int) -> OrganizationDetailViewModel(get(), get(), get(), organizationId) }

    // Create feature ViewModels
    viewModel { CreateEventViewModel(get(), get()) }

    // Edit feature ViewModels
    viewModel { (eventId: Int) -> EditEventViewModel(get(), get(), get(), get(), eventId) }
    viewModel { (venueId: Int) -> EditVenueViewModel(get(), get(), get(), get(), venueId) }
    viewModel { (performerId: Int) -> EditPerformerViewModel(get(), get(), get(), performerId) }
    viewModel { (locationId: Int) -> EditLocationViewModel(get(), get(), get(), locationId) }
    viewModel { (organizationId: Int) -> EditOrganizationViewModel(get(), get(), get(), organizationId) }
    viewModel { (organizationId: Int) -> ManageOrganizationMembersViewModel(get(), organizationId) }
    viewModel { (organizationId: Int) -> OrganizationMemberPickerViewModel(get(), get(), get(), organizationId) }

    // Agenda item management ViewModels
    viewModel { (eventId: Int) -> ManageAgendaItemsViewModel(get(), get(), eventId) }
    viewModel { (eventId: Int) -> CreateAgendaItemViewModel(get(), get(), eventId) }
    viewModel { (agendaItemId: Int) -> EditAgendaItemViewModel(get(), agendaItemId) }

    viewModel { CreateEventWizardViewModel(get(), get(), get()) }
    viewModel { CreateVenueViewModel(get()) }
    viewModel { CreateVenueWizardViewModel(get(), get()) }
    viewModel { CreatePerformerViewModel(get()) }
    viewModel { CreatePerformerWizardViewModel(get(), get()) }
    viewModel { CreateOrganizationWizardViewModel(get(), get()) }
    viewModel { VenuePickerViewModel(get()) }
    viewModel { PerformerPickerViewModel(get()) }
    viewModel { (venueId: Int?) -> LocationPickerViewModel(get(), venueId) }
    viewModel { CreateHubViewModel(get()) }

    // Search
    viewModel { SearchViewModel(get()) }

    // Account/Profile
    viewModel { UserProfileViewModel(get(), get(), get(), get(), get()) }
    viewModel { (user: User) -> EditProfileViewModel(get(), get(), get(), user) }
    viewModel { SettingsViewModel(get(), get(), get(), get(), get()) }
    viewModel { OnboardingWizardViewModel(get(), get()) }
    viewModel { SavedEventsViewModel(get()) }
    viewModel { AttendingEventsViewModel(get()) }
    viewModel { FriendsListViewModel(get()) }

    // Other User Profile
    viewModel { (userId: String) -> ViewOtherUserProfileViewModel(get(), get(), userId) }

    // Collaborators
    viewModel { (entityType: String, entityId: Int) -> UserPickerViewModel(get(), get(), get(), get(), entityType, entityId) }
    viewModel { (entityType: String, entityId: Int) -> ManageCollaboratorsViewModel(get(), entityType, entityId) }

    // Messaging
    viewModel { ConversationsViewModel(get(), get(), get()) }
    viewModel { NewConversationViewModel(get(), get(), get(), get()) }
    viewModel { (conversationId: Int) -> ChatViewModel(get(), get(), get(), get(), conversationId) }

    // Notifications - singleton to persist unread count across navigation
    single { NotificationViewModel(get(), get(), get()) }
}
