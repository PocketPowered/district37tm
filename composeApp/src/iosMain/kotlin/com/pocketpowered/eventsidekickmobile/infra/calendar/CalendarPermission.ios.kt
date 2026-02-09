package com.district37.toastmasters.infra.calendar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
actual fun rememberCalendarPermissionLauncher(
    onResult: (Boolean) -> Unit
): () -> Unit {
    val calendarService: CalendarService = koinInject()
    val scope = rememberCoroutineScope()

    return {
        scope.launch {
            val granted = calendarService.requestCalendarPermission()
            onResult(granted)
        }
    }
}
