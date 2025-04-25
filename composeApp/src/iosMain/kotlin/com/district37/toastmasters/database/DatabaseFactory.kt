package com.district37.toastmasters.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import co.touchlab.kermit.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSHomeDirectory
import platform.Foundation.NSURL

actual class DatabaseFactory actual constructor(private val appContext: Any?) {
    @OptIn(ExperimentalForeignApi::class)
    actual fun createDriver(): SqlDriver {
        Logger.withTag("DatabaseFactory").i { "Creating database! " }
        val fileManager = NSFileManager.defaultManager
        val documentsPath = NSHomeDirectory() + "/Documents"
        val documentsUrl = NSURL.fileURLWithPath(documentsPath)
        
        // Ensure the Documents directory exists
        fileManager.createDirectoryAtURL(
            documentsUrl,
            withIntermediateDirectories = true,
            attributes = null,
            error = null
        )
        
        // Use just the filename for the database
        val databaseName = "tm.db"
        
        return NativeSqliteDriver(
            schema = TMDatabase.Schema,
            name = databaseName
        )
    }
} 