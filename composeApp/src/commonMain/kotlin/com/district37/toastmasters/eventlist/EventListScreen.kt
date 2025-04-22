package com.district37.toastmasters.eventlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.LocalAppViewModel
import com.district37.toastmasters.models.EventPreview
import com.district37.toastmasters.navigation.EVENT_ID_ARG
import com.district37.toastmasters.navigation.NavigationItemKey
import com.district37.toastmasters.navigation.StatefulScaffold
import com.district37.toastmasters.notifications.NotificationsEntry
import com.wongislandd.nexus.navigation.LocalNavHostController
import com.wongislandd.nexus.util.Resource
import com.wongislandd.nexus.util.conditionallyChain
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun EventListScreen() {
    val navController = LocalNavHostController.current
    val appViewModel = LocalAppViewModel.current
    val viewModel = koinViewModel<EventListViewModel>()
    val coroutineScope = rememberCoroutineScope()
    val screenState by viewModel.screenStateSlice.screenState.collectAsState()
    val isRefreshing = screenState is Resource.Loading
    StatefulScaffold(
        actions = {
            IconButton({
                appViewModel.navigate(
                    navController,
                    NavigationItemKey.INFO
                )
            }) {
                Icon(Icons.Default.Info, contentDescription = "Info")
            }
            NotificationsEntry()
        },
        onRefresh = {
            viewModel.uiEventBus.sendEvent(
                coroutineScope, RefreshTriggered
            )
        },
        isRefreshing = isRefreshing,
        resource = screenState
    ) { data ->
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                data.availableTabs.forEach { tab ->
                    item {
                        Tab(
                            text = {
                                Text(
                                    tab.displayName,
                                    modifier = Modifier.padding(horizontal = 36.dp)
                                )
                            },
                            selected = tab.isSelected,
                            onClick = {
                                viewModel.uiEventBus.sendEvent(
                                    coroutineScope,
                                    TabChanged(tab)
                                )
                            },
                            modifier = Modifier.conditionallyChain(
                                tab.isSelected, Modifier.background(MaterialTheme.colors.secondary)
                            )
                        )
                    }
                }
            }
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp).fillMaxSize()
                    .background(MaterialTheme.colors.background)
            ) {
                items(data.events) { event ->
                    EventCard(event, onCardClick = {
                        coroutineScope.launch {
                            appViewModel.navigate(
                                navController,
                                NavigationItemKey.EVENT_DETAILS,
                                mapOf(EVENT_ID_ARG to event.id)
                            )
                        }
                    })
                }
            }
        }

    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun EventCard(
    eventPreview: EventPreview,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        backgroundColor = MaterialTheme.colors.secondary.copy(alpha = .7f),
        modifier = modifier.padding(horizontal = 8.dp).height(
            height = 200.dp
        ),
        onClick = onCardClick
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, start = 16.dp)
                    .align(Alignment.CenterStart)
            ) {
                Text(
                    text = eventPreview.title,
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2
                )
                Text(
                    text = eventPreview.locationInfo,
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface,
                )
                Text(
                    text = eventPreview.time,
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface,
                )
            }
        }

    }

}
