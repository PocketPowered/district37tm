package com.district37.toastmasters.resources

import Linkout
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import com.district37.toastmasters.models.BackendExternalLink
import com.district37.toastmasters.navigation.StatefulScaffold
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun FirstTimerResourcesScreen() {
    val viewModel: FirstTimerResourcesViewModel = koinViewModel<FirstTimerResourcesViewModel>()
    val resourcesResource by viewModel.resources.collectAsState()
    StatefulScaffold(
        title = "First Timer Resources",
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
                        text = "No first timer resources available",
                        modifier = Modifier.padding(16.dp).fillMaxSize(),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                item {
                    Text(
                        text = "Welcome to your first conference! Here are some resources to help you get started.",
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ResourceItem(resource: BackendExternalLink) {
    val urlHandler = LocalUriHandler.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        onClick = {
            try {
                resource.url?.let { url ->
                    urlHandler.openUri(url)
                }
            } catch (e: Exception) {
                Logger.e("FirstTimerResourcesScreen") {
                    "Could not link out to $resource"
                }
            }
        },
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = resource.displayName ?: "Untitled Resource",
                    style = MaterialTheme.typography.subtitle1,
                    textAlign = TextAlign.Start
                )

                resource.description?.also {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp),
                        textAlign = TextAlign.Start
                    )
                }
                Text(
                    text = resource.url ?: "",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.primary,
                    textAlign = TextAlign.Start
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Linkout,
                contentDescription = "Open link",
                modifier = Modifier.size(24.dp)
            )
        }
    }
} 