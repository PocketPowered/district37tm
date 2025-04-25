package com.district37.toastmasters.eventlist

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.LocalAppViewModel
import com.district37.toastmasters.favorites.FavoriteEventToggle
import com.district37.toastmasters.models.EventPreview
import com.district37.toastmasters.models.EventTag
import com.district37.toastmasters.models.toHumanReadableString
import com.district37.toastmasters.navigation.EVENT_ID_ARG
import com.district37.toastmasters.navigation.NavigationItemKey
import com.wongislandd.nexus.navigation.LocalNavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
private fun Modifier.shimmerEffect(): Modifier = composed {
    val shimmerColors = listOf(
        Color(0xFFB8860B).copy(alpha = 0.2f),
        Color(0xFFF2DF74).copy(alpha = 0.4f),
        Color(0xFFB8860B).copy(alpha = 0.2f)
    )

    val transition = rememberInfiniteTransition()
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 2000f, // extend further for smooth pass
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2500,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    )

    drawBehind {
        val brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(x = translateAnim.value - 1000f, y = 0f),
            end = Offset(x = translateAnim.value, y = 0f)
        )
        drawRect(brush)
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun EventCard(
    eventPreview: EventPreview,
    modifier: Modifier = Modifier
) {
    val navController = LocalNavHostController.current
    val appViewModel = LocalAppViewModel.current
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier.padding(horizontal = 8.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            backgroundColor = MaterialTheme.colors.onSecondary,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                coroutineScope.launch(Dispatchers.Main) {
                    appViewModel.navigate(
                        navController,
                        NavigationItemKey.EVENT_DETAILS,
                        mapOf(EVENT_ID_ARG to eventPreview.id)
                    )
                }
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = eventPreview.title,
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start,
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
                        .padding(start = 8.dp)
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

        if (eventPreview.tag == EventTag.HIGHLIGHTED) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(16.dp))
                    .shimmerEffect()
            )
        }
    }
}