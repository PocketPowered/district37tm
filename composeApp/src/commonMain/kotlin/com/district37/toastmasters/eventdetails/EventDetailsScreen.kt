package com.district37.toastmasters.eventdetails

import Linkout
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.components.ImageCarousel
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Event Header with Images
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (it.event.images?.isNotEmpty() == true) {
                        ImageCarousel(
                            images = it.event.images,
                            height = 400
                        )
                    }
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        elevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = it.event.title,
                                style = MaterialTheme.typography.h5,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Date",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = it.event.time.toHumanReadableString(),
                                    style = MaterialTheme.typography.body1
                                )
                            }
                            
                            if (it.event.locationInfo.isNotEmpty()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Location",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = it.event.locationInfo,
                                        style = MaterialTheme.typography.body1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Event Description
            if (it.event.description.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        elevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "About",
                                style = MaterialTheme.typography.h6,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = it.event.description,
                                style = MaterialTheme.typography.body1
                            )
                        }
                    }
                }
            }

            // Agenda
            if (it.event.agenda.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        elevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Agenda",
                                style = MaterialTheme.typography.h6,
                                fontWeight = FontWeight.Bold
                            )
                            
                            it.event.agenda.forEachIndexed { index, agendaItem ->
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = agendaItem.title,
                                        style = MaterialTheme.typography.subtitle1,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = agendaItem.time.toHumanReadableString(showDate = false),
                                        style = MaterialTheme.typography.caption
                                    )
                                    if (agendaItem.description.isNotEmpty()) {
                                        Text(
                                            text = agendaItem.description,
                                            style = MaterialTheme.typography.body2
                                        )
                                    }
                                    if (agendaItem.locationInfo.isNotEmpty()) {
                                        Text(
                                            text = agendaItem.locationInfo,
                                            style = MaterialTheme.typography.caption
                                        )
                                    }
                                    if (index < it.event.agenda.size - 1) {
                                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Additional Links
            if (it.event.additionalLinks.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        elevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Additional Resources",
                                style = MaterialTheme.typography.h6,
                                fontWeight = FontWeight.Bold
                            )
                            it.event.additionalLinks.forEach { link ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Linkout,
                                        contentDescription = "Link",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = link.displayName,
                                        style = MaterialTheme.typography.body1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Location Details
            if (it.location != null && (it.location.locationName.isNotEmpty() || it.location.locationImages.isNotEmpty())) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        elevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Location Details",
                                style = MaterialTheme.typography.h6,
                                fontWeight = FontWeight.Bold
                            )
                            if (it.location.locationName.isNotEmpty()) {
                                Text(
                                    text = it.location.locationName,
                                    style = MaterialTheme.typography.body1
                                )
                            }
                            if (it.location.locationImages.isNotEmpty()) {
                                ImageCarousel(
                                    images = it.location.locationImages,
                                    height = 300
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}