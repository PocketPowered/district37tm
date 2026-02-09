package com.district37.toastmasters.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Badge type for more options icons.
 */
enum class OptionBadge {
    NONE,
    ADD,      // Green + badge
    REMOVE    // Red X badge
}

/**
 * Data class representing an option in the more options bottom sheet.
 */
data class MoreOption(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val isActive: Boolean = false,
    val isLoading: Boolean = false,
    val activeColor: Color? = null,
    val badge: OptionBadge = OptionBadge.NONE,
    val onClick: () -> Unit
)

/**
 * State holder for the more options bottom sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
class MoreOptionsState(
    private val scope: CoroutineScope,
    val sheetState: SheetState
) {
    var showSheet by mutableStateOf(false)

    fun show() {
        showSheet = true
    }

    fun hide() {
        scope.launch {
            sheetState.hide()
            showSheet = false
        }
    }
}

/**
 * Remember a MoreOptionsState instance for the current composition.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberMoreOptionsState(): MoreOptionsState {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    return remember { MoreOptionsState(scope, sheetState) }
}

/**
 * More options bottom sheet component.
 * Displays a menu with customizable options.
 *
 * @param state The MoreOptionsState to control visibility
 * @param title The title displayed at the top of the sheet
 * @param options List of options to display
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreOptionsBottomSheet(
    state: MoreOptionsState,
    title: String = "Options",
    options: List<MoreOption>
) {
    if (state.showSheet) {
        ModalBottomSheet(
            onDismissRequest = { state.showSheet = false },
            sheetState = state.sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                options.forEachIndexed { index, option ->
                    MoreOptionRow(
                        icon = option.icon,
                        title = option.title,
                        subtitle = option.subtitle,
                        isActive = option.isActive,
                        isLoading = option.isLoading,
                        activeColor = option.activeColor,
                        badge = option.badge,
                        onClick = {
                            option.onClick()
                            state.hide()
                        }
                    )

                    if (index < options.lastIndex) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

/**
 * Row item for options in the bottom sheet.
 */
@Composable
private fun MoreOptionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isActive: Boolean,
    isLoading: Boolean,
    activeColor: Color?,
    badge: OptionBadge,
    onClick: () -> Unit
) {
    val iconTint = when {
        isActive && activeColor != null -> activeColor
        isActive -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLoading, onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = iconTint
                )

                // Badge overlay
                if (badge != OptionBadge.NONE) {
                    val badgeColor = when (badge) {
                        OptionBadge.ADD -> Color(0xFF4CAF50) // Green
                        OptionBadge.REMOVE -> Color(0xFFF44336) // Red
                        else -> Color.Transparent
                    }
                    val badgeIcon = when (badge) {
                        OptionBadge.ADD -> Icons.Filled.Add
                        OptionBadge.REMOVE -> Icons.Filled.Close
                        else -> Icons.Filled.Add
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 4.dp, y = 4.dp)
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(badgeColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = badgeIcon,
                            contentDescription = null,
                            modifier = Modifier.size(10.dp),
                            tint = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
