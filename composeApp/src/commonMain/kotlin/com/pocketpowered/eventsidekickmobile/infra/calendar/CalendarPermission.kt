package com.district37.toastmasters.infra.calendar

import androidx.compose.runtime.Composable

/**
 * Creates a launcher for requesting calendar permissions.
 *
 * @param onResult Callback invoked with true if permission was granted, false otherwise
 * @return A function that when called, triggers the permission request
 */
@Composable
expect fun rememberCalendarPermissionLauncher(
    onResult: (Boolean) -> Unit
): () -> Unit
