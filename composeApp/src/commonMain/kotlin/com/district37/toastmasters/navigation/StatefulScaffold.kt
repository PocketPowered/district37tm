package com.district37.toastmasters.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.DrawerValue
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberDrawerState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.wongislandd.nexus.GenericErrorScreen
import com.wongislandd.nexus.navigation.GlobalTopAppBar
import com.wongislandd.nexus.navigation.LocalNavHostController
import com.wongislandd.nexus.util.PullToRefreshWrapper
import com.wongislandd.nexus.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun <T> StatefulScaffold(
    title: String? = null,
    modifier: Modifier = Modifier,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    actions: (@Composable RowScope.() -> Unit) = {},
    forceHamburgerMenu: Boolean = false,
    resource: Resource<T>,
    successContent: @Composable (T) -> Unit
) {
    val navController = LocalNavHostController.current
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scaffoldState = rememberScaffoldState(drawerState)
    Scaffold(
        topBar = {
            GlobalTopAppBar(
                title = title, homeDestination =
                supportedNavigationItems[NavigationItemKey.EVENT_LIST]?.completeRoute
                    ?: throw IllegalStateException("Home destination not found"),
                actions = {
                    actions()
                },
                onHamburgerMenuClick = {
                    coroutineScope.launch(Dispatchers.Main) {
                        drawerState.open()
                    }
                },
                forceHamburgerMenu = forceHamburgerMenu
            )
        },
        drawerContent = {
            DrawerContent(
                onItemClick = { key ->
                    val navigationItem = requireNotNull(supportedNavigationItems[key]) {
                        "Couldn't find navigation item with key $key!"
                    }
                    navController.navigate(navigationItem.completeRoute)
                },
                onCloseDrawer = {
                    coroutineScope.launch(Dispatchers.Main) {
                        drawerState.close()
                    }
                }
            )
        },
        scaffoldState = scaffoldState,
        modifier = modifier
    ) {
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
                        //GenericLoadingScreen()
                    }

                    is Resource.Success -> {
                        if (resource.data != null) {
                            successContent(resource.data)
                        } else {
                            GenericErrorScreen("Successful response but no data!")
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}