package com.district37.toastmasters.components.schedules

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.LocalNavController
import com.district37.toastmasters.navigation.AgendaItemDetailNavigationArgs
import com.district37.toastmasters.models.AgendaDate
import com.district37.toastmasters.models.AgendaItemConnection
import com.district37.toastmasters.util.DateTimeFormatter
import com.district37.toastmasters.util.Resource
import kotlinx.datetime.LocalDate

/**
 * Standalone tab row for selecting agenda dates with optional filter chip.
 * Can be used with stickyHeader in LazyColumn for sticky behavior.
 *
 * @param agendaDates List of dates to display as tabs
 * @param selectedTabIndex Currently selected tab index
 * @param onTabSelected Callback when a tab is selected
 * @param modifier Modifier for the component
 * @param stickyInsetHeight Height to extend background upward when used as sticky header.
 *        This draws a background above the component to cover the gradient/status bar area
 *        when the header becomes "stuck" at the top. Does not add padding to content.
 * @param myScheduleOnly Whether the "My RSVPs" filter is active
 * @param onToggleMySchedule Callback to toggle the filter; if null, chip is not shown
 * @param trailingContent Optional trailing content (e.g., settings icon)
 */
@Composable
fun AgendaTabRow(
    agendaDates: List<AgendaDate>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    stickyInsetHeight: Dp = 0.dp,
    myScheduleOnly: Boolean = false,
    onToggleMySchedule: (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null
) {
    if (agendaDates.isEmpty()) return

    val surfaceColor = MaterialTheme.colorScheme.surface

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { clip = false }
            .drawBehind {
                // Draw background extending upward into inset area
                // This covers the gradient when the header is stuck at top
                if (stickyInsetHeight > 0.dp) {
                    drawRect(
                        color = surfaceColor,
                        topLeft = Offset(0f, -stickyInsetHeight.toPx()),
                        size = Size(size.width, stickyInsetHeight.toPx())
                    )
                }
            },
        color = surfaceColor
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.weight(1f),
                edgePadding = 0.dp
            ) {
                agendaDates.forEachIndexed { index, agendaDate ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { onTabSelected(index) },
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Date - formatted nicely
                            Text(
                                text = DateTimeFormatter.formatDayHeader(agendaDate.date),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Filter chip and trailing content on the right
            if (onToggleMySchedule != null || trailingContent != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    if (onToggleMySchedule != null) {
                        FilterChip(
                            selected = myScheduleOnly,
                            onClick = onToggleMySchedule,
                            label = { Text("My RSVPs") },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (myScheduleOnly) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                    trailingContent?.invoke()
                }
            }
        }
    }
}

/**
 * Swipeable content area for agenda tabs using HorizontalPager.
 * Allows horizontal swipe gestures to navigate between dates.
 *
 * @param agendaDates List of available dates
 * @param agendaItemsMap Map of date to agenda items connection
 * @param selectedTabIndex Currently selected tab index
 * @param onTabSelected Callback when a tab is selected (via swipe)
 * @param isLoadingMoreItems Set of dates currently loading more items
 * @param onLoadMoreItems Callback to load more agenda items for a date
 * @param emptyMessage Message to show when no agenda items are found
 */
@Composable
fun SwipeableAgendaTabContent(
    agendaDates: List<AgendaDate>,
    agendaItemsMap: Map<LocalDate, Resource<AgendaItemConnection>>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    isLoadingMoreItems: Set<LocalDate> = emptySet(),
    onLoadMoreItems: (date: LocalDate) -> Unit = {},
    emptyMessage: String = "No items for this date"
) {
    if (agendaDates.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No schedule available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    val pagerState = rememberPagerState(
        initialPage = selectedTabIndex,
        pageCount = { agendaDates.size }
    )

    // Sync pager -> feature state (when user swipes)
    LaunchedEffect(pagerState.settledPage) {
        if (pagerState.settledPage != selectedTabIndex) {
            onTabSelected(pagerState.settledPage)
        }
    }

    // Sync feature state -> pager (when tab is clicked)
    LaunchedEffect(selectedTabIndex) {
        if (pagerState.currentPage != selectedTabIndex) {
            pagerState.animateScrollToPage(selectedTabIndex)
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxWidth(),
        beyondViewportPageCount = 0,
        key = { pageIndex -> agendaDates[pageIndex].date.toString() }
    ) { pageIndex ->
        val date = agendaDates[pageIndex].date
        AgendaPageContent(
            date = date,
            agendaItemsState = agendaItemsMap[date],
            isLoadingMoreItems = isLoadingMoreItems,
            onLoadMoreItems = onLoadMoreItems,
            emptyMessage = emptyMessage
        )
    }
}

/**
 * Content for a single agenda page (date).
 */
@Composable
private fun AgendaPageContent(
    date: LocalDate,
    agendaItemsState: Resource<AgendaItemConnection>?,
    isLoadingMoreItems: Set<LocalDate>,
    onLoadMoreItems: (date: LocalDate) -> Unit,
    emptyMessage: String
) {
    val navController = LocalNavController.current

    // Remember the last successfully loaded connection to prevent content flash during loading
    var lastLoadedConnection by remember { mutableStateOf<AgendaItemConnection?>(null) }

    // Update last loaded connection when we get successful data
    LaunchedEffect(agendaItemsState) {
        if (agendaItemsState is Resource.Success && agendaItemsState.data.agendaItems.isNotEmpty()) {
            lastLoadedConnection = agendaItemsState.data
        }
    }

    // Determine if we're in a loading state
    val isLoading = agendaItemsState is Resource.Loading || agendaItemsState is Resource.NotLoading

    Box(modifier = Modifier.fillMaxWidth()) {
        when {
            // Error state - always show error
            agendaItemsState is Resource.Error -> {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = agendaItemsState.message ?: "Failed to load agenda items",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
            // Success with data
            agendaItemsState is Resource.Success -> {
                val connection = agendaItemsState.data
                val items = connection.agendaItems
                if (items.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emptyMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    AgendaContentView(
                        connection = connection,
                        selectedDate = date,
                        isLoadingMoreItems = isLoadingMoreItems,
                        onLoadMoreItems = onLoadMoreItems,
                        onItemClick = { item ->
                            navController.navigate(AgendaItemDetailNavigationArgs(item.id, showEvent = false))
                        }
                    )
                }
            }
            // Loading state - show previous content if available, otherwise show loading indicator
            isLoading && lastLoadedConnection != null -> {
                // Show previous content with loading overlay
                Box(modifier = Modifier.fillMaxWidth()) {
                    AgendaContentView(
                        connection = lastLoadedConnection!!,
                        selectedDate = date,
                        isLoadingMoreItems = isLoadingMoreItems,
                        onLoadMoreItems = onLoadMoreItems,
                        onItemClick = { item ->
                            navController.navigate(AgendaItemDetailNavigationArgs(item.id, showEvent = false))
                        },
                        modifier = Modifier.alpha(0.5f)
                    )
                    // Loading overlay
                    Box(
                        modifier = Modifier.matchParentSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            // Loading state with no previous content - show centered loading
            else -> {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

/**
 * Content area for agenda tabs showing the calendar view for the selected date.
 * Handles loading, error, success, and empty states.
 *
 * During loading, maintains the previous content to prevent scroll position jumps.
 *
 * @param agendaDates List of available dates
 * @param agendaItemsMap Map of date to agenda items connection
 * @param selectedTabIndex Currently selected tab index
 * @param isLoadingMoreItems Set of dates currently loading more items
 * @param onLoadMoreItems Callback to load more agenda items for a date
 * @param emptyMessage Message to show when no agenda items are found
 */
@Composable
fun AgendaTabContent(
    agendaDates: List<AgendaDate>,
    agendaItemsMap: Map<LocalDate, Resource<AgendaItemConnection>>,
    selectedTabIndex: Int,
    isLoadingMoreItems: Set<LocalDate> = emptySet(),
    onLoadMoreItems: (date: LocalDate) -> Unit = {},
    emptyMessage: String = "No items for this date"
) {
    if (agendaDates.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No schedule available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    val selectedDate = agendaDates[selectedTabIndex].date
    AgendaPageContent(
        date = selectedDate,
        agendaItemsState = agendaItemsMap[selectedDate],
        isLoadingMoreItems = isLoadingMoreItems,
        onLoadMoreItems = onLoadMoreItems,
        emptyMessage = emptyMessage
    )
}

/**
 * Internal composable for displaying agenda content (calendar view + pagination).
 */
@Composable
private fun AgendaContentView(
    connection: AgendaItemConnection,
    selectedDate: LocalDate,
    isLoadingMoreItems: Set<LocalDate>,
    onLoadMoreItems: (date: LocalDate) -> Unit,
    onItemClick: (com.district37.toastmasters.models.AgendaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Calendar day view with time grid and overlap handling
        CalendarDayView(
            items = connection.agendaItems,
            onItemClick = onItemClick
        )

        // Loading more items indicator and trigger
        val isLoadingMore = isLoadingMoreItems.contains(selectedDate)
        if (connection.hasNextPage || isLoadingMore) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLoadingMore) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    // Trigger load more when this becomes visible
                    LaunchedEffect(selectedDate) {
                        onLoadMoreItems(selectedDate)
                    }
                    // Show a subtle loading indicator while waiting
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
        }
    }
}

/**
 * Tab-based agenda display component with lazy loading.
 * Shows dates in tabs and loads agenda items on-demand when a tab is selected.
 * Supports pagination for agenda items within each date.
 *
 * This is a convenience wrapper that combines AgendaTabRow and AgendaTabContent.
 * For sticky header behavior in LazyColumn, use the individual components instead.
 *
 * @param agendaDates List of dates to display as tabs
 * @param agendaItemsMap Map of date to agenda items connection
 * @param selectedTabIndex Currently selected tab index (managed externally by AgendaTabsFeature)
 * @param isLoadingMoreItems Set of dates currently loading more items
 * @param onTabSelected Callback when a tab is selected (should call feature.selectTab)
 * @param onLoadMoreItems Callback to load more agenda items for a date
 * @param showLocation Whether to show location chips on agenda items
 * @param showPerformers Whether to show performer chips on agenda items
 * @param emptyMessage Message to show when no agenda items are found
 */
@Composable
fun AgendaTabs(
    agendaDates: List<AgendaDate>,
    agendaItemsMap: Map<LocalDate, Resource<AgendaItemConnection>>,
    selectedTabIndex: Int,
    isLoadingMoreItems: Set<LocalDate> = emptySet(),
    onTabSelected: (Int) -> Unit,
    onLoadMoreItems: (date: LocalDate) -> Unit = {},
    showLocation: Boolean = true,
    showPerformers: Boolean = true,
    emptyMessage: String = "No items for this date"
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        AgendaTabRow(
            agendaDates = agendaDates,
            selectedTabIndex = selectedTabIndex,
            onTabSelected = onTabSelected
        )
        AgendaTabContent(
            agendaDates = agendaDates,
            agendaItemsMap = agendaItemsMap,
            selectedTabIndex = selectedTabIndex,
            isLoadingMoreItems = isLoadingMoreItems,
            onLoadMoreItems = onLoadMoreItems,
            emptyMessage = emptyMessage
        )
    }
}
