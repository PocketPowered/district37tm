package com.district37.toastmasters.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.district37.toastmasters.util.ErrorType
import com.district37.toastmasters.util.Resource

/**
 * Standardized error dialog component that handles Resource.Error states consistently.
 *
 * Features:
 * - Consistent error titles based on error type
 * - Default error messages for common error types
 * - Optional retry action for network errors
 * - Automatic dismiss and cancel buttons
 *
 * Usage:
 * ```kotlin
 * @Composable
 * fun MyScreen(viewModel: MyViewModel) {
 *     val data by viewModel.data.collectAsState()
 *
 *     ErrorDialogHost(
 *         error = data as? Resource.Error,
 *         onDismiss = { viewModel.clearError() },
 *         onRetry = { viewModel.retry() }
 *     )
 * }
 * ```
 *
 * @param error The error to display, or null if no error
 * @param onDismiss Callback when the dialog is dismissed
 * @param onRetry Optional retry callback (shows retry button for network errors)
 */
@Composable
fun ErrorDialogHost(
    error: Resource.Error?,
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)? = null
) {
    if (error != null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(getErrorTitle(error.errorType)) },
            text = { Text(error.message ?: getDefaultErrorMessage(error.errorType)) },
            confirmButton = {
                if (onRetry != null && error.errorType == ErrorType.NETWORK_ERROR) {
                    Button(onClick = { onDismiss(); onRetry() }) {
                        Text("Retry")
                    }
                } else {
                    Button(onClick = onDismiss) { Text("OK") }
                }
            },
            dismissButton = if (onRetry != null) {
                { TextButton(onClick = onDismiss) { Text("Cancel") } }
            } else null
        )
    }
}

/**
 * Get user-friendly error title based on error type
 */
private fun getErrorTitle(errorType: ErrorType): String = when (errorType) {
    ErrorType.NETWORK_ERROR -> "Connection Error"
    ErrorType.SERVER_ERROR -> "Server Error"
    ErrorType.CLIENT_ERROR -> "Request Error"
    ErrorType.UNKNOWN_ERROR -> "Error"
}

/**
 * Get default error message for error types when no specific message is provided
 */
private fun getDefaultErrorMessage(errorType: ErrorType): String = when (errorType) {
    ErrorType.NETWORK_ERROR -> "Please check your internet connection and try again."
    ErrorType.SERVER_ERROR -> "Something went wrong on our end. Please try again later."
    ErrorType.CLIENT_ERROR -> "There was a problem with your request."
    ErrorType.UNKNOWN_ERROR -> "An unexpected error occurred."
}
