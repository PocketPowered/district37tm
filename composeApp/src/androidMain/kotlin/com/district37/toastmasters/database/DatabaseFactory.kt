package com.district37.toastmasters.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual class DatabaseFactory actual constructor(private val appContext: Any?) {
    actual fun createDriver(): SqlDriver {
        val appContext = requireNotNull(appContext as? Context)
        return AndroidSqliteDriver(
            schema = TMDatabase.Schema,
            context = appContext,
            name = "tm.db"
        )
    }
} 