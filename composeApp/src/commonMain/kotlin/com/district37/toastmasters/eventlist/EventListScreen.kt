package com.district37.toastmasters.eventlist

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.models.DateTabInfo
import com.district37.toastmasters.navigation.StatefulScaffold
import com.district37.toastmasters.notifications.NotificationsEntry
import com.wongislandd.nexus.util.Resource
import com.wongislandd.nexus.util.conditionallyChain
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun EventListScreen() {
    val viewModel = koinViewModel<EventListViewModel>()
    val coroutineScope = rememberCoroutineScope()
    val screenState by viewModel.eventListScreenStateSlice.screenState.collectAsState()
    val isRefreshing = screenState is Resource.Loading
    StatefulScaffold(
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
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                .background(MaterialTheme.colors.surface),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Agenda",
                style = MaterialTheme.typography.h5,
                color = MaterialTheme.colors.secondary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            DateSelector(data.availableTabs, onTabClick = { tab ->
                viewModel.uiEventBus.sendEvent(
                    coroutineScope,
                    DateChanged(tab)
                )
            })
            AgendaSelector(
                agendaSelection = data.agendaOption,
                onFullAgendaClicked = {
                    viewModel.uiEventBus.sendEvent(
                        coroutineScope,
                        AgendaChanged(AgendaOption.FULL_AGENDA)
                    )
                },
                onMyAgendaClicked = {
                    viewModel.uiEventBus.sendEvent(
                        coroutineScope,
                        AgendaChanged(AgendaOption.FAVORITES_AGENDA)
                    )
                }
            )
            if (data.events.isEmpty()) {
                Text(
                    text = if (data.agendaOption == AgendaOption.FAVORITES_AGENDA) "No favorites found" else "No events found",
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.secondary,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp).fillMaxSize()
            ) {
                items(data.events) { event ->
                    EventCard(event)
                }
            }
        }

    }
}


@Composable
fun AgendaSelector(
    agendaSelection: AgendaOption,
    onFullAgendaClicked: () -> Unit,
    onMyAgendaClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val indicatorOffset by animateDpAsState(
        targetValue = if (agendaSelection == AgendaOption.FULL_AGENDA) 0.dp else 180.dp,
        label = "IndicatorOffsetAnimation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colors.onSurface.copy(alpha = 0.05f))
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.5f)
                    .offset(x = indicatorOffset)
                    .clip(CircleShape)
                    .background(MaterialTheme.colors.secondary)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onFullAgendaClicked() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Agenda",
                    color = if (agendaSelection == AgendaOption.FULL_AGENDA) MaterialTheme.colors.onSecondary else MaterialTheme.colors.secondary,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onMyAgendaClicked() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Favorites",
                    color = if (agendaSelection == AgendaOption.FAVORITES_AGENDA) MaterialTheme.colors.onSecondary else MaterialTheme.colors.secondary,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun DateSelector(
    availableTabs: List<DateTabInfo>,
    onTabClick: (DateTabInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        availableTabs.forEach { tab ->
            item {
                CustomTab(tab, onTabClick = onTabClick, modifier = Modifier)
            }
        }
    }
}


@Composable
private fun CustomTab(
    tab: DateTabInfo,
    onTabClick: (DateTabInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    Tab(
        text = {
            Text(
                tab.displayName,
                color = if (tab.isSelected) MaterialTheme.colors.onSecondary else MaterialTheme.colors.secondary,
                fontWeight = FontWeight.Bold
            )
        },
        selected = tab.isSelected,
        onClick = { onTabClick(tab) },
        modifier = modifier.clip(
            CircleShape
        ).conditionallyChain(
            tab.isSelected, Modifier.background(MaterialTheme.colors.secondary)
        )
    )
}
