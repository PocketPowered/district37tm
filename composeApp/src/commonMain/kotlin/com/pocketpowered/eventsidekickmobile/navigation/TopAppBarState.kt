package com.district37.toastmasters.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Configuration for the floating top app bar.
 * Use [TopAppBarDefaults] for common configurations, or compose properties directly
 * for custom behavior.
 *
 * This is a composable data class - all properties are immutable and can be compared
 * for equality to avoid unnecessary recompositions.
 *
 * @property hideTopBar Completely hide the top bar (e.g., login screen)
 * @property hideBottomNav Hide the bottom navigation bar (e.g., chat screens)
 * @property showBackButton Show a back button on the left side
 * @property centerContent Content to display in the center of the top bar
 * @property actions Composable content for the right action area
 * @property enableScrollCollapse Enable scroll-to-collapse behavior
 * @property showUserAvatar Show the user's avatar in the left bubble (root screens only)
 * @property gradientColor Custom color for the top gradient overlay (e.g., profile banner color)
 */
@Immutable
data class TopAppBarConfig(
    val hideTopBar: Boolean = false,
    val hideBottomNav: Boolean = false,
    val showBackButton: Boolean = false,
    val centerContent: CenterContent = CenterContent.None,
    val actions: (@Composable RowScope.() -> Unit)? = null,
    val enableScrollCollapse: Boolean = false,
    val showUserAvatar: Boolean = false,
    val gradientColor: Color? = null
) {
    /**
     * Center content variants for the top bar.
     * Each variant renders differently in the floating top bar.
     */
    sealed interface CenterContent {
        /** No center content - empty space */
        data object None : CenterContent

        /** App logo (EventSidekickTitle) */
        data object Logo : CenterContent

        /** Text title with optional subtitle */
        data class Title(
            val title: String,
            val subtitle: String? = null
        ) : CenterContent

        /** Avatar with display name (for chat/profile screens) */
        data class Avatar(
            val displayName: String,
            val avatarUrl: String?
        ) : CenterContent

        /** Custom composable content */
        data class Custom(
            val content: @Composable () -> Unit
        ) : CenterContent
    }

    companion object {
        /** Hidden configuration - completely hides the top bar */
        val Hidden = TopAppBarConfig(hideTopBar = true)
    }
}

/**
 * Pre-built [TopAppBarConfig] factory functions for common screen types.
 * Use these instead of constructing configs manually for consistency.
 *
 * Note: Named "AppBarConfigs" to avoid conflicts with Material3's TopAppBarDefaults.
 */
object AppBarConfigs {
    /**
     * Root screen configuration: avatar + logo in left bubble, optional actions in right bubble.
     * Used for main tab screens (Explore, Create, Account).
     *
     * @param actions Optional composable for right-side actions (search, notifications, etc.)
     */
    fun rootScreen(
        actions: (@Composable RowScope.() -> Unit)? = null
    ) = TopAppBarConfig(
        showUserAvatar = true,
        centerContent = TopAppBarConfig.CenterContent.Logo,
        actions = actions
    )

    /**
     * Detail screen configuration: back button, scroll-to-collapse behavior.
     * Used for content detail screens (event details, venue details, etc.).
     *
     * @param actions Optional composable for right-side actions (share, edit, etc.)
     */
    fun detailScreen(
        actions: (@Composable RowScope.() -> Unit)? = null
    ) = TopAppBarConfig(
        showBackButton = true,
        enableScrollCollapse = true,
        centerContent = TopAppBarConfig.CenterContent.None,
        actions = actions
    )

    /**
     * Title screen configuration: back button with centered title and optional subtitle.
     * Used for list screens, form screens, and other titled content.
     *
     * @param title The title to display in the center
     * @param subtitle Optional subtitle below the title
     * @param actions Optional composable for right-side actions
     */
    fun titleScreen(
        title: String,
        subtitle: String? = null,
        actions: (@Composable RowScope.() -> Unit)? = null
    ) = TopAppBarConfig(
        showBackButton = true,
        centerContent = TopAppBarConfig.CenterContent.Title(title, subtitle),
        actions = actions
    )

    /**
     * Form screen configuration: back button with centered title, hides bottom nav.
     * Used for creation forms, edit screens, and wizards where bottom actions need space.
     *
     * @param title The title to display in the center
     * @param subtitle Optional subtitle below the title
     * @param actions Optional composable for right-side actions
     */
    fun formScreen(
        title: String,
        subtitle: String? = null,
        actions: (@Composable RowScope.() -> Unit)? = null
    ) = TopAppBarConfig(
        showBackButton = true,
        hideBottomNav = true,
        centerContent = TopAppBarConfig.CenterContent.Title(title, subtitle),
        actions = actions
    )

    /**
     * Chat screen configuration: back button, avatar + name in center, hides bottom nav.
     * Used for messaging/conversation screens.
     *
     * @param displayName The name to display next to the avatar
     * @param avatarUrl Optional URL for the avatar image
     */
    fun chatScreen(
        displayName: String,
        avatarUrl: String?
    ) = TopAppBarConfig(
        showBackButton = true,
        hideBottomNav = true,
        centerContent = TopAppBarConfig.CenterContent.Avatar(displayName, avatarUrl)
    )

    /**
     * Search screen configuration: back button with custom center content.
     * Used for screens that need a search field or other custom content in the center.
     *
     * @param centerContent Custom composable for the center area
     * @param actions Optional composable for right-side actions
     */
    fun customCenter(
        centerContent: @Composable () -> Unit,
        actions: (@Composable RowScope.() -> Unit)? = null
    ) = TopAppBarConfig(
        showBackButton = true,
        centerContent = TopAppBarConfig.CenterContent.Custom(centerContent),
        actions = actions
    )
}
