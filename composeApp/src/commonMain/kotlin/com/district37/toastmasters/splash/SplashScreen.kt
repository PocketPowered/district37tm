package com.district37.toastmasters.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import com.district37.toastmasters.LocalAppViewModel
import com.district37.toastmasters.navigation.NavigationItemKey
import com.wongislandd.nexus.navigation.LocalNavHostController
import district37toastmasters.composeapp.generated.resources.Res
import district37toastmasters.composeapp.generated.resources.conference_2025
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalResourceApi::class)
@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    val navController = LocalNavHostController.current
    val appViewModel = LocalAppViewModel.current

    LaunchedEffect(Unit) {
        delay(3000)
        appViewModel.navigate(
            navController,
            NavigationItemKey.EVENT_LIST
        )
    }

    Box(
        modifier = modifier.fillMaxSize().background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(Res.drawable.conference_2025),
            contentDescription = null,
            contentScale = ContentScale.Crop, // Makes it fill the screen, cropping as needed
            modifier = Modifier.fillMaxSize()
        )
    }
}