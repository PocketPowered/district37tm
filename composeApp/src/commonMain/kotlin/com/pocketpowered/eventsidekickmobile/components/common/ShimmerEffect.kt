package com.district37.toastmasters.components.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Applies an animated shimmer effect overlay with custom colors.
 */
@Composable
fun Modifier.shimmerEffect(colors: List<Color>, durationMillis: Int = 2500): Modifier = composed {
    val transition = rememberInfiniteTransition()
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    )

    drawWithContent {
        val brush = Brush.linearGradient(
            colors = colors,
            start = Offset(x = translateAnim - 500f, y = 0f),
            end = Offset(x = translateAnim + 500f, y = 0f)
        )
        drawRect(brush)
        drawContent()
    }
}

/**
 * Applies an animated shimmer effect with default theme-aware colors.
 * Uses a subtle gradient that's visible in both light and dark modes.
 */
@Composable
fun Modifier.shimmerEffect(durationMillis: Int = 1500): Modifier = composed {
    val baseColor = MaterialTheme.colorScheme.surfaceVariant
    val highlightColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    shimmerEffect(listOf(baseColor, highlightColor, baseColor))
}
