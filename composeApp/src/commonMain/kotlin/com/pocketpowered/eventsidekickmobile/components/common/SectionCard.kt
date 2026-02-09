package com.district37.toastmasters.components.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Reusable section card wrapper with rounded corners and consistent styling.
 * Used to wrap content sections in detail screens for a modern card-based layout.
 *
 * @param modifier Optional modifier
 * @param horizontalPadding Horizontal margin around the card (default 16dp)
 * @param contentPadding Internal padding inside the card (default 20dp)
 * @param cornerRadius Corner radius for rounded shape (default 16dp)
 * @param elevation Shadow elevation (default 2dp)
 * @param content Content to display inside the card
 */
@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 16.dp,
    contentPadding: Dp = 20.dp,
    cornerRadius: Dp = 16.dp,
    elevation: Dp = 2.dp,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        shape = RoundedCornerShape(cornerRadius),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = elevation,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(contentPadding)
        ) {
            content()
        }
    }
}
