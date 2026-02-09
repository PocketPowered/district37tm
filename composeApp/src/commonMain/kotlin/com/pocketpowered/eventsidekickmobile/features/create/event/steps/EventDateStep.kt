package com.district37.toastmasters.features.create.event.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.components.forms.DateTimePickerField
import com.district37.toastmasters.components.wizard.WizardStepContainer
import com.district37.toastmasters.features.create.event.CreateEventWizardViewModel

/**
 * Step for selecting event start and end dates/times.
 * Allows users to pick when the event is taking place.
 */
@Composable
fun EventDateStep(
    viewModel: CreateEventWizardViewModel,
    modifier: Modifier = Modifier
) {
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()

    WizardStepContainer(
        prompt = "When is your event?",
        subtitle = "Select the start and end date/time for your event",
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DateTimePickerField(
                label = "Start Date",
                selectedDateTime = startDate,
                onDateTimeSelected = { viewModel.updateStartDate(it) },
                required = true,
                modifier = Modifier.fillMaxWidth()
            )
            DateTimePickerField(
                label = "End Date",
                selectedDateTime = endDate,
                onDateTimeSelected = { viewModel.updateEndDate(it) },
                minDateTime = startDate,
                required = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
