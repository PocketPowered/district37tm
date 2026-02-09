package com.district37.toastmasters.components.forms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * A reusable date and time picker field component.
 * Shows the selected date and time, and opens pickers when clicked.
 *
 * @param label The label for the field
 * @param selectedDateTime The currently selected date/time as an Instant
 * @param onDateTimeSelected Callback when a new date/time is selected
 * @param minDateTime Optional minimum allowed date/time
 * @param maxDateTime Optional maximum allowed date/time
 * @param required Whether this field is required
 * @param error Optional error message to display
 * @param enabled Whether the field is enabled
 * @param modifier Modifier for the field
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerField(
    label: String,
    selectedDateTime: Instant?,
    onDateTimeSelected: (Instant) -> Unit,
    minDateTime: Instant? = null,
    maxDateTime: Instant? = null,
    required: Boolean = false,
    error: String? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val timeZone = TimeZone.currentSystemDefault()
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Current selection state
    val currentLocalDateTime = selectedDateTime?.toLocalDateTime(timeZone)
    val currentDate = currentLocalDateTime?.date
    val currentTime = currentLocalDateTime?.time

    // For the date picker, we need to work with milliseconds since epoch
    val initialDateMillis = selectedDateTime?.toEpochMilliseconds()

    // SelectableDates to enforce min/max date constraints
    val selectableDates = remember(minDateTime, maxDateTime) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val date = Instant.fromEpochMilliseconds(utcTimeMillis)
                    .toLocalDateTime(TimeZone.UTC).date

                val minDate = minDateTime?.toLocalDateTime(timeZone)?.date
                val maxDate = maxDateTime?.toLocalDateTime(timeZone)?.date

                val afterMin = minDate == null || date >= minDate
                val beforeMax = maxDate == null || date <= maxDate

                return afterMin && beforeMax
            }
        }
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis,
        selectableDates = selectableDates
    )

    val timePickerState = rememberTimePickerState(
        initialHour = currentTime?.hour ?: 12,
        initialMinute = currentTime?.minute ?: 0,
        is24Hour = false
    )

    Column(modifier = modifier) {
        Text(
            text = if (required) "$label *" else label,
            style = MaterialTheme.typography.labelMedium,
            color = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { showDatePicker = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    if (currentDate != null) {
                        Text(
                            text = formatDateForDisplay(currentDate),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        Text(
                            text = "Select date",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable(enabled = enabled && currentDate != null) {
                        showTimePicker = true
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                if (currentTime != null) {
                    Text(
                        text = formatTimeForDisplay(currentTime),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.clickable(enabled = enabled) {
                            showTimePicker = true
                        }
                    )
                } else {
                    Text(
                        text = "Time",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable(enabled = enabled && currentDate != null) {
                            showTimePicker = true
                        }
                    )
                }
            }
        }

        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = Instant.fromEpochMilliseconds(millis)
                                .toLocalDateTime(TimeZone.UTC).date

                            // Combine with existing time or default to noon
                            val time = currentTime ?: LocalTime(12, 0)
                            val newDateTime = LocalDateTime(selectedDate, time)
                                .toInstant(timeZone)

                            onDateTimeSelected(newDateTime)

                            // If no time was set, open time picker
                            if (currentTime == null) {
                                showTimePicker = true
                            }
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker Dialog
    if (showTimePicker && currentDate != null) {
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = {
                val newTime = LocalTime(timePickerState.hour, timePickerState.minute)
                val newDateTime = LocalDateTime(currentDate, newTime).toInstant(timeZone)

                // Validate against min/max
                val isValid = (minDateTime == null || newDateTime >= minDateTime) &&
                        (maxDateTime == null || newDateTime <= maxDateTime)

                if (isValid) {
                    onDateTimeSelected(newDateTime)
                }
                showTimePicker = false
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        text = { content() }
    )
}

private fun formatDateForDisplay(date: LocalDate): String {
    val month = date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    return "$month ${date.dayOfMonth}, ${date.year}"
}

private fun formatTimeForDisplay(time: LocalTime): String {
    val hour = time.hour
    val minute = time.minute

    val hour12 = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }

    val amPm = if (hour < 12) "AM" else "PM"
    val minuteStr = minute.toString().padStart(2, '0')

    return "$hour12:$minuteStr $amPm"
}
