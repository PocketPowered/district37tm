package com.district37.toastmasters.components.events

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.district37.toastmasters.LocalNavController
import com.district37.toastmasters.models.Event
import com.district37.toastmasters.navigation.EventDetailNavigationArgs

/**
 * Minimal event preview card that displays pre-fetched event data
 * and handles navigation to event detail screen.
 *
 * @param event The event to display
 * @param modifier Optional modifier for the card
 */
@Composable
fun MinimalEventPreviewCard(
    event: Event,
    modifier: Modifier = Modifier
) {
    val navController = LocalNavController.current
    MinimalEventCard(
        event = event,
        onClick = { navController.navigate(EventDetailNavigationArgs(event.id)) },
        modifier = modifier
    )
}
