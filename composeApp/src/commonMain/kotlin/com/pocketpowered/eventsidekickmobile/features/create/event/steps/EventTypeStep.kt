package com.district37.toastmasters.features.create.event.steps

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.components.wizard.WizardStepContainer
import com.district37.toastmasters.features.create.event.CreateEventWizardViewModel
import com.district37.toastmasters.graphql.type.EventType

/**
 * Step 3: Event Type
 * Allows selection of an event type using large, tappable cards in a 2-row scrollable layout.
 * Both rows scroll together as a single unit.
 */
@Composable
fun EventTypeStep(
    viewModel: CreateEventWizardViewModel,
    modifier: Modifier = Modifier
) {
    val selectedType by viewModel.eventType.collectAsState()
    val allTypes = viewModel.availableEventTypes
    val scrollState = rememberScrollState()

    // Split types into two rows for better visual layout
    val midPoint = (allTypes.size + 1) / 2
    val topRowTypes = allTypes.take(midPoint)
    val bottomRowTypes = allTypes.drop(midPoint)

    WizardStepContainer(
        prompt = "What type of event is this?",
        subtitle = "Select a category that best describes your event (optional)",
        modifier = modifier
    ) {
        // Single scrollable container for both rows
        Column(
            modifier = Modifier.horizontalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                topRowTypes.forEach { type ->
                    EventTypeCard(
                        eventType = type,
                        isSelected = selectedType == type,
                        onClick = {
                            if (selectedType == type) {
                                viewModel.selectEventType(null)
                            } else {
                                viewModel.selectEventType(type)
                            }
                        },
                        modifier = Modifier.width(140.dp)
                    )
                }
            }

            // Bottom row
            if (bottomRowTypes.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    bottomRowTypes.forEach { type ->
                        EventTypeCard(
                            eventType = type,
                            isSelected = selectedType == type,
                            onClick = {
                                if (selectedType == type) {
                                    viewModel.selectEventType(null)
                                } else {
                                    viewModel.selectEventType(type)
                                }
                            },
                            modifier = Modifier.width(140.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EventTypeCard(
    eventType: EventType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected) {
        getEventTypeColor(eventType).copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val borderColor = if (isSelected) {
        getEventTypeColor(eventType)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = getEventTypeIcon(eventType),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (isSelected) {
                    getEventTypeColor(eventType)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Text(
                text = getEventTypeDisplayName(eventType),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) {
                    getEventTypeColor(eventType)
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

private fun getEventTypeDisplayName(eventType: EventType): String {
    return when (eventType) {
        EventType.CONFERENCE -> "Conference"
        EventType.CONCERT -> "Concert"
        EventType.FESTIVAL -> "Festival"
        EventType.WORKSHOP -> "Workshop"
        else -> eventType.name.lowercase()
            .replaceFirstChar { it.uppercase() }
    }
}

private fun getEventTypeIcon(eventType: EventType): ImageVector {
    return when (eventType) {
        EventType.CONFERENCE -> Icons.Default.DateRange
        EventType.CONCERT -> Icons.Default.Star
        EventType.FESTIVAL -> Icons.Default.Face
        EventType.WORKSHOP -> Icons.Default.Build
        else -> Icons.Default.DateRange
    }
}

private fun getEventTypeColor(eventType: EventType): Color {
    return when (eventType) {
        EventType.CONFERENCE -> Color(0xFF2196F3) // Blue
        EventType.CONCERT -> Color(0xFF9C27B0)    // Purple
        EventType.FESTIVAL -> Color(0xFFE91E63)   // Pink
        EventType.WORKSHOP -> Color(0xFFFFC107)   // Amber
        else -> Color(0xFF9E9E9E)                 // Grey
    }
}
