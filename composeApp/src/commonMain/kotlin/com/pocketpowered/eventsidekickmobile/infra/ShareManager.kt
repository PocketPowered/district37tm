package com.district37.toastmasters.infra

/**
 * Platform-specific sharing functionality.
 * Opens the native share sheet on each platform to share a URL.
 */
expect class ShareManager {
    /**
     * Share a URL using the platform's native share functionality
     * @param url The URL to share
     * @param title Optional title for the share dialog (Android uses this as chooser title)
     */
    fun share(url: String, title: String? = null)
}
