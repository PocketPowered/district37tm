package com.district37.toastmasters.util

enum class PlatformType {
    ANDROID,
    IOS
}

expect val currentPlatform: PlatformType
