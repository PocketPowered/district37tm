package com.district37.toastmasters.locations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.models.Location
import com.district37.toastmasters.navigation.StatefulScaffold
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.placeholder.shimmer.ShimmerPlugin
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun LocationsScreen() {
    val viewModel: LocationsViewModel = koinViewModel<LocationsViewModel>()
    val locationsResource by viewModel.locations.collectAsState()

    StatefulScaffold(
        title = "Maps",
        resource = locationsResource,
        onRefresh = {
            viewModel.onRefresh()
        }
    ) { locations ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (locations.isEmpty()) {
                item {
                    Text(
                        text = "No locations available",
                        modifier = Modifier.padding(16.dp).fillMaxSize(),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                item {
                    Text(
                        text = "Browse conference locations and venues.",
                        style = MaterialTheme.typography.h5,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }
            items(locations) { location ->
                LocationItem(location)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun LocationItem(location: Location) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = location.locationName,
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (location.locationImages.isNotEmpty()) {
                LocationImageCarousel(location.locationImages)
            }
        }
    }
}

@Composable
private fun LocationImageCarousel(images: List<String>) {
    val pagerState = rememberPagerState() { images.size }
    val component = rememberImageComponent {
        +ShimmerPlugin()
    }

    Column {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) { page ->
            CoilImage(
                imageModel = { images[page] } ,
                component = component,
                imageOptions = ImageOptions(
                    contentScale = ContentScale.Crop,
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