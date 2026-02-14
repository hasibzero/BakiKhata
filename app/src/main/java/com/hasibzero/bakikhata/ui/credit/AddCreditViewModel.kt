package com.hasibzero.bakikhata.ui.credit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasibzero.bakikhata.data.db.entity.CreditEntry
import com.hasibzero.bakikhata.data.repository.CreditEntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddCreditState(
    val isLoading: Boolean = false,
    val creditEntry: CreditEntry? = null,
    val productName: String = "",
    val quantity: String = "",
    val price: String = "",
    val totalAmount: Double = 0.0,
    val date: Long = System.currentTimeMillis(),
    val notes: String = "",
    val productNameError: String? = null,
    val quantityError: String? = null,
    val priceError: String? = null,
    val isEditMode: Boolean = false
)

sealed class AddCreditEvent {
    data class ShowMessage(val message: String) : AddCreditEvent()
    object NavigateBack : AddCreditEvent()
}

@HiltViewModel
class AddCreditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val creditEntryRepository: CreditEntryRepository
) : ViewModel() {

    private val customerId: Long = savedStateHandle.get<Long>("customerId") ?: -1L
    private val creditId: Long = savedStateHandle.get<Long>("creditId") ?: -1L

    private val _state = MutableStateFlow(AddCreditState())
    val state: StateFlow<AddCreditState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<AddCreditEvent>()
    val events: SharedFlow<AddCreditEvent> = _events.asSharedFlow()

    init {
        if (creditId != -1L) {
            loadCreditEntry()
        }
    }

    private fun loadCreditEntry() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            try {
                creditEntryRepository.getCreditEntryById(creditId).collect { creditEntry ->
                    creditEntry?.let {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            creditEntry = it,
                            productName = it.productName,
                            quantity = it.quantity.toString(),
                            price = it.price.toString(),
                            totalAmount = it.totalAmount,
                            date = it.date,
                            notes = it.notes ?: "",
                            isEditMode = true
                        )
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
                _events.emit(AddCreditEvent.ShowMessage(e.message ?: "An error occurred"))
            }
        }
    }

    fun onProductNameChanged(productName: String) {
        _state.value = _state.value.copy(productName = productName, productNameError = null)
    }

    fun onQuantityChanged(quantity: String) {
        _state.value = _state.value.copy(quantity = quantity, quantityError = null)
        calculateTotal()
    }

    fun onPriceChanged(price: String) {
        _state.value = _state.value.copy(price = price, priceError = null)
        calculateTotal()
    }

    fun onDateChanged(date: Long) {
        _state.value = _state.value.copy(date = date)
    }

    fun onNotesChanged(notes: String) {
        _state.value = _state.value.copy(notes = notes)
    }

    private fun calculateTotal() {
        val currentState = _state.value
        val quantity = currentState.quantity.toDoubleOrNull() ?: 0.0
        val price = currentState.price.toDoubleOrNull() ?: 0.0
        val total = quantity * price
        _state.value = currentState.copy(totalAmount = total)
    }

    fun saveCreditEntry() {
        val currentState = _state.value
        
        // Validation
        var hasError = false
        if (currentState.productName.isBlank()) {
            _state.value = currentState.copy(productNameError = "Product name is required")
            hasError = true
        }
        if (currentState.quantity.isBlank() || currentState.quantity.toDoubleOrNull() == null) {
            _state.value = currentState.copy(quantityError = "Quantity is required")
            hasError = true
        }
        if (currentState.price.isBlank() || currentState.price.toDoubleOrNull() == null) {
            _state.value = currentState.copy(priceError = "Price is required")
            hasError = true
        }
        
        if (hasError) return

        viewModelScope.launch {
            _state.value = currentState.copy(isLoading = true)
            
            try {
                val quantity = currentState.quantity.toDouble()
                val price = currentState.price.toDouble()
                
                if (currentState.isEditMode) {
                    // Update existing credit entry
                    currentState.creditEntry?.let { creditEntry ->
                        val updatedEntry = creditEntry.copy(
                            productName = currentState.productName,
                            quantity = quantity,
                            price = price,
                            totalAmount = currentState.totalAmount,
                            date = currentState.date,
                            notes = currentState.notes.ifBlank { null },
                            updatedAt = System.currentTimeMillis()
                        )
                        creditEntryRepository.updateCreditEntry(updatedEntry)
                        _events.emit(AddCreditEvent.ShowMessage("Credit updated successfully"))
                    }
                } else {
                    // Insert new credit entry
                    val newEntry = CreditEntry(
                        customerId = customerId,
                        productName = currentState.productName,
                        quantity = quantity,
                        price = price,
                        totalAmount = currentState.totalAmount,
                        date = currentState.date,
                        notes = currentState.notes.ifBlank { null }
                    )
                    creditEntryRepository.insertCreditEntry(newEntry)
                    _events.emit(AddCreditEvent.ShowMessage("Credit added successfully"))
                }
                
                _events.emit(AddCreditEvent.NavigateBack)
            } catch (e: Exception) {
                _state.value = currentState.copy(isLoading = false)
                _events.emit(AddCreditEvent.ShowMessage(e.message ?: "An error occurred"))
            }
        }
    }

    fun deleteCreditEntry() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            try {
                creditEntryRepository.deleteCreditEntry(creditId)
                _events.emit(AddCreditEvent.ShowMessage("Credit deleted successfully"))
                _events.emit(AddCreditEvent.NavigateBack)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
                _events.emit(AddCreditEvent.ShowMessage(e.message ?: "An error occurred"))
            }
        }
    }
}
