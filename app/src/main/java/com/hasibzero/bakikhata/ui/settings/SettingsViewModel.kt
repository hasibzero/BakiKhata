package com.hasibzero.bakikhata.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasibzero.bakikhata.util.BackupManager
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

data class SettingsState(
    val isLoading: Boolean = false,
    val isPinEnabled: Boolean = false,
    val isFingerprintEnabled: Boolean = false,
    val selectedTheme: String = "System Default",
    val appVersion: String = "1.0.0"
)

sealed class SettingsEvent {
    data class ShowMessage(val message: String) : SettingsEvent()
    object ShowPinSetup : SettingsEvent()
    object ShowThemeDialog : SettingsEvent()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    private val sharedPrefs = context.getSharedPreferences("security_prefs", Context.MODE_PRIVATE)

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val isPinEnabled = sharedPrefs.getBoolean("pin_enabled", false)
        val isFingerprintEnabled = sharedPrefs.getBoolean("fingerprint_enabled", false)
        val theme = sharedPrefs.getString("theme", "System Default") ?: "System Default"
        
        _state.value = _state.value.copy(
            isPinEnabled = isPinEnabled,
            isFingerprintEnabled = isFingerprintEnabled,
            selectedTheme = theme
        )
    }

    fun togglePinLock(enabled: Boolean) {
        if (enabled) {
            viewModelScope.launch {
                _events.emit(SettingsEvent.ShowPinSetup)
            }
        } else {
            sharedPrefs.edit().putBoolean("pin_enabled", false).apply()
            _state.value = _state.value.copy(isPinEnabled = false)
        }
    }

    fun setPinEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("pin_enabled", enabled).apply()
        _state.value = _state.value.copy(isPinEnabled = enabled)
    }

    fun toggleFingerprint(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("fingerprint_enabled", enabled).apply()
        _state.value = _state.value.copy(isFingerprintEnabled = enabled)
    }

    fun showThemeDialog() {
        viewModelScope.launch {
            _events.emit(SettingsEvent.ShowThemeDialog)
        }
    }

    fun setTheme(theme: String) {
        sharedPrefs.edit().putString("theme", theme).apply()
        _state.value = _state.value.copy(selectedTheme = theme)
        // Apply theme change would be handled in the fragment
    }

    fun backupData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            try {
                val backupFile = BackupManager.createBackup(context)
                if (backupFile != null) {
                    _events.emit(SettingsEvent.ShowMessage("Backup completed successfully"))
                } else {
                    _events.emit(SettingsEvent.ShowMessage("Error creating backup"))
                }
            } catch (e: Exception) {
                _events.emit(SettingsEvent.ShowMessage("Error creating backup: ${e.message}"))
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun restoreData(backupFile: java.io.File) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            try {
                val success = BackupManager.restoreBackup(context, backupFile)
                if (success) {
                    _events.emit(SettingsEvent.ShowMessage("Restore completed successfully"))
                } else {
                    _events.emit(SettingsEvent.ShowMessage("Error restoring data"))
                }
            } catch (e: Exception) {
                _events.emit(SettingsEvent.ShowMessage("Error restoring data: ${e.message}"))
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
}
