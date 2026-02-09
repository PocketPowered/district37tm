package com.district37.toastmasters.features.onboarding.steps

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.components.wizard.WizardStepContainer
import com.district37.toastmasters.features.onboarding.OnboardingWizardViewModel

/**
 * Step 2: Username
 * Collects the user's username with real-time validation and availability checking
 * Shows personalized greeting using first name from step 1
 */
@Composable
fun UsernameStep(
    viewModel: OnboardingWizardViewModel,
    modifier: Modifier = Modifier
) {
    val firstName by viewModel.firstName.collectAsState()
    val username by viewModel.username.collectAsState()
    val usernameError by viewModel.usernameError.collectAsState()
    val isCheckingUsername by viewModel.isCheckingUsername.collectAsState()
    val isUsernameAvailable by viewModel.isUsernameAvailable.collectAsState()
    val focusManager = LocalFocusManager.current

    WizardStepContainer(
        prompt = "Hey $firstName! Select a username.",
        subtitle = "This is how others will find you",
        modifier = modifier
    ) {
        OutlinedTextField(
            value = username,
            onValueChange = { viewModel.updateUsername(it) },
            prefix = { Text("@") },
            placeholder = { Text("username") },
            singleLine = true,
            isError = usernameError != null,
            supportingText = {
                val error = usernameError
                when {
                    error != null -> Text(error)
                    isCheckingUsername -> Text("Checking availability...")
                    isUsernameAvailable == true -> Text(
                        "Username is available!",
                        color = MaterialTheme.colorScheme.primary
                    )
                    username.isBlank() -> Text("3-18 characters: lowercase, numbers, underscores")
                    else -> Text("3-18 characters: lowercase, numbers, underscores")
                }
            },
            trailingIcon = {
                when {
                    isCheckingUsername -> CircularProgressIndicator(
                        modifier = Modifier.size(20.dp)
                    )
                    isUsernameAvailable == true -> Icon(
                        Icons.Default.Check,
                        contentDescription = "Available",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    usernameError != null -> Icon(
                        Icons.Default.Close,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Ascii,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.clearFocus()
                }
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
