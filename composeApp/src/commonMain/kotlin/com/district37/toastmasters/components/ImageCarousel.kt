package com.district37.toastmasters.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.placeholder.shimmer.ShimmerPlugin

@Composable
fun ImageCarousel(
    images: List<String>,
    modifier: Modifier = Modifier,
    height: Int = 200
) {
    if (images.isEmpty()) return

    val pagerState = rememberPagerState() { images.size }
    val component = rememberImageComponent {
        +ShimmerPlugin()
    }

    Column(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(height.dp)
        ) { page ->
            CoilImage(
                imageModel = { images[page] },
                component = component,
                imageOptions = ImageOptions(
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.Center
                ),
                modifier = Modifier.fillMaxSize(),
                failure = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colors.error)
                    )
                }
            )
        }

        // Page indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(images.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) {
                    MaterialTheme.colors.primary
                } else {
                    MaterialTheme.colors.onSurface.copy(alpha = 0.2f)
                }
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .size(8.dp)
                        .background(color, CircleShape)
                )
            }
        }
    }
} 