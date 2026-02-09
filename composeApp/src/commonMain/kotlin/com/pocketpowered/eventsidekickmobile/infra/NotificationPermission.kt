package com.district37.toastmasters.infra

import androidx.compose.runtime.Composable

/**
 * Creates a launcher for requesting notification permission.
 *
 * @param onResult Callback invoked with true if permission was granted, false otherwise
 * @return A function that when called, triggers the permission request
 */
@Composable
expect fun rememberNotificationPermissionLauncher(
    onResult: (Boolean) -> Unit
): () -> Unit
