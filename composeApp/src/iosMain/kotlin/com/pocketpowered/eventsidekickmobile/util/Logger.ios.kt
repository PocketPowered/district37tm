package com.district37.toastmasters.util

import platform.Foundation.NSLog

/**
 * iOS implementation of Logger using NSLog
 */
actual object Logger {
    actual fun d(tag: String, message: String) {
        NSLog("DEBUG [$tag]: $message")
    }

    actual fun e(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) {
            NSLog("ERROR [$tag]: $message - ${throwable.message}")
            throwable.printStackTrace()
        } else {
            NSLog("ERROR [$tag]: $message")
        }
    }

    actual fun i(tag: String, message: String) {
        NSLog("INFO [$tag]: $message")
    }
}
