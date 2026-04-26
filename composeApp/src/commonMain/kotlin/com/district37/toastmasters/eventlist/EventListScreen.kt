package com.district37.toastmasters.eventlist

import EventIcon
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.LocalAppViewModel
import com.district37.toastmasters.favorites.FavoriteEventToggle
import com.district37.toastmasters.models.DateTabInfo
import com.district37.toastmasters.navigation.EVENT_ID_ARG
import com.district37.toastmasters.navigation.NavigationItemKey
import com.district37.toastmasters.navigation.StatefulScaffold
import com.district37.toastmasters.notifications.NotificationsEntry
import com.wongislandd.nexus.navigation.LocalNavHostController
import com.wongislandd.nexus.util.Resource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun EventListScreen() {
    val viewModel = koinViewModel<EventListViewModel>()
    val appViewModel = LocalAppViewModel.current
    val navController = LocalNavHostController.current
    val coroutineScope = rememberCoroutineScope()
    val screenState by viewModel.eventListScreenStateSlice.screenState.collectAsState()
    val isRefreshing = screenState is Resource.Loading
    StatefulScaffold(
        title = (screenState as? Resource.Success)?.data?.topBarTitle,
        actions = {
            NotificationsEntry()
        },
        onRefresh = {
            viewModel.uiEventBus.sendEvent(
                coroutineScope, RefreshTriggered
            )
        },
        isRefreshing = isRefreshing,
        forceHamburgerMenu = true,
        resource = screenState
    ) { data ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .background(MaterialTheme.colors.surface),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = data.scheduleTitle,
                style = MaterialTheme.typography.h5,
                color = MaterialTheme.colors.secondary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            AgendaViewModeToggle(
                selectedMode = data.viewMode,
                onModeChanged = { viewMode ->
                    viewModel.uiEventBus.sendEvent(
                        coroutineScope,
                        AgendaViewModeChanged(viewMode)
                    )
                }
            )

            DateSelector(
                availableTabs = data.availableTabs,
                agendaSelection = data.agendaOption,
                onTabClick = { tab ->
                    viewModel.uiEventBus.sendEvent(
                        coroutineScope,
                        DateChanged(tab)
                    )
                },
                onFavoritesToggle = {
                    val nextAgenda = if (data.agendaOption == AgendaOption.FAVORITES_AGENDA) {
                        AgendaOption.FULL_AGENDA
                    } else {
                        AgendaOption.FAVORITES_AGENDA
                    }
                    viewModel.uiEventBus.sendEvent(coroutineScope, AgendaChanged(nextAgenda))
                }
            )

            if (data.events.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (data.isScheduleLoading) {
                        CircularProgressIndicator()
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (data.agendaOption == AgendaOption.FAVORITES_AGENDA) "No favorites found" else "No events found",
                                style = MaterialTheme.typography.h5,
                                color = MaterialTheme.colors.secondary,
                                textAlign = TextAlign.Center
                            )
                            if (data.agendaOption == AgendaOption.FAVORITES_AGENDA) {
                                Text(
                                    text = "Click the heart next to events to add favorites!",
                                    style = MaterialTheme.typography.body2,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 8.dp, start = 32.dp, end = 32.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (data.events.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp)
                ) {
                    when (data.viewMode) {
                        AgendaViewMode.EVENT_LIST -> {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .fillMaxSize()
                            ) {
                                items(data.events) { event ->
                                    EventCard(event)
                                }
                            }
                        }

                        AgendaViewMode.SCHEDULE -> {
                            CalendarAgendaView(
                                events = data.events,
                                onEventClick = { event ->
                                    appViewModel.navigate(
                                        navController,
                                        NavigationItemKey.EVENT_DETAILS,
                                        mapOf(EVENT_ID_ARG to event.id)
                                    )
                                },
                                onFavoriteToggle = { event ->
                                    appViewModel.uiEventBus.sendEvent(
                                        coroutineScope,
                                        FavoriteEventToggle(event.id, !event.isFavorited)
                                    )
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    if (data.isScheduleLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
private fun AgendaViewModeToggle(
    selectedMode: AgendaViewMode,
    onModeChanged: (AgendaViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf(
        AgendaViewMode.EVENT_LIST to Icons.AutoMirrored.Outlined.List,
        AgendaViewMode.SCHEDULE to EventIcon
    )
    val selectedIndex = options.indexOfFirst { it.first == selectedMode }.coerceAtLeast(0)
    val thumbOffset by animateDpAsState(
        targetValue = if (selectedIndex == 0) 4.dp else 52.dp,
        animationSpec = tween(durationMillis = 180),
        label = "agenda-view-switch"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = MaterialTheme.colors.primary.copy(alpha = 0.10f),
            modifier = Modifier,
            onClick = {
                val nextMode = if (selectedMode == AgendaViewMode.EVENT_LIST) {
                    AgendaViewMode.SCHEDULE
                } else {
                    AgendaViewMode.EVENT_LIST
                }
                onModeChanged(nextMode)
            }
        ) {
            Box(
                modifier = Modifier
                    .size(width = 92.dp, height = 40.dp)
                    .padding(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    options.forEachIndexed { index, (_, icon) ->
                        AgendaViewModeIcon(
                            icon = icon,
                            isActive = selectedIndex == index,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 2.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier
                        .size(36.dp)
                        .offset(x = thumbOffset)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = options[selectedIndex].second,
                            contentDescription = if (selectedMode == AgendaViewMode.EVENT_LIST) {
                                "List view selected"
                            } else {
                                "Schedule view selected"
                            },
                            tint = MaterialTheme.colors.onPrimary
                        )
                    }
                }
            }
        }

        Text(
            text = if (selectedMode == AgendaViewMode.EVENT_LIST) "List View" else "Schedule View",
            style = MaterialTheme.typography.caption,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
private fun AgendaViewModeIcon(
    icon: ImageVector,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.height(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isActive) {
                MaterialTheme.colors.primary.copy(alpha = 0.25f)
            } else {
                MaterialTheme.colors.onSurface.copy(alpha = 0.45f)
            }
        )
    }
}

@Composable
private fun DateSelector(
    availableTabs: List<DateTabInfo>,
    agendaSelection: AgendaOption,
    onTabClick: (DateTabInfo) -> Unit,
    onFavoritesToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedTabIndex = availableTabs.indexOfFirst { it.isSelected }.let { idx ->
        if (idx >= 0) idx else 0
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.weight(1f),
            edgePadding = 16.dp,
            backgroundColor = MaterialTheme.colors.surface,
            contentColor = MaterialTheme.colors.secondary,
            divider = {}
        ) {
            availableTabs.forEach { tab ->
                Tab(
                    selected = tab.isSelected,
                    onClick = { onTabClick(tab) },
                    text = {
                        Text(
                            tab.displayName,
                            fontWeight = if (tab.isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        IconButton(onClick = onFavoritesToggle) {
            Icon(
                imageVector = if (agendaSelection == AgendaOption.FAVORITES_AGENDA) {
                    Icons.Filled.Favorite
                } else {
                    Icons.Outlined.FavoriteBorder
                },
                contentDescription = if (agendaSelection == AgendaOption.FAVORITES_AGENDA) {
                    "Show full schedule"
                } else {
                    "Show favorites"
                },
                tint = if (agendaSelection == AgendaOption.FAVORITES_AGENDA) {
                    Color.Red
                } else {
                    MaterialTheme.colors.onSurface.copy(alpha = 0.75f)
                }
            )
        }
    }
}
