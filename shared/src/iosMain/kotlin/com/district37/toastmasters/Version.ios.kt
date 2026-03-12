package com.district37.toastmasters

import platform.Foundation.NSBundle

class IOSVersionInfo : VersionInfo {
    override val versionName: String
        get() = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String
            ?: ""
    override val versionCode: Int
        get() = (NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleVersion") as? String)
            ?.toIntOrNull()
            ?: 0
}

actual fun getVersionInfo(context: Any?): VersionInfo = IOSVersionInfo()
