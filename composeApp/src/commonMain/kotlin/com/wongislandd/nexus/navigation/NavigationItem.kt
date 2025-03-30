package com.wongislandd.nexus.navigation

class NavigationItem(
    val navigationKey: String,
    val displayName: String,
    private val baseRoute: String,
    private val supportedArgs: List<String>? = listOf(),
) {
    val completeRoute: String
        get() {
            if (supportedArgs.isNullOrEmpty()) return baseRoute
            val extras = supportedArgs.let { args ->
                "/" + args.joinToString("/") { "{$it}" }
            }
            return this.baseRoute + extras
        }

    fun reconstructRoute(
        args: Map<String, Any?> = emptyMap()
    ): String {
        val baseRoute = this.baseRoute
        return buildString {
            append(baseRoute)
            supportedArgs?.forEach { arg ->
                append("/${args[arg]}")
            }
        }
    }
}