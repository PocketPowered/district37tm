package com.district37.toastmasters.favorites

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.LocalAppViewModel
import com.district37.toastmasters.eventlist.EventCard
import com.district37.toastmasters.navigation.NavigationItemKey
import com.district37.toastmasters.navigation.StatefulScaffold
import com.wongislandd.nexus.navigation.LocalNavHostController
import com.wongislandd.nexus.util.Resource
import kotlinx.coroutines.launch

@Composable
fun FavoritedEventsList() {
    val navController = LocalNavHostController.current
    val appViewModel = LocalAppViewModel.current
    val coroutineScope = rememberCoroutineScope()
    val screenState by appViewModel.favoritesSlice.screenState.collectAsState()
    val isRefreshing = screenState is Resource.Loading

    StatefulScaffold(
        onRefresh = {

        },
        isRefreshing = isRefreshing,
        resource = screenState
    ) { favoritedEvents ->
        if (favoritedEvents.isEmpty()) {
            Text(
                text = "No favorited events",
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(favoritedEvents.size) { idx ->
                    val eventPreview = favoritedEvents[idx]
                    EventCard(
                        eventPreview = eventPreview,
                        onCardClick = {
                            coroutineScope.launch {
                                appViewModel.navigate(
                                    navController,
                                    NavigationItemKey.EVENT_DETAILS,
                                    mapOf("eventId" to eventPreview.id)
                                )
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        onFavoriteClick = {
                            appViewModel.uiEventBus.sendEvent(
                                coroutineScope,
                                FavoriteEventToggle(eventPreview.id, !eventPreview.isFavorited)
                            )
                        }
                    )
                }
            }
        }
    }
} 