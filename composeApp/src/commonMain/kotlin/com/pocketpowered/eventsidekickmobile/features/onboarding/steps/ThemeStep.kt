package com.district37.toastmasters.features.onboarding.steps

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
import com.district37.toastmasters.features.onboarding.OnboardingWizardViewModel

/**
 * Step 4: Profile Theme Colors (Optional)
 * Allows selection of primary (banner) and secondary (background) colors for the user's profile.
 */
@Composable
fun ThemeStep(
    viewModel: OnboardingWizardViewModel,
    modifier: Modifier = Modifier
) {
    val primaryColor by viewModel.primaryColor.collectAsState()
    val secondaryColor by viewModel.secondaryColor.collectAsState()

    WizardStepContainer(
        prompt = "Customize your profile",
        subtitle = "Choose colors for your profile banner and background (optional)",
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            ColorPicker(
                selectedColor = primaryColor,
                onColorSelected = { viewModel.updatePrimaryColor(it) },
                label = "Banner Color"
            )
        }
    }
}
