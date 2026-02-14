package com.hasibzero.bakikhata.ui.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.hasibzero.bakikhata.R
import com.hasibzero.bakikhata.databinding.FragmentAddCustomerBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddCustomerFragment : Fragment() {

    private var _binding: FragmentAddCustomerBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AddCustomerViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddCustomerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupInputs()
        setupButtons()
        observeState()
        observeEvents()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupInputs() {
        binding.nameInput.doOnTextChanged { text, _, _, _ ->
            viewModel.onNameChanged(text?.toString() ?: "")
        }
        
        binding.phoneInput.doOnTextChanged { text, _, _, _ ->
            viewModel.onPhoneNumberChanged(text?.toString() ?: "")
        }
        
        binding.addressInput.doOnTextChanged { text, _, _, _ ->
            viewModel.onAddressChanged(text?.toString() ?: "")
        }
    }

    private fun setupButtons() {
        binding.saveButton.setOnClickListener {
            viewModel.saveCustomer()
        }
        
        binding.deleteButton.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirm_delete_title)
            .setMessage(R.string.delete_customer_confirmation)
            .setPositiveButton(R.string.yes) { _, _ ->
                viewModel.deleteCustomer()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    binding.progressBar.isVisible = state.isLoading
                    
                    // Update toolbar title
                    binding.toolbar.title = if (state.isEditMode) {
                        getString(R.string.edit_customer)
                    } else {
                        getString(R.string.add_customer)
                    }
                    
                    // Set initial values if in edit mode
                    if (state.isEditMode && binding.nameInput.text.isNullOrEmpty()) {
                        binding.nameInput.setText(state.name)
                        binding.phoneInput.setText(state.phoneNumber)
                        binding.addressInput.setText(state.address)
                    }
                    
                    // Show/hide delete button
                    binding.deleteButton.isVisible = state.isEditMode
                    
                    // Show error messages
                    binding.nameInputLayout.error = state.nameError
                }
            }
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is AddCustomerEvent.ShowMessage -> {
                            Snackbar.make(binding.root, event.message, Snackbar.LENGTH_SHORT).show()
                        }
                        is AddCustomerEvent.NavigateBack -> {
                            findNavController().navigateUp()
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
