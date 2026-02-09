package com.district37.toastmasters.infra

import android.content.ClipData
import android.content.Context
import android.content.ClipboardManager as AndroidClipboardManager

actual class ClipboardManager(private val context: Context) {
    actual fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as AndroidClipboardManager
        val clip = ClipData.newPlainText("Auth Token", text)
        clipboard.setPrimaryClip(clip)
    }
}
