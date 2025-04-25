package com.district37.toastmasters.resources

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.navigation.StatefulScaffold
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun ResourcesScreen() {
    val viewModel: ResourcesViewModel = koinViewModel<ResourcesViewModel>()
    val resourcesResource by viewModel.resources.collectAsState()
    StatefulScaffold(
        title = "Resources",
        resource = resourcesResource,
        onRefresh = {
            viewModel.onRefresh()
        }
    ) { resources ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (resources.isEmpty()) {
                item {
                    Text(
                        text = "No resources available",
                        modifier = Modifier.padding(16.dp).fillMaxSize(),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                item {
                    Text(
                        text = "Browse more materials and resources to support your conference experience.",
                        style = MaterialTheme.typography.h5,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }
            items(resources) { resource ->
                ResourceItem(resource)
            }
        }
    }
}