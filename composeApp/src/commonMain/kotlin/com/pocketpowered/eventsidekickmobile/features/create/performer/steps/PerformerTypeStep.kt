package com.district37.toastmasters.features.create.performer.steps

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TheaterComedy
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.components.wizard.WizardStepContainer
import com.district37.toastmasters.features.create.performer.CreatePerformerWizardViewModel
import com.district37.toastmasters.features.create.performer.PerformerTypeOption

/**
 * Step 2: Performer Type
 * Allows selection of performer type with visual cards.
 */
@Composable
fun PerformerTypeStep(
    viewModel: CreatePerformerWizardViewModel,
    modifier: Modifier = Modifier
) {
    val selectedType by viewModel.performerType.collectAsState()
    val types = viewModel.availablePerformerTypes

    // Split types into two rows
    val midpoint = (types.size + 1) / 2
    val topRowTypes = types.take(midpoint)
    val bottomRowTypes = types.drop(midpoint)

    val scrollState = rememberScrollState()

    WizardStepContainer(
        prompt = "What type of performer?",
        subtitle = "Select a category (optional)",
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
                    PerformerTypeCard(
                        type = type,
                        isSelected = selectedType == type,
                        onClick = {
                            if (selectedType == type) {
                                viewModel.selectPerformerType(null)
                            } else {
                                viewModel.selectPerformerType(type)
                            }
                        }
                    )
                }
            }

            // Bottom row
            if (bottomRowTypes.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    bottomRowTypes.forEach { type ->
                        PerformerTypeCard(
                            type = type,
                            isSelected = selectedType == type,
                            onClick = {
                                if (selectedType == type) {
                                    viewModel.selectPerformerType(null)
                                } else {
                                    viewModel.selectPerformerType(type)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PerformerTypeCard(
    type: PerformerTypeOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = contentColor,
                    modifier = Modifier.size(28.dp)
                )
            } else {
                Icon(
                    imageVector = type.icon,
                    contentDescription = type.displayName,
                    tint = contentColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = type.displayName,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Get an icon for each performer type
 */
private val PerformerTypeOption.icon: ImageVector
    get() = when (this) {
        PerformerTypeOption.SPEAKER -> Icons.Default.RecordVoiceOver
        PerformerTypeOption.MUSICIAN -> Icons.Default.MusicNote
        PerformerTypeOption.BAND -> Icons.Default.Groups
        PerformerTypeOption.DJ -> Icons.Default.Mic
        PerformerTypeOption.COMEDIAN -> Icons.Default.TheaterComedy
        PerformerTypeOption.ACTOR -> Icons.Default.Face
        PerformerTypeOption.DANCER -> Icons.Default.Person
        PerformerTypeOption.ARTIST -> Icons.Default.Star
        PerformerTypeOption.HOST -> Icons.Default.Mic
        PerformerTypeOption.PANELIST -> Icons.Default.Groups
        PerformerTypeOption.INSTRUCTOR -> Icons.Default.School
        PerformerTypeOption.ATHLETE -> Icons.Default.SportsScore
        PerformerTypeOption.OTHER -> Icons.Default.Person
    }
