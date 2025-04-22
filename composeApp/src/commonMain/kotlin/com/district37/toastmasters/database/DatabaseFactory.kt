package com.district37.toastmasters.database

import app.cash.sqldelight.db.SqlDriver

expect class DatabaseFactory(appContext: Any? = null) {
    fun createDriver(): SqlDriver
} 