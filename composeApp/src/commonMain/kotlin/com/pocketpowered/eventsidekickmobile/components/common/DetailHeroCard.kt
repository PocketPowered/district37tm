package com.district37.toastmasters.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.components.images.ImageGallery
import com.district37.toastmasters.graphql.type.EventType
import com.district37.toastmasters.models.Image

/**
 * Reusable detail hero card with edge-to-edge image and gradient content section
 *
 * @param images List of images to display in gallery
 * @param title Main title text
 * @param typeBadge Optional type badge text (e.g., "conference", "venue")
 * @param subtitle Optional subtitle text (e.g., date, location)
 * @param description Optional description text
 * @param additionalContent Optional composable for custom content below description
 */
@Composable
fun DetailHeroCard(
    images: List<Image>,
    title: String,
    typeBadge: String? = null,
    subtitle: String? = null,
    description: String? = null,
    additionalContent: @Composable (() -> Unit)? = null
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Edge-to-edge hero image
            if (images.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    ImageGallery(
                        images = images,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Gradient content section below image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            0.0f to MaterialTheme.colorScheme.background.copy(alpha = 1.0f),
                            0.8f to MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
                            1.0f to MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Title
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    // Type Badge
                    typeBadge?.let { type ->
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = type,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    // Subtitle (e.g., date, location)
                    subtitle?.let { sub ->
                        Text(
                            text = sub,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Description
                    description?.let { desc ->
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Additional custom content
                    additionalContent?.invoke()
                }
            }
        }
    }
}
