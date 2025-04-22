package com.district37.toastmasters.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual class DatabaseFactory actual constructor(private val appContext: Any?) {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = NotificationDatabase.Schema,
            name = "notification.db"
        )
    }
} 