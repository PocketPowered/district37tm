package com.district37.toastmasters

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform