package com.district37.toastmasters.components.images

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.models.Image
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage

/**
 * Reusable image gallery component that displays images in a horizontal pager
 *
 * @param images List of images to display
 * @param modifier Optional modifier for the gallery container
 */
@Composable
fun ImageGallery(
    images: List<Image>,
    modifier: Modifier = Modifier
) {
    if (images.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { images.size })
    var showFullScreenViewer by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            ImageCard(
                image = images[page],
                onClick = { showFullScreenViewer = true }
            )
        }

        // Page indicator
        if (images.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(images.size) { index ->
                    Surface(
                        modifier = Modifier.size(8.dp),
                        shape = CircleShape,
                        color = if (index == pagerState.currentPage) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            Color.White
                        }
                    ) {}
                }
            }
        }
    }

    // Full-screen image viewer
    if (showFullScreenViewer) {
        FullScreenImageViewer(
            images = images,
            initialPage = pagerState.currentPage,
            onDismiss = { showFullScreenViewer = false }
        )
    }
}

/**
 * Displays a single image card with optional caption
 *
 * @param image The image to display
 * @param onClick Callback when the image is clicked
 */
@Composable
fun ImageCard(
    image: Image,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clickable(onClick = onClick)
    ) {
        CoilImage(
            imageModel = { image.url },
            modifier = Modifier
                .fillMaxSize(),
            imageOptions = ImageOptions(
                contentScale = ContentScale.Crop,
                alignment = image.getCropAlignment(),
                contentDescription = image.altText ?: "Image"
            )
        )
    }
}
