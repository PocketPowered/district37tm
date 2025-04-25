package com.district37.toastmasters.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import com.district37.toastmasters.LocalAppViewModel
import com.district37.toastmasters.navigation.NavigationItemKey
import com.wongislandd.nexus.navigation.LocalNavHostController
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import district37toastmasters.composeapp.generated.resources.Res
import district37toastmasters.composeapp.generated.resources.conference_2025
import org.jetbrains.compose.resources.painterResource

@OptIn(KoinExperimentalAPI::class)
@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    val navController = LocalNavHostController.current
    val appViewModel = LocalAppViewModel.current
    val viewModel: SplashViewModel = koinViewModel()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(isLoading) {
        if (!isLoading) {
            appViewModel.navigate(
                navController,
                NavigationItemKey.EVENT_LIST,
                removeSelfFromStack = true
            )
        }
    }

    Box(
        modifier = modifier.fillMaxSize().background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(Res.drawable.conference_2025),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}