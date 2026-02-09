package com.district37.toastmasters.features.account

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.district37.toastmasters.auth.AuthViewModel
import com.district37.toastmasters.auth.CountryCode
import com.district37.toastmasters.auth.CountryCodes
import com.district37.toastmasters.auth.models.LoginState
import com.district37.toastmasters.auth.models.OtpCredentialType
import com.district37.toastmasters.auth.models.OtpLoginState
import com.district37.toastmasters.components.auth.CountryCodePicker
import com.district37.toastmasters.navigation.ConfigureTopAppBar
import com.district37.toastmasters.navigation.TopAppBarConfig
import com.district37.toastmasters.util.PlatformType
import com.district37.toastmasters.util.currentPlatform
import district37toastmasters.composeapp.generated.resources.Res
import district37toastmasters.composeapp.generated.resources.gatherlogo
import org.jetbrains.compose.resources.painterResource

/**
 * Redesigned Login screen matching the "Gather" mockup.
 * Supports email, phone (with country code picker), and Google OAuth.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLongPressLogo: () -> Unit = {}
) {
    val otpLoginState by authViewModel.otpLoginState.collectAsState()
    val loginState by authViewModel.loginState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Track the last awaiting state for use during VerifyingOtp
    var lastAwaitingState by remember { mutableStateOf<OtpLoginState.AwaitingOtpVerification?>(null) }

    // Update lastAwaitingState when we enter AwaitingOtpVerification
    LaunchedEffect(otpLoginState) {
        if (otpLoginState is OtpLoginState.AwaitingOtpVerification) {
            lastAwaitingState = otpLoginState as OtpLoginState.AwaitingOtpVerification
        } else if (otpLoginState is OtpLoginState.EnteringCredential) {
            lastAwaitingState = null
        }
    }

    // Show error in snackbar for Google login errors
    LaunchedEffect(loginState) {
        if (loginState is LoginState.Error) {
            snackbarHostState.showSnackbar((loginState as LoginState.Error).message)
            authViewModel.resetLoginState()
        }
    }

    // Show OTP verification screen when awaiting code
    when (val state = otpLoginState) {
        is OtpLoginState.AwaitingOtpVerification -> {
            OtpVerificationScreen(
                credential = state.credential,
                credentialType = state.credentialType,
                isLoading = false,
                error = null,
                onVerify = { otp -> authViewModel.verifyOtp(otp) },
                onResend = { authViewModel.resendOtp() },
                onBack = { authViewModel.resetOtpState() }
            )
            return
        }
        is OtpLoginState.VerifyingOtp -> {
            // Use the tracked lastAwaitingState for credentials during verification
            lastAwaitingState?.let { prevState ->
                OtpVerificationScreen(
                    credential = prevState.credential,
                    credentialType = prevState.credentialType,
                    isLoading = true,
                    error = null,
                    onVerify = {},
                    onResend = {},
                    onBack = {}
                )
                return
            }
        }
        is OtpLoginState.Error -> {
            val prevState = state.previousState
            if (prevState is OtpLoginState.AwaitingOtpVerification) {
                OtpVerificationScreen(
                    credential = prevState.credential,
                    credentialType = prevState.credentialType,
                    isLoading = false,
                    error = state.message,
                    onVerify = { otp -> authViewModel.verifyOtp(otp) },
                    onResend = { authViewModel.resendOtp() },
                    onBack = { authViewModel.resetOtpState() }
                )
                return
            }
        }
        else -> {}
    }

    // Hide the floating top bar on the login screen
    ConfigureTopAppBar(TopAppBarConfig.Hidden)

    // Main login screen
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // App Logo
            Image(
                painter = painterResource(Res.drawable.gatherlogo),
                contentDescription = "Gather",
                modifier = Modifier.height(48.dp),
                contentScale = ContentScale.FillHeight
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Create an account heading
            Text(
                text = "Create an account",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "Enter your email or phone to sign up for this app",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Email/Phone Input with smart detection
            EmailPhoneInput(
                onSubmit = { credential, type ->
                    if (type == OtpCredentialType.EMAIL) {
                        authViewModel.sendEmailOtp(credential)
                    } else {
                        authViewModel.sendPhoneOtp(credential)
                    }
                },
                isLoading = otpLoginState is OtpLoginState.SendingOtp,
                error = (otpLoginState as? OtpLoginState.Error)?.let {
                    if (it.previousState is OtpLoginState.EnteringCredential ||
                        it.previousState is OtpLoginState.SendingOtp) {
                        it.message
                    } else null
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // OR Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = "or",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Continue with Google
            GoogleSignInButton(
                onClick = { authViewModel.startGoogleLogin() },
                isLoading = loginState == LoginState.Loading
            )

            // Continue with Apple (iOS only - not yet implemented)
            if (currentPlatform == PlatformType.IOS) {
                Spacer(modifier = Modifier.height(12.dp))

                AppleSignInButton(
                    onClick = { /* Not implemented */ },
                    enabled = false
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Terms and Privacy add back when there is a Terms of Service link
//            Text(
//                text = "By clicking continue, you agree to our Terms of Service and Privacy Policy",
//                style = MaterialTheme.typography.bodySmall,
//                color = MaterialTheme.colorScheme.onSurfaceVariant,
//                textAlign = TextAlign.Center,
//                modifier = Modifier.padding(bottom = 32.dp)
//            )
        }
    }
}

@Composable
private fun EmailPhoneInput(
    onSubmit: (String, OtpCredentialType) -> Unit,
    isLoading: Boolean,
    error: String?
) {
    var input by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf(CountryCodes.default) }
    var isPhoneMode by remember { mutableStateOf(false) }

    // Detect if input looks like a phone number
    LaunchedEffect(input) {
        // Consider it phone mode if:
        // - Input starts with digits and contains only digits/spaces/dashes
        // - OR input contains only digits and is at least 3 characters
        val cleanedInput = input.filter { it.isDigit() || it == ' ' || it == '-' || it == '+' }
        isPhoneMode = input.isNotEmpty() &&
                cleanedInput == input &&
                input.any { it.isDigit() }
    }

    Column {
        if (isPhoneMode) {
            // Phone input with country code picker
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    CountryCodePicker(
                        selectedCountry = selectedCountry,
                        onCountrySelected = { selectedCountry = it }
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("Phone number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading,
                    isError = error != null
                )
            }
        } else {
            // Email input
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text("email@domain.com") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                isError = error != null
            )
        }

        if (error != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Continue Button
        Button(
            onClick = {
                val credential = if (isPhoneMode) {
                    // Combine country code with phone number (digits only)
                    "${selectedCountry.dialCode}${input.filter { it.isDigit() }}"
                } else {
                    input.trim()
                }
                val type = if (isPhoneMode) OtpCredentialType.PHONE else OtpCredentialType.EMAIL
                onSubmit(credential, type)
            },
            enabled = input.isNotBlank() && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Continue")
            }
        }
    }
}

/**
 * Google Sign-In button following Google's branding guidelines.
 */
@Composable
private fun GoogleSignInButton(
    onClick: () -> Unit,
    isLoading: Boolean
) {
    OutlinedButton(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
        } else {
            // Google "G" logo
            GoogleLogo(modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text("Continue with Google", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/**
 * Apple Sign-In button (currently disabled/not implemented)
 */
@Composable
private fun AppleSignInButton(
    onClick: () -> Unit,
    enabled: Boolean
) {
    val contentColor = if (enabled) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    }

    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        ),
        border = BorderStroke(
            1.dp,
            if (enabled) MaterialTheme.colorScheme.outline
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.38f)
        )
    ) {
        // Apple logo
        Text(
            text = "",  // Apple logo Unicode character
            fontWeight = FontWeight.Bold,
            color = contentColor,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text("Continue with Apple", color = contentColor)
    }
}

/**
 * Simple Google "G" logo representation using colored text.
 */
@Composable
private fun GoogleLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "G",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4285F4) // Google Blue
        )
    }
}
