package com.district37.toastmasters.components.forms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.navigation.ConfigureTopAppBar
import com.district37.toastmasters.navigation.AppBarConfigs
import com.district37.toastmasters.navigation.LocalTopAppBarInsets

/**
 * Reusable scaffold for entity creation forms
 *
 * Provides a consistent layout with:
 * - TopAppBar configuration with back button
 * - Scrollable content area for form fields
 * - Bottom action bar with Cancel/Create buttons
 * - Loading overlay during submission
 *
 * @param title The title shown in the top app bar
 * @param onBackClick Callback when back button is pressed
 * @param onSubmit Callback when Create button is pressed
 * @param onCancel Callback when Cancel button is pressed (defaults to onBackClick)
 * @param isSubmitting Whether the form is currently submitting
 * @param submitEnabled Whether the submit button should be enabled
 * @param submitLabel Label for the submit button (default: "Create")
 * @param content Composable content for the form fields
 */
@Composable
fun EntityFormScaffold(
    title: String,
    onBackClick: () -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit = onBackClick,
    isSubmitting: Boolean = false,
    submitEnabled: Boolean = true,
    submitLabel: String = "Create",
    content: @Composable () -> Unit
) {
    // Configure the top app bar
    ConfigureTopAppBar(
        config = AppBarConfigs.formScreen(title = title),
        onBackClick = onBackClick
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
            // Scrollable form content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Add top padding for floating top bar
                Spacer(modifier = Modifier.height(topBarInsets.recommendedContentPadding))
                content()

                // Add bottom padding for the action bar
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Bottom action bar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            tonalElevation = 3.dp,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    enabled = !isSubmitting,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = onSubmit,
                    enabled = submitEnabled && !isSubmitting,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(submitLabel)
                    }
                }
            }
        }

        // Loading overlay
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
