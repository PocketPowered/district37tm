package com.district37.toastmasters.components.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.components.common.EventSidekickTitle
import com.district37.toastmasters.navigation.LocalTopAppBarController
import com.district37.toastmasters.navigation.TopAppBarConfig
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage

/**
 * Floating top bar with split pill design.
 *
 * For root screens (no back button):
 * - Left bubble: User avatar + logo
 * - Right bubble: Actions (search, notifications)
 * - Middle: Empty for maximum screen real estate
 *
 * For detail screens (with back button):
 * - Left: Back button pill
 * - Center: Content based on centerContent type (title, avatar, logo, custom)
 * - Right: Actions pill
 *
 * Design specs:
 * - Two separate floating pills
 * - Semi-transparent surface with 95% opacity
 * - Shadow elevation: 12dp
 * - Tonal elevation: 3dp
 * - Padding: 16dp horizontal, 8dp vertical margins from screen edges
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatingTopBar(
    config: TopAppBarConfig,
    onBackClick: (() -> Unit)?,
    userAvatarUrl: String? = null,
    onAvatarClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val controller = LocalTopAppBarController.current
    val density = LocalDensity.current

    // Measure status bar height
    val statusBarHeight = with(density) {
        WindowInsets.statusBars.getTop(density).toDp()
    }

    Box(
        modifier = modifier.onGloballyPositioned { coordinates ->
            with(density) {
                val totalHeight = coordinates.size.height.toDp()
                // Content height = total height - status bar - vertical padding (8dp * 2)
                val contentHeight = totalHeight - statusBarHeight - 16.dp

                controller.updateInsets(
                    statusBarHeight = statusBarHeight,
                    topBarContentHeight = contentHeight
                )
            }
        }
    ) {
        if (config.showBackButton) {
            DetailFloatingTopBar(
                config = config,
                onBackClick = onBackClick,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            RootFloatingTopBar(
                config = config,
                userAvatarUrl = if (config.showUserAvatar) userAvatarUrl else null,
                onAvatarClick = onAvatarClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Root screen floating top bar with split bubbles.
 * Left bubble: Avatar + Logo (primary content) - compact, left-aligned
 * Right bubble: Actions like search, notifications (secondary content) - right-aligned
 * Middle: Empty space for maximum screen real estate
 */
@Composable
private fun RootFloatingTopBar(
    config: TopAppBarConfig,
    userAvatarUrl: String?,
    onAvatarClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Left bubble: Avatar + Logo (primary) - aligned to start, max width clamped
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            shadowElevation = 12.dp,
            tonalElevation = 3.dp,
            modifier = Modifier.align(Alignment.CenterStart).widthIn(max = 180.dp)
        ) {
            Row(
                modifier = Modifier.padding(
                    start = if (!userAvatarUrl.isNullOrBlank()) 6.dp else 12.dp,
                    end = 12.dp,
                    top = 6.dp,
                    bottom = 6.dp
                ),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User avatar (only if showUserAvatar is true and URL is available)
                if (!userAvatarUrl.isNullOrBlank()) {
                    CoilImage(
                        imageModel = { userAvatarUrl },
                        imageOptions = ImageOptions(
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center
                        ),
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .then(
                                if (onAvatarClick != null) {
                                    Modifier.clickable(onClick = onAvatarClick)
                                } else {
                                    Modifier
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // App logo
                EventSidekickTitle()
            }
        }

        // Right bubble: Actions (secondary) - aligned to end
        config.actions?.let { actions ->
            ActionsPill(
                actions = actions,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

/**
 * Detail screen floating top bar (back button + center content + actions)
 */
@Composable
private fun DetailFloatingTopBar(
    config: TopAppBarConfig,
    onBackClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Back button in pill
        BackButtonPill(onBackClick = onBackClick)

        // Center: Content based on centerContent type
        CenterContentPill(centerContent = config.centerContent)

        // Right: Actions in pill or spacer for balance
        config.actions?.let { actions ->
            ActionsPill(actions = actions)
        } ?: Spacer(modifier = Modifier.width(44.dp)) // Balance the back button
    }
}

/**
 * Back button rendered in a floating pill.
 */
@Composable
private fun BackButtonPill(
    onBackClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shadowElevation = 12.dp,
        tonalElevation = 3.dp,
        modifier = modifier
    ) {
        IconButton(
            onClick = { onBackClick?.invoke() },
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Actions rendered in a floating pill.
 */
@Composable
private fun ActionsPill(
    actions: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shadowElevation = 12.dp,
        tonalElevation = 3.dp,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            actions(this)
        }
    }
}

/**
 * Center content pill that renders based on the CenterContent type.
 */
@Composable
private fun CenterContentPill(
    centerContent: TopAppBarConfig.CenterContent,
    modifier: Modifier = Modifier
) {
    when (centerContent) {
        is TopAppBarConfig.CenterContent.None -> {
            // Empty space for detail screens - no center element
            Spacer(modifier = modifier.width(1.dp))
        }

        is TopAppBarConfig.CenterContent.Logo -> {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shadowElevation = 12.dp,
                tonalElevation = 3.dp,
                modifier = modifier
            ) {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    EventSidekickTitle()
                }
            }
        }

        is TopAppBarConfig.CenterContent.Title -> {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shadowElevation = 12.dp,
                tonalElevation = 3.dp,
                modifier = modifier
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = centerContent.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    centerContent.subtitle?.let { subtitle ->
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        is TopAppBarConfig.CenterContent.Avatar -> {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shadowElevation = 12.dp,
                tonalElevation = 3.dp,
                modifier = modifier
            ) {
                Row(
                    modifier = Modifier.padding(
                        start = if (!centerContent.avatarUrl.isNullOrBlank()) 6.dp else 16.dp,
                        end = 16.dp,
                        top = 6.dp,
                        bottom = 6.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!centerContent.avatarUrl.isNullOrBlank()) {
                        CoilImage(
                            imageModel = { centerContent.avatarUrl },
                            imageOptions = ImageOptions(
                                contentScale = ContentScale.Crop,
                                alignment = Alignment.Center
                            ),
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = centerContent.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        is TopAppBarConfig.CenterContent.Custom -> {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shadowElevation = 12.dp,
                tonalElevation = 3.dp,
                modifier = modifier
            ) {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    centerContent.content()
                }
            }
        }
    }
}
