package com.district37.toastmasters

class IOSVersionInfo : VersionInfo {
    override val versionName: String
        get() = "iOS Toastmasters"
    override val versionCode: Int
        get() = 5
}

actual fun getVersionInfo(context: Any?): VersionInfo = IOSVersionInfo() 