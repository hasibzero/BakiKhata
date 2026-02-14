package com.hasibzero.bakikhata.ui.security

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SecurityState(
    val isLoading: Boolean = false,
    val pin: String = "",
    val errorMessage: String? = null,
    val isFingerprintAvailable: Boolean = false
)

sealed class SecurityEvent {
    object UnlockSuccess : SecurityEvent()
    data class UnlockFailed(val message: String) : SecurityEvent()
    object ShowFingerprintPrompt : SecurityEvent()
}

@HiltViewModel
class SecurityViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(SecurityState())
    val state: StateFlow<SecurityState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<SecurityEvent>()
    val events: SharedFlow<SecurityEvent> = _events.asSharedFlow()

    private val sharedPrefs = context.getSharedPreferences("security_prefs", Context.MODE_PRIVATE)

    init {
        checkFingerprintAvailability()
    }

    private fun checkFingerprintAvailability() {
        val isFingerprintEnabled = sharedPrefs.getBoolean("fingerprint_enabled", false)
        // In real app, check if biometric hardware is available
        _state.value = _state.value.copy(isFingerprintAvailable = isFingerprintEnabled)
    }

    fun onPinChanged(pin: String) {
        _state.value = _state.value.copy(pin = pin, errorMessage = null)
    }

    fun verifyPin() {
        val enteredPin = _state.value.pin
        val savedPin = sharedPrefs.getString("pin_code", "1234") // Default PIN for testing
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            // Simulate verification delay
            kotlinx.coroutines.delay(300)
            
            if (enteredPin == savedPin) {
                _events.emit(SecurityEvent.UnlockSuccess)
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    pin = "",
                    errorMessage = "Wrong PIN. Try again."
                )
                _events.emit(SecurityEvent.UnlockFailed("Wrong PIN"))
            }
        }
    }

    fun requestFingerprint() {
        viewModelScope.launch {
            _events.emit(SecurityEvent.ShowFingerprintPrompt)
        }
    }

    fun onFingerprintSuccess() {
        viewModelScope.launch {
            _events.emit(SecurityEvent.UnlockSuccess)
        }
    }

    fun onFingerprintFailed() {
        viewModelScope.launch {
            _events.emit(SecurityEvent.UnlockFailed("Fingerprint not recognized"))
        }
    }
}
