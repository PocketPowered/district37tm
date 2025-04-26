package com.district37.toastmasters

interface VersionInfo {
    val versionName: String
    val versionCode: Int
}

expect fun getVersionInfo(context: Any?): VersionInfo 