package com.district37.toastmasters.features.create.organization.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.components.common.ColorPicker
import com.district37.toastmasters.components.wizard.WizardStepContainer
import com.district37.toastmasters.features.create.organization.CreateOrganizationWizardViewModel

/**
 * Step 4: Organization Color Scheme
 * Allows selection of primary and secondary brand colors.
 */
@Composable
fun OrganizationColorSchemeStep(
    viewModel: CreateOrganizationWizardViewModel,
    modifier: Modifier = Modifier
) {
    val primaryColor by viewModel.primaryColor.collectAsState()
    val secondaryColor by viewModel.secondaryColor.collectAsState()

    WizardStepContainer(
        prompt = "Choose your brand colors",
        subtitle = "These colors will be used throughout your organization's profile",
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            ColorPicker(
                selectedColor = primaryColor,
                onColorSelected = { viewModel.updatePrimaryColor(it) },
                label = "Primary Color"
            )

            ColorPicker(
                selectedColor = secondaryColor,
                onColorSelected = { viewModel.updateSecondaryColor(it) },
                label = "Secondary Color"
            )
        }
    }
}
