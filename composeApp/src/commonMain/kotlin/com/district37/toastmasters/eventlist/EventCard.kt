package com.district37.toastmasters.eventlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.LocalAppViewModel
import com.district37.toastmasters.favorites.FavoriteEventToggle
import com.district37.toastmasters.models.EventPreview
import com.district37.toastmasters.models.toHumanReadableString
import com.district37.toastmasters.navigation.EVENT_ID_ARG
import com.district37.toastmasters.navigation.NavigationItemKey
import com.wongislandd.nexus.navigation.LocalNavHostController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun EventCard(
    eventPreview: EventPreview,
    modifier: Modifier = Modifier
) {
    val navController = LocalNavHostController.current
    val appViewModel = LocalAppViewModel.current
    val coroutineScope = rememberCoroutineScope()
    Card(
        shape = RoundedCornerShape(16.dp),
        backgroundColor = MaterialTheme.colors.onSecondary,
        modifier = modifier.padding(horizontal = 8.dp),
        onClick = {
            coroutineScope.launch {
                appViewModel.navigate(
                    navController,
                    NavigationItemKey.EVENT_DETAILS,
                    mapOf(EVENT_ID_ARG to eventPreview.id)
                )
            }
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp)
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
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface,
                )
                Text(
                    text = eventPreview.time.toHumanReadableString(),
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface,
                )
            }
            Icon(
                imageVector = if (eventPreview.isFavorited) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (eventPreview.isFavorited) "Unfavorite" else "Favorite",
                tint = if (eventPreview.isFavorited) Color.Red else MaterialTheme.colors.onSurface,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(24.dp)
                    .clickable {
                        appViewModel.uiEventBus.sendEvent(
                            coroutineScope, FavoriteEventToggle(
                                eventPreview.id,
                                !eventPreview.isFavorited
                            )
                        )
                    }
            )
        }

    }
}