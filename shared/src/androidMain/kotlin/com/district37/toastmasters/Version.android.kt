package com.district37.toastmasters

import android.content.Context
import android.content.pm.PackageInfo
import android.os.Build

class AndroidVersionInfo(private val packageInfo: PackageInfo) : VersionInfo {
    override val versionName: String = packageInfo.versionName ?: ""
    override val versionCode: Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode.toInt()
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode
        }
}

actual fun getVersionInfo(context: Any?): VersionInfo {
    val androidContext = requireNotNull(context as? Context) { "Context must be provided for Android" }
    val packageInfo = androidContext.packageManager.getPackageInfo(androidContext.packageName, 0)
    return AndroidVersionInfo(packageInfo)
} 
