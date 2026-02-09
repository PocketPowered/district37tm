package com.district37.toastmasters.components.events

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.district37.toastmasters.LocalNavController
import com.district37.toastmasters.models.Event
import com.district37.toastmasters.navigation.EventDetailNavigationArgs

/**
 * Event preview card component that displays pre-fetched event data.
 *
 * @param event The event to display
 * @param modifier Optional modifier for the card
 */
@Composable
fun EventPreviewCard(
    event: Event,
    modifier: Modifier = Modifier
) {
    val navController = LocalNavController.current
    EventCard(
        event = event,
        onClick = { navController.navigate(EventDetailNavigationArgs(event.id)) },
        modifier = modifier
    )
}
