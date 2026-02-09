package com.district37.toastmasters.infra

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
actual fun rememberNotificationPermissionLauncher(
    onResult: (Boolean) -> Unit
): () -> Unit {
    val pushNotificationService: PushNotificationService = koinInject()
    val scope = rememberCoroutineScope()

    return {
        scope.launch {
            val granted = pushNotificationService.requestNotificationPermission()
            onResult(granted)
        }
    }
}
