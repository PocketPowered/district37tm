package com.district37.toastmasters.eventdetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.components.EventImage
import com.district37.toastmasters.models.toHumanReadableString
import com.district37.toastmasters.navigation.StatefulScaffold
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun EventDetailsScreen(eventId: Int, modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<EventDetailsViewModel>()
    LaunchedEffect(eventId) {
        viewModel.screenStateSlice.initialize(eventId)
    }
    val screenState by viewModel.screenStateSlice.screenState.collectAsState()
    StatefulScaffold(resource = screenState) {
        LazyColumn(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // TODO transform this into a carousel
                    it.event.images?.firstOrNull()?.let { firstImage ->
                        EventImage(
                            url = firstImage,
                            modifier = Modifier.height(400.dp).fillMaxWidth()
                        )
                    }
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp),
                    ) {
                        Text(text = it.event.title, fontWeight = FontWeight.Bold)
                        // Display location info with enhanced styling if we have a matching location
                        if (it.location != null) {
                            Text(
                                text = it.location.locationName,
                                fontWeight = FontWeight.Medium
                            )
                            if (it.location.locationImages.isNotEmpty()) {
                                // TODO: Add location images carousel
                            }
                        } else {
                            Text(text = it.event.locationInfo)
                        }
                        Text(text = it.event.time.toHumanReadableString())
                        Spacer(modifier = Modifier.height(4.dp))
                        it.event.agenda.forEach { agendaItem ->
                            Text(text = agendaItem.title)
                            Text(text = agendaItem.description)
                            Text(text = agendaItem.locationInfo)
                            Text(text = agendaItem.time.toHumanReadableString(showDate = false))
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
            item {
                Text(text = it.event.description, modifier = Modifier.padding(horizontal = 8.dp))
            }
        }
    }
}