package com.district37.toastmasters

class AndroidVersionInfo() : VersionInfo {
    override val versionName: String = "JVM"
    override val versionCode: Int = 1
}

actual fun getVersionInfo(context: Any?): VersionInfo {
    return AndroidVersionInfo()
}