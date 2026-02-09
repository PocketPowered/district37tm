package com.district37.toastmasters.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.components.images.ImageGallery
import com.district37.toastmasters.models.Image
import com.district37.toastmasters.navigation.LocalTopAppBarInsets
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage

/**
 * Minimalist hero image component - pure image with no gradient overlay.
 * Designed for full-bleed display at 400dp height.
 *
 * The image extends under the floating top bar, but a top spacer ensures that
 * important content is visible below the fade gradient and floating top bar.
 *
 * @param images List of images to display
 * @param useTopBarPadding Whether to pad content to be visible below top bar (default: true)
 * @param modifier Optional modifier
 */
@Composable
fun MinimalistEventHero(
    images: List<Image>,
    useTopBarPadding: Boolean = true,
    modifier: Modifier = Modifier
) {
    // Read insets to pad content appropriately
    val insets = LocalTopAppBarInsets.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(400.dp)
    ) {
        if (images.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Image fills entire space (full-bleed under top bar)
                ImageGallery(
                    images = images,
                    modifier = Modifier.fillMaxSize()
                )

                // Transparent spacer to push pager indicators down
                // This ensures indicators are visible below the top bar's fade gradient
                if (useTopBarPadding && images.size > 1) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(insets.recommendedHeroPadding)
                    )
                }
            }
        } else {
            // Placeholder when no image
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
            }
        }
    }
}

/**
 * Floating content card that overlaps the hero image.
 * Contains event title, date, description, and engagement actions.
 *
 * @param title Event name
 * @param subtitle Date/time information
 * @param description Optional event description
 * @param typeBadge Optional type badge text
 * @param engagementContent Optional composable for engagement bar
 * @param modifier Optional modifier
 */
@Composable
fun MinimalistContentCard(
    title: String,
    subtitle: String?,
    description: String?,
    typeBadge: String? = null,
    engagementContent: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .offset(y = (-40).dp), // Negative offset to overlap hero
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Type badge
            typeBadge?.let { type ->
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = type,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Event name - prominent
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Date range
            subtitle?.let { sub ->
                Text(
                    text = sub,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Description
            description?.let { desc ->
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Engagement bar
            engagementContent?.invoke()
        }
    }
}

/**
 * Minimalist section wrapper with title and optional action.
 * Provides consistent styling for content sections.
 *
 * @param title Section title
 * @param action Optional composable for action button/content
 * @param content Content to display in the section
 * @param modifier Optional modifier
 */
@Composable
fun MinimalistSection(
    title: String,
    action: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            action?.invoke()
        }

        // Section content
        content()
    }
}

/**
 * Minimal icon button with consistent styling.
 *
 * @param icon Icon to display
 * @param contentDescription Accessibility description
 * @param onClick Click handler
 * @param tint Optional tint color
 * @param modifier Optional modifier
 */
@Composable
fun MinimalIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(44.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint
        )
    }
}

/**
 * Spacing component to compensate for floating content card negative margin.
 * Use after MinimalistContentCard to maintain proper spacing.
 */
@Composable
fun ContentCardSpacing(modifier: Modifier = Modifier) {
    Box(modifier = modifier.height(8.dp))
}
