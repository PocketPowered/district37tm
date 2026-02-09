package com.district37.toastmasters.features.create.venue.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.components.forms.FormDropdown
import com.district37.toastmasters.components.wizard.WizardStepContainer
import com.district37.toastmasters.features.create.venue.CreateVenueWizardViewModel

/**
 * US States for the dropdown selector
 */
private val US_STATES = listOf(
    "" to "Select State",
    "AL" to "Alabama",
    "AK" to "Alaska",
    "AZ" to "Arizona",
    "AR" to "Arkansas",
    "CA" to "California",
    "CO" to "Colorado",
    "CT" to "Connecticut",
    "DE" to "Delaware",
    "FL" to "Florida",
    "GA" to "Georgia",
    "HI" to "Hawaii",
    "ID" to "Idaho",
    "IL" to "Illinois",
    "IN" to "Indiana",
    "IA" to "Iowa",
    "KS" to "Kansas",
    "KY" to "Kentucky",
    "LA" to "Louisiana",
    "ME" to "Maine",
    "MD" to "Maryland",
    "MA" to "Massachusetts",
    "MI" to "Michigan",
    "MN" to "Minnesota",
    "MS" to "Mississippi",
    "MO" to "Missouri",
    "MT" to "Montana",
    "NE" to "Nebraska",
    "NV" to "Nevada",
    "NH" to "New Hampshire",
    "NJ" to "New Jersey",
    "NM" to "New Mexico",
    "NY" to "New York",
    "NC" to "North Carolina",
    "ND" to "North Dakota",
    "OH" to "Ohio",
    "OK" to "Oklahoma",
    "OR" to "Oregon",
    "PA" to "Pennsylvania",
    "RI" to "Rhode Island",
    "SC" to "South Carolina",
    "SD" to "South Dakota",
    "TN" to "Tennessee",
    "TX" to "Texas",
    "UT" to "Utah",
    "VT" to "Vermont",
    "VA" to "Virginia",
    "WA" to "Washington",
    "WV" to "West Virginia",
    "WI" to "Wisconsin",
    "WY" to "Wyoming",
    "DC" to "District of Columbia"
)

/**
 * Step 2: Venue Location
 * Collects the address, city, state, and zip code.
 */
@Composable
fun VenueLocationStep(
    viewModel: CreateVenueWizardViewModel,
    modifier: Modifier = Modifier
) {
    val address by viewModel.address.collectAsState()
    val city by viewModel.city.collectAsState()
    val state by viewModel.state.collectAsState()
    val zipCode by viewModel.zipCode.collectAsState()
    val focusManager = LocalFocusManager.current

    // Find the selected state pair, or default to the placeholder
    val selectedStatePair = US_STATES.find { it.first == state } ?: US_STATES.first()

    WizardStepContainer(
        prompt = "Where is it located?",
        subtitle = "Enter the venue's address",
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Street Address
            OutlinedTextField(
                value = address,
                onValueChange = { viewModel.updateAddress(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Street Address") },
                placeholder = { Text("123 Main Street") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )

            // City
            OutlinedTextField(
                value = city,
                onValueChange = { viewModel.updateCity(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("City") },
                placeholder = { Text("City") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                )
            )

            // State and Zip Code in a row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // State Dropdown
                FormDropdown(
                    selectedOption = selectedStatePair,
                    options = US_STATES,
                    onOptionSelected = { statePair ->
                        viewModel.updateState(statePair.first)
                    },
                    label = "State",
                    modifier = Modifier.weight(1f),
                    optionLabel = { pair ->
                        if (pair.first.isEmpty()) pair.second else "${pair.first} - ${pair.second}"
                    },
                    required = true
                )

                // Zip Code (numbers only)
                OutlinedTextField(
                    value = zipCode,
                    onValueChange = { newValue ->
                        // Only accept digits and limit to 5 characters
                        if (newValue.all { it.isDigit() } && newValue.length <= 5) {
                            viewModel.updateZipCode(newValue)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    label = { Text("Zip Code") },
                    placeholder = { Text("12345") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                        }
                    )
                )
            }
        }
    }
}
