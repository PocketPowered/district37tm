package com.district37.toastmasters.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.placeholder.shimmer.ShimmerPlugin

@Composable
fun EventImage(
    url: String,
    contentScale: ContentScale = ContentScale.FillBounds,
    modifier: Modifier = Modifier
) {
    CoilImage(
        modifier = modifier,
        imageModel = { url },
        imageOptions = ImageOptions(
            contentScale = contentScale,
            alignment = Alignment.Center
        ),
        component = rememberImageComponent {
            +ShimmerPlugin()
        },
        failure = {
            Box(modifier = Modifier.fillMaxSize()) {
                Text("Could not load image.", modifier = Modifier.align(Alignment.Center))
            }
        }
    )
}