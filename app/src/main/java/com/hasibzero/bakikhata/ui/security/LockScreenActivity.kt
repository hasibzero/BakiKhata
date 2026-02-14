package com.hasibzero.bakikhata.ui.security

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.hasibzero.bakikhata.databinding.ActivityLockScreenBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LockScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLockScreenBinding
    private val viewModel: SecurityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLockScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupInputs()
        setupButtons()
        observeState()
        observeEvents()
    }

    private fun setupInputs() {
        binding.pinInput.doOnTextChanged { text, _, _, _ ->
            viewModel.onPinChanged(text?.toString() ?: "")
            
            // Auto-verify when PIN is 4 digits
            if (text?.length == 4) {
                viewModel.verifyPin()
            }
        }
    }

    private fun setupButtons() {
        binding.unlockButton.setOnClickListener {
            viewModel.verifyPin()
        }
        
        binding.fingerprintButton.setOnClickListener {
            viewModel.requestFingerprint()
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    binding.fingerprintButton.isVisible = state.isFingerprintAvailable
                    
                    if (state.errorMessage != null) {
                        binding.errorText.isVisible = true
                        binding.errorText.text = state.errorMessage
                        binding.pinInput.setText("")
                    } else {
                        binding.errorText.isVisible = false
                    }
                }
            }
        }
    }

    private fun observeEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is SecurityEvent.UnlockSuccess -> {
                            finish()
                        }
                        is SecurityEvent.UnlockFailed -> {
                            Snackbar.make(binding.root, event.message, Snackbar.LENGTH_SHORT).show()
                        }
                        is SecurityEvent.ShowFingerprintPrompt -> {
                            // In real app, show biometric prompt
                            Snackbar.make(binding.root, "Fingerprint authentication would be shown here", Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        // Prevent going back without unlocking
        // User must unlock or close the app
        moveTaskToBack(true)
    }
}
