package com.district37.toastmasters.components.organizations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.models.Organization
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage

/**
 * Parse a hex color string to Compose Color.
 * Supports formats: "#RRGGBB" or "#AARRGGBB"
 */
private fun String.parseHexColor(): Color? {
    return try {
        val hex = this.removePrefix("#")
        when (hex.length) {
            6 -> Color(
                red = hex.substring(0, 2).toInt(16) / 255f,
                green = hex.substring(2, 4).toInt(16) / 255f,
                blue = hex.substring(4, 6).toInt(16) / 255f
            )
            8 -> Color(
                alpha = hex.substring(0, 2).toInt(16) / 255f,
                red = hex.substring(2, 4).toInt(16) / 255f,
                green = hex.substring(4, 6).toInt(16) / 255f,
                blue = hex.substring(6, 8).toInt(16) / 255f
            )
            else -> null
        }
    } catch (e: Exception) {
        null
    }
}

/**
 * Carousel-style organization card with logo/image, designed to match VenueCarouselCard styling.
 * Used in horizontal scrolling lists/carousels.
 */
@Composable
internal fun OrganizationCarouselCard(
    organization: Organization,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val logoUrl = organization.logoUrl
    val primaryImage = organization.images.firstOrNull()

    // Use organization's primary color if available, otherwise use theme
    val primaryColor = organization.primaryColor?.parseHexColor()
    val secondaryColor = organization.secondaryColor?.parseHexColor()

    Card(
        modifier = modifier,
        onClick = onClick,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp,
            pressedElevation = 3.dp
        ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column {
            // Image Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                when {
                    logoUrl != null -> {
                        CoilImage(
                            imageModel = { logoUrl },
                            modifier = Modifier.fillMaxSize(),
                            imageOptions = ImageOptions(
                                contentScale = ContentScale.Crop,
                                contentDescription = "${organization.name} logo"
                            )
                        )
                    }
                    primaryImage != null -> {
                        CoilImage(
                            imageModel = { primaryImage.url },
                            modifier = Modifier.fillMaxSize(),
                            imageOptions = ImageOptions(
                                contentScale = ContentScale.Crop,
                                alignment = primaryImage.getCropAlignment(),
                                contentDescription = primaryImage.altText ?: organization.name
                            )
                        )
                    }
                    else -> {
                        // Placeholder when no image - use org colors if available
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            primaryColor ?: MaterialTheme.colorScheme.secondaryContainer,
                                            secondaryColor ?: MaterialTheme.colorScheme.tertiaryContainer
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }

            // Content Section
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = organization.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Show tag if available
                organization.tag?.let { tag ->
                    Text(
                        text = "@$tag",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
