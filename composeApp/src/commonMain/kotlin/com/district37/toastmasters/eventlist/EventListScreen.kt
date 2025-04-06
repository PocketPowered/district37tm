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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.LocalAppViewModel
import com.district37.toastmasters.components.EventImage
import com.district37.toastmasters.models.EventPreview
import com.district37.toastmasters.navigation.EVENT_ID_ARG
import com.district37.toastmasters.navigation.NavigationItemKey
import com.district37.toastmasters.navigation.StatefulScaffold
import com.wongislandd.nexus.navigation.LocalNavHostController
import com.wongislandd.nexus.util.Resource
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
                coroutineScope.launch {
                    appViewModel.navigate(
                        navController,
                        NavigationItemKey.INFO
                    )
                }
            }) {
                Icon(Icons.Default.Info, contentDescription = "123")
            }
        },
        onRefresh = {

        },
        isRefreshing = isRefreshing,
        resource = screenState
    ) { data ->
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background)
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun EventCard(
    eventPreview: EventPreview,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(2.dp), modifier = modifier.height(
            height = 400.dp
        ), onClick = onCardClick
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            EventImage(url = eventPreview.image, modifier = Modifier.fillMaxSize())
            // Gradient overlay at the bottom for readability
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(100.dp)
                    .blur(8.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colors.primary.copy(alpha = 0.4f),
                                MaterialTheme.colors.primary.copy(alpha = 0.6f),
                                MaterialTheme.colors.primary.copy(alpha = 0.7f),
                                MaterialTheme.colors.primary.copy(alpha = 0.9f)
                            ),
                            startY = 0f,
                            endY = 180f
                        )
                    )
            )
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, start = 8.dp)
                    .align(Alignment.BottomStart)
            ) {
                Text(
                    text = eventPreview.title,
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onPrimary,
                    modifier = Modifier,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2
                )
                Text(
                    text = eventPreview.locationInfo,
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onPrimary,
                )
                Text(
                    text = eventPreview.time.timeDisplay,
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onPrimary,
                )
            }
        }

    }

}
