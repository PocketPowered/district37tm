package com.district37.toastmasters.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.wongislandd.nexus.GenericErrorScreen
import com.wongislandd.nexus.GenericLoadingScreen
import com.wongislandd.nexus.navigation.GlobalTopAppBar
import com.wongislandd.nexus.util.PullToRefreshWrapper
import com.wongislandd.nexus.util.Resource

@Composable
fun <T> StatefulScaffold(
    title: String? = null,
    modifier: Modifier = Modifier,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    actions: (@Composable RowScope.() -> Unit) = {},
    resource: Resource<T>,
    successContent: @Composable (T) -> Unit
) {
    Scaffold(topBar = {
        GlobalTopAppBar(
            title = title, homeDestination =
            supportedNavigationItems[NavigationItemKey.LANDING_PAGE]?.completeRoute
                ?: throw IllegalStateException("Home destination not found"),
            actions = actions
        )
    }, modifier = modifier) {
        PullToRefreshWrapper(isRefreshing, onRefresh) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (resource) {
                    is Resource.Error -> {
                        GenericErrorScreen()
                    }

                    is Resource.Loading -> {
                        GenericLoadingScreen()
                    }

                    is Resource.Success -> {
                        if (resource.data != null) {
                            successContent(resource.data)
                        } else {
                            GenericErrorScreen("Successful response but no data!")
                        }
                    }
                }
            }
        }
    }
}