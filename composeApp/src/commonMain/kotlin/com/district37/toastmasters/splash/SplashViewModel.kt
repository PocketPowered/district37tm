package com.district37.toastmasters.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.time.TimeSource

private const val SPLASH_MIN_DURATION_MS = 3000L
private const val SPLASH_OVERRIDE_FETCH_TIMEOUT_MS = 2500L

class SplashViewModel(
    private val splashRepository: SplashRepository
) : ViewModel() {
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _splashImageUrl = MutableStateFlow<String?>(null)
    val splashImageUrl: StateFlow<String?> = _splashImageUrl

    init {
        viewModelScope.launch(Dispatchers.Default) {
            val splashStart = TimeSource.Monotonic.markNow()
            _splashImageUrl.value = splashRepository.getCachedSplashImageUrl()
            val latestImageUrl = try {
                withTimeout(SPLASH_OVERRIDE_FETCH_TIMEOUT_MS) {
                    splashRepository.syncSplashImageUrlFromNetwork()
                }
            } catch (_: TimeoutCancellationException) {
                _splashImageUrl.value
            }
            _splashImageUrl.value = latestImageUrl
            val remainingDelay = SPLASH_MIN_DURATION_MS - splashStart.elapsedNow().inWholeMilliseconds
            if (remainingDelay > 0) {
                delay(remainingDelay)
            }
            _isLoading.value = false
        }
    }
}
