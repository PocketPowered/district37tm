package com.district37.toastmasters.components.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import district37toastmasters.composeapp.generated.resources.Res
import district37toastmasters.composeapp.generated.resources.gatherlogo
import org.jetbrains.compose.resources.painterResource

@Composable
fun EventSidekickTitle() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(Res.drawable.gatherlogo),
            contentDescription = "Event Sidekick",
            modifier = Modifier.height(40.dp),
            contentScale = ContentScale.FillHeight
        )
    }
}
