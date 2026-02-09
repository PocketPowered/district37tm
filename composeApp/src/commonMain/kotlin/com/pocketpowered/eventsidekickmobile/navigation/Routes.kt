package com.district37.toastmasters.navigation

import kotlinx.serialization.Serializable

// Explore Tab Routes
@Serializable
object ExploreRoute

@Serializable
object SearchRoute

@Serializable
object EventListRoute

@Serializable
data class AllEventsRoute(val eventTypeName: String? = null)

@Serializable
data class EventDetailNavigationArgs(val eventId: Int)

@Serializable
data class VenueDetailNavigationArgs(val venueId: Int)

@Serializable
data class VenueEventsNavigationArgs(val venueId: Int)

@Serializable
data class AgendaItemDetailNavigationArgs(val agendaItemId: Int, val showEvent: Boolean = true)

@Serializable
data class PerformerDetailNavigationArgs(val performerId: Int)

@Serializable
data class PerformerEventsNavigationArgs(val performerId: Int)

@Serializable
data class LocationDetailNavigationArgs(val locationId: Int)

@Serializable
data class OrganizationDetailNavigationArgs(val organizationId: Int)

@Serializable
data class OrganizationEventsNavigationArgs(val organizationId: Int)

@Serializable
data class UserProfileNavigationArgs(val userId: String)

// Account Tab Routes
@Serializable
object AccountHomeRoute

@Serializable
object EditProfileRoute

@Serializable
object SettingsRoute

@Serializable
object AppearanceSettingsRoute

@Serializable
object CalendarSettingsRoute

@Serializable
object CalendarSelectionRoute

@Serializable
object SyncedItemsRoute

@Serializable
object SubscribedEventsRoute

@Serializable
object AttendingEventsRoute

@Serializable
object FriendsListRoute

@Serializable
object DeveloperRoute

// Create Tab Routes
@Serializable
object CreateHubRoute

@Serializable
object CreateEventRoute

@Serializable
object CreateVenueRoute

@Serializable
object CreatePerformerRoute

@Serializable
object CreateOrganizationRoute

// View All Editable Entities Routes
@Serializable
object MyEditableEventsRoute

@Serializable
object MyEditableVenuesRoute

@Serializable
object MyEditablePerformersRoute

@Serializable
object ArchivedEventsRoute

@Serializable
data class VenuePickerRoute(val returnToRoute: String? = null)

@Serializable
data class PerformerPickerRoute(val returnToRoute: String? = null)

@Serializable
data class LocationPickerRoute(val venueId: Int?, val returnToRoute: String? = null)

// Edit/Manage Routes
@Serializable
data class EditEventRoute(val eventId: Int)

@Serializable
data class EditVenueRoute(val venueId: Int)

@Serializable
data class EditPerformerRoute(val performerId: Int)

@Serializable
data class EditLocationRoute(val locationId: Int)

@Serializable
data class EditOrganizationRoute(val organizationId: Int)

@Serializable
data class ManageAgendaItemsRoute(val eventId: Int)

@Serializable
data class CreateAgendaItemRoute(val eventId: Int)

@Serializable
data class EditAgendaItemRoute(val agendaItemId: Int)

// Collaborator Management Routes
@Serializable
data class UserPickerRoute(val entityType: String, val entityId: Int, val entityName: String)

@Serializable
data class ManageCollaboratorsRoute(val entityType: String, val entityId: Int, val entityName: String)

// Organization Member Management Routes
@Serializable
data class ManageOrganizationMembersRoute(val organizationId: Int, val organizationName: String)

@Serializable
data class OrganizationMemberPickerRoute(val organizationId: Int, val organizationName: String)

// Notification Routes
@Serializable
object NotificationCenterRoute

// Messaging Routes
@Serializable
object ConversationsRoute

@Serializable
object NewConversationRoute

@Serializable
data class ChatRoute(
    val conversationId: Int,
    val displayName: String? = null,
    val avatarUrl: String? = null
)
