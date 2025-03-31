package com.district37.toastmasters

class JVMPlatform: Platform {
    override val name: String = "JVM Platform"
}

actual fun getPlatform(): Platform {
    return JVMPlatform()
}