package com.hasibzero.bakikhata.ui.credit

import android.app.DatePickerDialog
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
import com.hasibzero.bakikhata.databinding.FragmentAddCreditBinding
import com.hasibzero.bakikhata.util.DateUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class AddCreditFragment : Fragment() {

    private var _binding: FragmentAddCreditBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AddCreditViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddCreditBinding.inflate(inflater, container, false)
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
        binding.productNameInput.doOnTextChanged { text, _, _, _ ->
            viewModel.onProductNameChanged(text?.toString() ?: "")
        }
        
        binding.quantityInput.doOnTextChanged { text, _, _, _ ->
            viewModel.onQuantityChanged(text?.toString() ?: "")
        }
        
        binding.priceInput.doOnTextChanged { text, _, _, _ ->
            viewModel.onPriceChanged(text?.toString() ?: "")
        }
        
        binding.notesInput.doOnTextChanged { text, _, _, _ ->
            viewModel.onNotesChanged(text?.toString() ?: "")
        }
        
        binding.dateInput.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = viewModel.state.value.date
        
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth)
                viewModel.onDateChanged(selectedCalendar.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun setupButtons() {
        binding.saveButton.setOnClickListener {
            viewModel.saveCreditEntry()
        }
        
        binding.deleteButton.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirm_delete_title)
            .setMessage(R.string.confirm_delete_message)
            .setPositiveButton(R.string.yes) { _, _ ->
                viewModel.deleteCreditEntry()
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
                        getString(R.string.edit_credit)
                    } else {
                        getString(R.string.add_credit)
                    }
                    
                    // Set initial values if in edit mode
                    if (state.isEditMode && binding.productNameInput.text.isNullOrEmpty()) {
                        binding.productNameInput.setText(state.productName)
                        binding.quantityInput.setText(state.quantity)
                        binding.priceInput.setText(state.price)
                        binding.notesInput.setText(state.notes)
                    }
                    
                    // Update total amount
                    binding.totalAmount.setText("%.2f".format(state.totalAmount))
                    
                    // Update date
                    binding.dateInput.setText(DateUtils.formatDate(state.date))
                    
                    // Show/hide delete button
                    binding.deleteButton.isVisible = state.isEditMode
                    
                    // Show error messages
                    binding.productNameInputLayout.error = state.productNameError
                    binding.quantityInputLayout.error = state.quantityError
                    binding.priceInputLayout.error = state.priceError
                }
            }
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is AddCreditEvent.ShowMessage -> {
                            Snackbar.make(binding.root, event.message, Snackbar.LENGTH_SHORT).show()
                        }
                        is AddCreditEvent.NavigateBack -> {
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
