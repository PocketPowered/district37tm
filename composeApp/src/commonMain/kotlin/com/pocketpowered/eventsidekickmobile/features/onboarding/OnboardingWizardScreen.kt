package com.district37.toastmasters.features.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.district37.toastmasters.components.wizard.WizardScaffold
import com.district37.toastmasters.features.onboarding.steps.BioStep
import com.district37.toastmasters.features.onboarding.steps.DisplayNameStep
import com.district37.toastmasters.features.onboarding.steps.ThemeStep
import com.district37.toastmasters.features.onboarding.steps.UsernameStep
import com.district37.toastmasters.navigation.TopAppBarConfig
import com.district37.toastmasters.navigation.AppBarConfigs
import com.district37.toastmasters.util.Resource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Onboarding wizard screen shown to new users who need to set up their profile.
 *
 * 4-step wizard flow:
 * 1. Display Name - "What should we call you?"
 * 2. Username - "Hey {firstName}! Select a username."
 * 3. Bio (Optional) - "Tell us about yourself"
 * 4. Theme Colors (Optional) - "Customize your profile"
 *
 * Uses WizardScaffold for consistent layout with stepper dots.
 * Cannot be dismissed - user must complete all steps.
 */
@Composable
fun OnboardingWizardScreen(
    viewModel: OnboardingWizardViewModel = koinViewModel(),
    onOnboardingComplete: () -> Unit
) {
    val currentStep by viewModel.currentStep.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val completionResult by viewModel.completionResult.collectAsState()
    val canProceed by viewModel.canProceed.collectAsState()

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Handle completion result
    LaunchedEffect(completionResult) {
        when (val result = completionResult) {
            is Resource.Success -> {
                errorMessage = null
                onOnboardingComplete()
            }
            is Resource.Error -> {
                errorMessage = result.message ?: "Failed to complete setup. Please try again."
            }
            else -> { }
        }
    }

    val isLastStep = currentStep == viewModel.totalSteps - 1
    val nextLabel = if (isLastStep) "Get Started" else "Continue"

    WizardScaffold(
        currentStep = currentStep,
        totalSteps = viewModel.totalSteps,
        title = "", // Not used when topAppBarConfig is provided
        onBack = {
            errorMessage = null
            if (!viewModel.goToPreviousStep()) {
                // At first step - do nothing (can't dismiss)
            }
        },
        onNext = {
            errorMessage = null
            if (isLastStep) {
                viewModel.completeOnboarding()
            } else {
                viewModel.goToNextStep()
            }
        },
        nextEnabled = canProceed,
        nextLabel = nextLabel,
        isSubmitting = isSubmitting,
        errorMessage = errorMessage,
        onDismissError = {
            errorMessage = null
            viewModel.resetCompletionResult()
        },
        topAppBarConfig = AppBarConfigs.rootScreen()  // Show EventSidekick logo
    ) {
        when (currentStep) {
            0 -> DisplayNameStep(viewModel = viewModel)
            1 -> UsernameStep(viewModel = viewModel)
            2 -> BioStep(viewModel = viewModel)
            3 -> ThemeStep(viewModel = viewModel)
        }
    }
}
