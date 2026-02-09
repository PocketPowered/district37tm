package com.district37.toastmasters.features.account

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.components.common.ColorPicker
import com.district37.toastmasters.navigation.AppBarConfigs
import com.district37.toastmasters.navigation.ConfigureTopAppBar
import com.district37.toastmasters.navigation.LocalTopAppBarInsets
import com.district37.toastmasters.util.toImageBitmap
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberCameraPickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch

/**
 * Edit Profile screen allowing users to modify their display name, bio, and profile picture
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: EditProfileViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showImageOptionsSheet by remember { mutableStateOf(false) }
    var showUrlInputSheet by remember { mutableStateOf(false) }
    var tempImageUrl by remember { mutableStateOf("") }
    var selectedImageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val urlSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    // Gallery picker launcher using FileKit
    val galleryLauncher = rememberFilePickerLauncher(
        type = FileKitType.Image
    ) { platformFile ->
        platformFile?.let { file ->
            scope.launch {
                // Read bytes using FileKit extension
                val bytes = file.readBytes()
                // Convert to ImageBitmap for display
                selectedImageBitmap = bytes.toImageBitmap()
                viewModel.onImageSelected(bytes)
            }
        }
    }

    // Camera launcher using FileKit - launches system camera app
    val cameraLauncher = rememberCameraPickerLauncher { platformFile ->
        platformFile?.let { file ->
            scope.launch {
                // Read bytes using FileKit extension
                val bytes = file.readBytes()
                // Convert to ImageBitmap for display
                selectedImageBitmap = bytes.toImageBitmap()
                viewModel.onImageSelected(bytes)
            }
        }
    }

    // Handle save success - navigate back
    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            viewModel.resetSaveSuccess()
            onNavigateBack()
        }
    }

    // Show error in snackbar
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    // Sync temp URL with state when URL sheet opens
    LaunchedEffect(showUrlInputSheet) {
        if (showUrlInputSheet) {
            tempImageUrl = state.profileImageUrl
        }
    }

    // Clear the local bitmap when image bytes are cleared (e.g., after successful save)
    LaunchedEffect(state.selectedImageBytes) {
        if (state.selectedImageBytes == null) {
            selectedImageBitmap = null
        }
    }

    // Configure the root scaffold's top app bar (formScreen hides bottom nav for CTA)
    ConfigureTopAppBar(
        config = AppBarConfigs.formScreen(title = "Edit Profile"),
        onBackClick = onNavigateBack
    )

    val topBarInsets = LocalTopAppBarInsets.current

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(topBarInsets.recommendedContentPadding))

            // Profile Picture with edit overlay
            Box(
                contentAlignment = Alignment.Center
            ) {
                EditableProfileAvatar(
                    imageUrl = state.profileImageUrl,
                    selectedImageBitmap = selectedImageBitmap ?: state.selectedImageBytes?.toImageBitmap(),
                    displayName = state.displayName,
                    isUploading = state.isUploadingImage,
                    onClick = { showImageOptionsSheet = true },
                    enabled = !state.isSaving && !state.isUploadingImage
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = { showImageOptionsSheet = true },
                enabled = !state.isSaving && !state.isUploadingImage
            ) {
                if (state.isUploadingImage) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Uploading...")
                } else {
                    Text("Change Photo")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Display Name field
            OutlinedTextField(
                value = state.displayName,
                onValueChange = viewModel::onDisplayNameChanged,
                label = { Text("Display Name") },
                placeholder = { Text("Enter your display name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !state.isSaving
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Username field
            OutlinedTextField(
                value = state.username,
                onValueChange = viewModel::onUsernameChanged,
                label = { Text("Username") },
                placeholder = { Text("username") },
                prefix = { Text("@") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !state.isSaving,
                isError = state.usernameError != null,
                supportingText = {
                    val error = state.usernameError
                    when {
                        error != null -> Text(error)
                        state.isCheckingUsername -> Text("Checking availability...")
                        state.isUsernameAvailable == true -> Text("Username is available!", color = MaterialTheme.colorScheme.primary)
                        state.username.isBlank() -> Text("3-18 characters: lowercase, numbers, underscores")
                        else -> Text("3-18 characters: lowercase, numbers, underscores")
                    }
                },
                trailingIcon = {
                    when {
                        state.isCheckingUsername -> CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        state.isUsernameAvailable == true -> Icon(
                            Icons.Default.Person,
                            contentDescription = "Available",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Bio field
            OutlinedTextField(
                value = state.bio,
                onValueChange = viewModel::onBioChanged,
                label = { Text("Bio") },
                placeholder = { Text("Tell us about yourself") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
                enabled = !state.isSaving
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Share a little about yourself with other event attendees",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Profile Colors Section
            Text(
                text = "Profile Colors",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Customize your profile's appearance",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            ColorPicker(
                selectedColor = state.primaryColor,
                onColorSelected = viewModel::onPrimaryColorChanged,
                label = "Banner Color"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // # TODO SECONDARY BACKGROUND COLOR DOES NOT LOOK GOOD YET, MAYBE CONSIDER DOING GRADIENT
//            ColorPicker(
//                selectedColor = state.secondaryColor,
//                onColorSelected = viewModel::onSecondaryColorChanged,
//                label = "Background Color"
//            )
//
//            Spacer(modifier = Modifier.height(32.dp))

            // Save Button
            Button(
                onClick = viewModel::saveProfile,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSaving && !state.isUploadingImage
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (state.isUploadingImage) "Uploading..." else "Saving...")
                } else {
                    Text("Save Changes")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Snackbar host positioned at the bottom of the Box
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // Bottom sheet for image options (Gallery, Camera, URL)
    if (showImageOptionsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showImageOptionsSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Change Profile Picture",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Choose from Gallery
                ImageOptionRow(
                    icon = Icons.Default.PhotoLibrary,
                    text = "Choose from Gallery",
                    onClick = {
                        scope.launch {
                            sheetState.hide()
                            showImageOptionsSheet = false
                        }
                        galleryLauncher.launch()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Take Photo - launches system camera
                ImageOptionRow(
                    icon = Icons.Default.CameraAlt,
                    text = "Take Photo",
                    onClick = {
                        scope.launch {
                            sheetState.hide()
                            showImageOptionsSheet = false
                        }
                        cameraLauncher.launch()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Enter URL
                ImageOptionRow(
                    icon = Icons.Default.Link,
                    text = "Enter Image URL",
                    onClick = {
                        scope.launch {
                            sheetState.hide()
                            showImageOptionsSheet = false
                        }
                        showUrlInputSheet = true
                    }
                )

                // Remove Photo option (only show if there's an existing photo)
                if (state.profileImageUrl.isNotBlank() || state.selectedImageBytes != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = {
                            viewModel.onProfileImageUrlChanged("")
                            viewModel.clearSelectedImage()
                            selectedImageBitmap = null
                            scope.launch {
                                sheetState.hide()
                                showImageOptionsSheet = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Remove Photo",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }

    // Bottom sheet for URL input
    if (showUrlInputSheet) {
        ModalBottomSheet(
            onDismissRequest = { showUrlInputSheet = false },
            sheetState = urlSheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Enter Image URL",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = tempImageUrl,
                    onValueChange = { tempImageUrl = it },
                    label = { Text("Image URL") },
                    placeholder = { Text("https://example.com/photo.jpg") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Preview
                if (tempImageUrl.isNotBlank() && (tempImageUrl.startsWith("http://") || tempImageUrl.startsWith("https://"))) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .align(Alignment.CenterHorizontally),
                        contentAlignment = Alignment.Center
                    ) {
                        CoilImage(
                            imageModel = { tempImageUrl },
                            imageOptions = ImageOptions(
                                contentScale = ContentScale.Crop,
                                alignment = Alignment.Center
                            ),
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Button(
                    onClick = {
                        viewModel.onProfileImageUrlChanged(tempImageUrl)
                        selectedImageBitmap = null
                        scope.launch {
                            urlSheetState.hide()
                            showUrlInputSheet = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Use This URL")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            urlSheetState.hide()
                            showUrlInputSheet = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

/**
 * Row item for image options in the bottom sheet
 */
@Composable
private fun ImageOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * Editable profile avatar with camera icon overlay
 */
@Composable
private fun EditableProfileAvatar(
    imageUrl: String,
    selectedImageBitmap: ImageBitmap?,
    displayName: String,
    isUploading: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(120.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Avatar image (prioritize selected device image over URL)
        when {
            selectedImageBitmap != null -> {
                Image(
                    bitmap = selectedImageBitmap,
                    contentDescription = "Selected profile picture",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            imageUrl.isNotBlank() -> {
                CoilImage(
                    imageModel = { imageUrl },
                    imageOptions = ImageOptions(
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center
                    ),
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    failure = {
                        AvatarFallback(displayName = displayName)
                    },
                    loading = {
                        AvatarFallback(displayName = displayName)
                    }
                )
            }
            else -> {
                AvatarFallback(displayName = displayName)
            }
        }

        // Camera icon overlay (or loading indicator)
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    strokeWidth = 3.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Change photo",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

/**
 * Fallback avatar showing initials or default icon
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
            .size(120.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (initials == "?") {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "User",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        } else {
            Text(
                text = initials,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
