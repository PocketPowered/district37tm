package com.district37.toastmasters.components.wizard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.components.common.EventSidekickTitle
import com.district37.toastmasters.navigation.AppBarConfigs
import com.district37.toastmasters.navigation.ConfigureTopAppBar
import com.district37.toastmasters.navigation.LocalTopAppBarInsets
import com.district37.toastmasters.navigation.TopAppBarConfig

/**
 * Reusable scaffold for multi-step wizard flows.
 * Provides a consistent layout with top app bar, animated content area,
 * progress indicator, and next/submit button.
 *
 * @param currentStep The current step index (0-indexed)
 * @param totalSteps Total number of steps in the wizard
 * @param title Title shown in the top app bar
 * @param onBack Callback when back button is pressed
 * @param onNext Callback when next/submit button is pressed
 * @param nextEnabled Whether the next button should be enabled
 * @param nextLabel Label for the next button (e.g., "Continue" or "Create Event")
 * @param isSubmitting Whether the wizard is currently submitting
 * @param errorMessage Optional error message to display
 * @param onDismissError Callback when error is dismissed
 * @param topAppBarConfig Optional custom top app bar config. If null, uses PaginatedList with title
 * @param content Composable content for the current step
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WizardScaffold(
    currentStep: Int,
    totalSteps: Int,
    title: String,
    onBack: () -> Unit,
    onNext: () -> Unit,
    nextEnabled: Boolean,
    nextLabel: String = "Continue",
    isSubmitting: Boolean = false,
    showSkipButton: Boolean = false,
    skipLabel: String = "Skip",
    onSkip: () -> Unit = {},
    errorMessage: String? = null,
    onDismissError: () -> Unit = {},
    topAppBarConfig: TopAppBarConfig? = null,
    content: @Composable () -> Unit
) {
    // Configure the top app bar
    val effectiveConfig = topAppBarConfig ?: AppBarConfigs.formScreen(title = title)
    ConfigureTopAppBar(
        config = effectiveConfig,
        onBackClick = onBack
    )

    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val topBarInsets = LocalTopAppBarInsets.current

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    focusManager.clearFocus()
                }
        ) {
            // Render TopAppBar when floating top bar is hidden (for onboarding flow)
            // This happens when the config hides the top bar but we still need navigation
            if (effectiveConfig.hideTopBar) {
                TopAppBar(
                    title = { EventSidekickTitle() },
                    navigationIcon = {
                        if (currentStep > 0) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(48.dp))
                        }
                    },
                    actions = {
                        Spacer(modifier = Modifier.width(48.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }

            // Main content area with animated transitions
            // Add top padding when using floating top bar (not hideTopBar)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .then(
                        if (!effectiveConfig.hideTopBar) {
                            Modifier.padding(top = topBarInsets.recommendedContentPadding)
                        } else {
                            Modifier
                        }
                    )
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            // Moving forward - slide in from right
                            slideInHorizontally { width -> width } togetherWith
                                slideOutHorizontally { width -> -width }
                        } else {
                            // Moving backward - slide in from left
                            slideInHorizontally { width -> -width } togetherWith
                                slideOutHorizontally { width -> width }
                        }
                    },
                    label = "wizard_step_transition"
                ) { _ ->
                    content()
                }
            }

            // Error banner (shows when there's an error)
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.errorContainer,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onDismissError) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss error",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // Bottom section with progress and button
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 3.dp,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Progress indicator dots
                    StepProgressIndicator(
                        currentStep = currentStep,
                        totalSteps = totalSteps
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Skip button (if enabled)
                    if (showSkipButton) {
                        OutlinedButton(
                            onClick = onSkip,
                            enabled = !isSubmitting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Text(
                                text = skipLabel,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Next/Submit button
                    Button(
                        onClick = onNext,
                        enabled = nextEnabled && !isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.height(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = nextLabel,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }

        // Loading overlay during submission
        if (isSubmitting) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
