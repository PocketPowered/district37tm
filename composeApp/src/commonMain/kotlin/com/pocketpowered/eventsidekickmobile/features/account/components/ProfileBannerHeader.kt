package com.district37.toastmasters.features.account.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage

/**
 * Profile banner header with Twitter-style layout:
 * - Full-width banner image (or gradient placeholder)
 * - Profile photo overlapping the banner
 * - User info below
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileBannerHeader(
    bannerImageUrl: String?,
    profileImageUrl: String?,
    displayName: String,
    username: String?,
    memberSince: String?,
    bio: String?,
    onLongPressAvatar: () -> Unit,
    primaryColor: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Banner with overlapping profile photo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(224.dp) // 180dp banner + 44dp for overlapping avatar
        ) {
            // Banner image or gradient placeholder
            BannerImage(
                bannerImageUrl = bannerImageUrl,
                primaryColor = primaryColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            // Profile photo overlapping the banner
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
            ) {
                ProfileAvatar(
                    avatarUrl = profileImageUrl,
                    displayName = displayName,
                    onLongPress = onLongPressAvatar,
                    modifier = Modifier.size(88.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Display name
        Text(
            text = displayName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // Username
        if (!username.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "@$username",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Member since
        if (!memberSince.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Member since $memberSince",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Bio
        if (!bio.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = bio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

/**
 * Banner image with gradient placeholder fallback
 */
@Composable
private fun BannerImage(
    bannerImageUrl: String?,
    primaryColor: String?,
    modifier: Modifier = Modifier
) {
    if (!bannerImageUrl.isNullOrBlank()) {
        CoilImage(
            imageModel = { bannerImageUrl },
            imageOptions = ImageOptions(
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center
            ),
            modifier = modifier,
            failure = {
                BannerPlaceholder(primaryColor = primaryColor, modifier = modifier)
            }
        )
    } else {
        BannerPlaceholder(primaryColor = primaryColor, modifier = modifier)
    }
}

/**
 * Gradient placeholder for banner when no image is available.
 * Uses custom primary color if provided, otherwise falls back to theme colors.
 */
@Composable
private fun BannerPlaceholder(
    primaryColor: String?,
    modifier: Modifier = Modifier
) {
    val customColor = primaryColor?.let { hexToColor(it) }

    Box(
        modifier = modifier.background(
            if (customColor != null) {
                // Use custom color with a subtle gradient to a darker shade
                Brush.linearGradient(
                    colors = listOf(
                        customColor,
                        customColor.copy(alpha = 0.8f)
                    )
                )
            } else {
                // Default theme gradient
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            }
        )
    )
}

/**
 * Convert a hex color string to a Compose Color.
 * Supports formats: "#RRGGBB" or "RRGGBB"
 */
private fun hexToColor(hex: String): Color? {
    val cleanHex = hex.removePrefix("#")
    return try {
        Color(("FF$cleanHex").toLong(16))
    } catch (_: Exception) {
        null
    }
}

/**
 * Profile avatar with border and long-press support
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProfileAvatar(
    avatarUrl: String?,
    displayName: String,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Outer box for the white border effect
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(4.dp) // 4dp border
            .combinedClickable(
                onClick = {},
                onLongClick = onLongPress
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!avatarUrl.isNullOrBlank()) {
            CoilImage(
                imageModel = { avatarUrl },
                imageOptions = ImageOptions(
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                failure = {
                    AvatarFallback(displayName = displayName)
                },
                loading = {
                    AvatarFallback(displayName = displayName)
                }
            )
        } else {
            AvatarFallback(displayName = displayName)
        }
    }
}

/**
 * Fallback avatar showing user initials
 */
@Composable
private fun AvatarFallback(
    displayName: String,
    modifier: Modifier = Modifier
) {
    val initials = displayName
        .split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifEmpty { "?" }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (initials == "?") {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "User",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        } else {
            Text(
                text = initials,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
