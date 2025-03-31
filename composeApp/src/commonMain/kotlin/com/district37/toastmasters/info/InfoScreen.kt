package com.district37.toastmasters.info

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.district37.toastmasters.navigation.StatefulScaffold
import com.wongislandd.nexus.util.Empty
import com.wongislandd.nexus.util.Resource

@Composable
fun InfoScreen(modifier: Modifier = Modifier) {
    StatefulScaffold(resource = Resource.Success(Empty)) {
        Box(modifier = modifier.fillMaxSize()) {
            Column {
                Text("Info Screen")
            }
        }
    }
}