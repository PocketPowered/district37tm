package com.district37.toastmasters.devsettings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Badge
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.navigation.StatefulScaffold
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun DevSettingsScreen() {
    val viewModel: DevSettingsViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    StatefulScaffold(
        title = "Dev Settings",
        resource = state,
        onRefresh = { viewModel.onRefresh() }
    ) { devState ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Text(
                    text = "Conference Override",
                    style = MaterialTheme.typography.h6
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select a conference to override the production active conference. Only affects this device.",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (devState.overrideId != null) {
                    OutlinedButton(
                        onClick = { viewModel.clearOverride() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear Override (Use Production Active)")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            items(devState.conferences, key = { it.id }) { conference ->
                ConferenceRow(
                    conference = conference,
                    isOverride = devState.overrideId == conference.id,
                    onSelect = {
                        if (devState.overrideId == conference.id) {
                            viewModel.clearOverride()
                        } else {
                            viewModel.setOverride(conference.id)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ConferenceRow(
    conference: ConferenceOption,
    isOverride: Boolean,
    onSelect: () -> Unit
) {
    val borderColor = when {
        isOverride -> MaterialTheme.colors.secondary
        else -> MaterialTheme.colors.surface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = if (isOverride) 4.dp else 2.dp,
        onClick = onSelect,
        backgroundColor = if (isOverride) MaterialTheme.colors.secondary.copy(alpha = 0.08f)
        else MaterialTheme.colors.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = conference.name,
                    style = MaterialTheme.typography.subtitle1,
                    modifier = Modifier.weight(1f)
                )
                Row {
                    if (conference.isProductionActive) {
                        Badge(
                            backgroundColor = MaterialTheme.colors.primary
                        ) {
                            Text(
                                text = "PROD",
                                style = MaterialTheme.typography.overline,
                                color = MaterialTheme.colors.onPrimary,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    if (isOverride) {
                        Badge(
                            backgroundColor = MaterialTheme.colors.secondary
                        ) {
                            Text(
                                text = "OVERRIDE",
                                style = MaterialTheme.typography.overline,
                                color = MaterialTheme.colors.onSecondary,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
            }
            if (conference.startDate != null || conference.endDate != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = listOfNotNull(conference.startDate, conference.endDate).joinToString(" – "),
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}
