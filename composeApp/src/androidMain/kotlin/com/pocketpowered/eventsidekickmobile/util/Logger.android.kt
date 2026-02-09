package com.district37.toastmasters.util

import android.util.Log

/**
 * Android implementation of Logger using Logcat
 */
actual object Logger {
    actual fun d(tag: String, message: String) {
        Log.d(tag, message)
    }

    actual fun e(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
    }

    actual fun i(tag: String, message: String) {
        Log.i(tag, message)
    }
}
