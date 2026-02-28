package com.district37.toastmasters.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.LocalAppViewModel
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import com.wongislandd.nexus.navigation.LocalNavHostController
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import district37toastmasters.composeapp.generated.resources.Res
import district37toastmasters.composeapp.generated.resources.app_icon_default
import org.jetbrains.compose.resources.painterResource

@OptIn(KoinExperimentalAPI::class)
@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    val navController = LocalNavHostController.current
    val appViewModel = LocalAppViewModel.current
    val viewModel: SplashViewModel = koinViewModel()
    val isLoading by viewModel.isLoading.collectAsState()
    val splashImageUrl by viewModel.splashImageUrl.collectAsState()
    val launchDestination by viewModel.launchDestination.collectAsState()

    LaunchedEffect(isLoading) {
        if (!isLoading) {
            appViewModel.navigate(
                navController,
                launchDestination,
                removeSelfFromStack = true
            )
        }
    }

    Box(
        modifier = modifier.fillMaxSize().background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        if (splashImageUrl.isNullOrBlank()) {
            DefaultSplashImage()
        } else {
            CoilImage(
                modifier = Modifier.fillMaxSize(),
                imageModel = { splashImageUrl!! },
                imageOptions = ImageOptions(
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center
                ),
                failure = { DefaultSplashImage() }
            )
        }
    }
}

@Composable
private fun DefaultSplashImage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(Res.drawable.app_icon_default),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(128.dp)
        )
    }
}
