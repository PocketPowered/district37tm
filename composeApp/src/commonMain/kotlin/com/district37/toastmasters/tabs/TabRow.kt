package com.district37.toastmasters.tabs

import androidx.compose.material.MaterialTheme
import androidx.compose.material.TabPosition
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.contentColorFor
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.graphics.Color
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
    @Composable
    fun TabRow(
        selectedTabIndex: Int,
        modifier: Modifier = Modifier,
        backgroundColor: Color = MaterialTheme.colors.primarySurface,
        contentColor: Color = contentColorFor(backgroundColor),
        indicator: @Composable @UiComposable (tabPositions: List<TabPosition>) -> Unit = @Composable { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex])
            )
        },
        divider: @Composable @UiComposable () -> Unit = @Composable {
            TabRowDefaults.Divider()
        },
        tabs: @Composable @UiComposable () -> Unit
    ): Unit {

    }