package com.hasibzero.bakikhata.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.hasibzero.bakikhata.R
import com.hasibzero.bakikhata.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupListeners()
        observeState()
        observeEvents()
    }

    private fun setupListeners() {
        // Security settings
        binding.pinLockSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.togglePinLock(isChecked)
        }
        
        binding.fingerprintSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleFingerprint(isChecked)
        }
        
        binding.changePinSetting.setOnClickListener {
            // Show PIN change dialog
            showPinSetupDialog(true)
        }
        
        // Backup & Restore
        binding.backupSetting.setOnClickListener {
            viewModel.backupData()
        }
        
        binding.restoreSetting.setOnClickListener {
            showRestoreConfirmation()
        }
        
        // Theme
        binding.themeSetting.setOnClickListener {
            viewModel.showThemeDialog()
        }
    }

    private fun showPinSetupDialog(isChange: Boolean = false) {
        // In a real app, this would be a proper PIN entry dialog or activity
        // For now, just a simple dialog
        AlertDialog.Builder(requireContext())
            .setTitle(if (isChange) "Change PIN" else "Set PIN")
            .setMessage("PIN setup would be implemented here")
            .setPositiveButton("OK") { _, _ ->
                if (!isChange) {
                    viewModel.setPinEnabled(true)
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                // Reset switch if cancelled
                if (!isChange) {
                    binding.pinLockSwitch.isChecked = false
                }
            }
            .show()
    }

    private fun showRestoreConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Restore Data")
            .setMessage("This will replace all current data with the backup. Continue?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.restoreData()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun showThemeDialog() {
        val themes = arrayOf("Light Mode", "Dark Mode", "System Default")
        val currentTheme = viewModel.state.value.selectedTheme
        val checkedItem = themes.indexOf(currentTheme)
        
        AlertDialog.Builder(requireContext())
            .setTitle("Select Theme")
            .setSingleChoiceItems(themes, checkedItem) { dialog, which ->
                val selectedTheme = themes[which]
                viewModel.setTheme(selectedTheme)
                applyTheme(selectedTheme)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun applyTheme(theme: String) {
        val mode = when (theme) {
            "Light Mode" -> AppCompatDelegate.MODE_NIGHT_NO
            "Dark Mode" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    binding.progressBar.isVisible = state.isLoading
                    
                    // Update switches without triggering listeners
                    binding.pinLockSwitch.setOnCheckedChangeListener(null)
                    binding.pinLockSwitch.isChecked = state.isPinEnabled
                    binding.pinLockSwitch.setOnCheckedChangeListener { _, isChecked ->
                        viewModel.togglePinLock(isChecked)
                    }
                    
                    binding.fingerprintSwitch.setOnCheckedChangeListener(null)
                    binding.fingerprintSwitch.isChecked = state.isFingerprintEnabled
                    binding.fingerprintSwitch.setOnCheckedChangeListener { _, isChecked ->
                        viewModel.toggleFingerprint(isChecked)
                    }
                    
                    binding.themeValue.text = state.selectedTheme
                    binding.versionText.text = state.appVersion
                }
            }
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is SettingsEvent.ShowMessage -> {
                            Snackbar.make(binding.root, event.message, Snackbar.LENGTH_SHORT).show()
                        }
                        is SettingsEvent.ShowPinSetup -> {
                            showPinSetupDialog(false)
                        }
                        is SettingsEvent.ShowThemeDialog -> {
                            showThemeDialog()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
