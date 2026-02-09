package com.district37.toastmasters.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.components.images.ImageGallery
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.Color
import com.district37.toastmasters.models.Image
import com.district37.toastmasters.navigation.LocalTopAppBarInsets

/**
 * Parallax hero image component with scroll-based animation.
 * The image scrolls at a slower rate than the content for a depth effect.
 *
 * @param images List of images to display in gallery
 * @param lazyListState The scroll state from the parent LazyColumn for parallax calculation
 * @param heroHeight Height of the hero section (default 300dp)
 * @param parallaxFactor How much slower the image scrolls (0.5 = half speed, default)
 * @param useTopBarPadding Whether to pad content for top bar visibility
 * @param modifier Optional modifier
 */
@Composable
fun ParallaxHero(
    images: List<Image>,
    lazyListState: LazyListState,
    parallaxFactor: Float = 0.5f,
    useTopBarPadding: Boolean = true,
    modifier: Modifier = Modifier
) {
    val insets = LocalTopAppBarInsets.current

    // Calculate parallax offset based on scroll position
    // Only apply when hero is visible (first item)
    val parallaxOffset by remember {
        derivedStateOf {
            if (lazyListState.firstVisibleItemIndex == 0) {
                lazyListState.firstVisibleItemScrollOffset * parallaxFactor
            } else {
                0f
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 300.dp)
            .background(Color.Black)
            .clipToBounds() // Prevent image from drawing outside bounds
    ) {
        if (images.isNotEmpty()) {
            // Image container with extra height for parallax movement
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        translationY = parallaxOffset
                    }
            ) {
                ImageGallery(
                    images = images
                )
            }

            // Spacer to push pager indicators below top bar fade gradient
            if (useTopBarPadding && images.size > 1) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(insets.recommendedHeroPadding)
                )
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
