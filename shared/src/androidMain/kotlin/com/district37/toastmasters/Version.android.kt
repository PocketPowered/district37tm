package com.district37.toastmasters

import android.content.Context
import android.content.pm.PackageInfo

class AndroidVersionInfo(private val packageInfo: PackageInfo) : VersionInfo {
    override val versionName: String = packageInfo.versionName
    override val versionCode: Int = packageInfo.versionCode
}

actual fun getVersionInfo(context: Any?): VersionInfo {
    val androidContext = requireNotNull(context as? Context) { "Context must be provided for Android" }
    val packageInfo = androidContext.packageManager.getPackageInfo(androidContext.packageName, 0)
    return AndroidVersionInfo(packageInfo)
} 