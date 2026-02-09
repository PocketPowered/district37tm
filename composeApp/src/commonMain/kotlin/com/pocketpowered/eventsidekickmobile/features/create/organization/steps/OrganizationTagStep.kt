package com.district37.toastmasters.features.create.organization.steps

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
import com.district37.toastmasters.features.create.organization.CreateOrganizationWizardViewModel

/**
 * Step 1: Organization Tag
 * Collects the organization's unique identifier with real-time validation and availability checking.
 * Tags share the same namespace as usernames, so they must be globally unique.
 */
@Composable
fun OrganizationTagStep(
    viewModel: CreateOrganizationWizardViewModel,
    modifier: Modifier = Modifier
) {
    val tag by viewModel.tag.collectAsState()
    val tagError by viewModel.tagError.collectAsState()
    val isCheckingTag by viewModel.isCheckingTag.collectAsState()
    val isTagAvailable by viewModel.isTagAvailable.collectAsState()
    val focusManager = LocalFocusManager.current

    WizardStepContainer(
        prompt = "Choose a unique tag for your organization",
        subtitle = "This will be used to identify your organization (like @username)",
        modifier = modifier
    ) {
        OutlinedTextField(
            value = tag,
            onValueChange = { viewModel.updateTag(it) },
            prefix = { Text("@") },
            placeholder = { Text("organization_tag") },
            singleLine = true,
            isError = tagError != null,
            supportingText = {
                val error = tagError
                when {
                    error != null -> Text(error)
                    isCheckingTag -> Text("Checking availability...")
                    isTagAvailable == true -> Text(
                        "Tag is available!",
                        color = MaterialTheme.colorScheme.primary
                    )
                    tag.isBlank() -> Text("3-18 characters: lowercase, numbers, underscores")
                    else -> Text("3-18 characters: lowercase, numbers, underscores")
                }
            },
            trailingIcon = {
                when {
                    isCheckingTag -> CircularProgressIndicator(
                        modifier = Modifier.size(20.dp)
                    )
                    isTagAvailable == true -> Icon(
                        Icons.Default.Check,
                        contentDescription = "Available",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    tagError != null -> Icon(
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
