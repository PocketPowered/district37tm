package com.district37.toastmasters.references

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
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import com.district37.toastmasters.models.BackendExternalLink
import com.district37.toastmasters.navigation.StatefulScaffold
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun ReferencesScreen() {
    val viewModel: ReferencesViewModel = koinViewModel<ReferencesViewModel>()
    val referencesResource by viewModel.references.collectAsState()
    StatefulScaffold(
        title = "References",
        resource = referencesResource,
        onRefresh = {
            viewModel.onRefresh()
        }
    ) { references ->
        if (references.isEmpty()) {
            Text(
                text = "No references available",
                modifier = Modifier.padding(16.dp).fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(references) { reference ->
                    ReferenceItem(reference)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ReferenceItem(reference: BackendExternalLink) {
    val urlHandler = LocalUriHandler.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        onClick = {
            try {
                reference.url?.let { url ->
                    urlHandler.openUri(url)
                }
            } catch (e: Exception) {
                Logger.e("ReferencesScreen") {
                    "Could not link out to $reference"
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
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = reference.displayName ?: "Untitled Reference",
                    style = MaterialTheme.typography.subtitle1
                )
                Text(
                    text = reference.url ?: "",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.primary
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