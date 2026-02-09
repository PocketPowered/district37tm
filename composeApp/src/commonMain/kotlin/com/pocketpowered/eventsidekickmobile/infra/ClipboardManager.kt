package com.district37.toastmasters.infra

/**
 * Platform-specific clipboard operations
 */
expect class ClipboardManager {
    /**
     * Copy text to the system clipboard
     */
    fun copyToClipboard(text: String)
}
