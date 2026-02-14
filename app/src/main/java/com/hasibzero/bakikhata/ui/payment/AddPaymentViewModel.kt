package com.hasibzero.bakikhata.ui.payment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasibzero.bakikhata.data.db.entity.Payment
import com.hasibzero.bakikhata.data.repository.CreditEntryRepository
import com.hasibzero.bakikhata.data.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddPaymentState(
    val isLoading: Boolean = false,
    val payment: Payment? = null,
    val amount: String = "",
    val date: Long = System.currentTimeMillis(),
    val notes: String = "",
    val remainingBalance: Double = 0.0,
    val amountError: String? = null,
    val isEditMode: Boolean = false
)

sealed class AddPaymentEvent {
    data class ShowMessage(val message: String) : AddPaymentEvent()
    object NavigateBack : AddPaymentEvent()
}

@HiltViewModel
class AddPaymentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val paymentRepository: PaymentRepository,
    private val creditEntryRepository: CreditEntryRepository
) : ViewModel() {

    private val customerId: Long = savedStateHandle.get<Long>("customerId") ?: -1L
    private val paymentId: Long = savedStateHandle.get<Long>("paymentId") ?: -1L

    private val _state = MutableStateFlow(AddPaymentState())
    val state: StateFlow<AddPaymentState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<AddPaymentEvent>()
    val events: SharedFlow<AddPaymentEvent> = _events.asSharedFlow()

    init {
        loadRemainingBalance()
        if (paymentId != -1L) {
            loadPayment()
        }
    }

    private fun loadRemainingBalance() {
        viewModelScope.launch {
            try {
                combine(
                    creditEntryRepository.getTotalCreditByCustomer(customerId),
                    paymentRepository.getTotalPaymentByCustomer(customerId)
                ) { totalCredit, totalPayment ->
                    (totalCredit ?: 0.0) - (totalPayment ?: 0.0)
                }.collect { balance ->
                    _state.value = _state.value.copy(remainingBalance = balance)
                }
            } catch (e: Exception) {
                _events.emit(AddPaymentEvent.ShowMessage(e.message ?: "An error occurred"))
            }
        }
    }

    private fun loadPayment() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            try {
                paymentRepository.getPaymentById(paymentId).collect { payment ->
                    payment?.let {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            payment = it,
                            amount = it.amount.toString(),
                            date = it.date,
                            notes = it.notes ?: "",
                            isEditMode = true
                        )
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
                _events.emit(AddPaymentEvent.ShowMessage(e.message ?: "An error occurred"))
            }
        }
    }

    fun onAmountChanged(amount: String) {
        _state.value = _state.value.copy(amount = amount, amountError = null)
    }

    fun onDateChanged(date: Long) {
        _state.value = _state.value.copy(date = date)
    }

    fun onNotesChanged(notes: String) {
        _state.value = _state.value.copy(notes = notes)
    }

    fun savePayment() {
        val currentState = _state.value
        
        // Validation
        if (currentState.amount.isBlank() || currentState.amount.toDoubleOrNull() == null) {
            _state.value = currentState.copy(amountError = "Amount is required")
            return
        }
        
        val amount = currentState.amount.toDouble()
        if (amount <= 0) {
            _state.value = currentState.copy(amountError = "Amount must be greater than 0")
            return
        }

        viewModelScope.launch {
            _state.value = currentState.copy(isLoading = true)
            
            try {
                if (currentState.isEditMode) {
                    // Update existing payment
                    currentState.payment?.let { payment ->
                        val updatedPayment = payment.copy(
                            amount = amount,
                            date = currentState.date,
                            notes = currentState.notes.ifBlank { null },
                            updatedAt = System.currentTimeMillis()
                        )
                        paymentRepository.updatePayment(updatedPayment)
                        _events.emit(AddPaymentEvent.ShowMessage("Payment updated successfully"))
                    }
                } else {
                    // Insert new payment
                    val newPayment = Payment(
                        customerId = customerId,
                        amount = amount,
                        date = currentState.date,
                        notes = currentState.notes.ifBlank { null }
                    )
                    paymentRepository.insertPayment(newPayment)
                    _events.emit(AddPaymentEvent.ShowMessage("Payment added successfully"))
                }
                
                _events.emit(AddPaymentEvent.NavigateBack)
            } catch (e: Exception) {
                _state.value = currentState.copy(isLoading = false)
                _events.emit(AddPaymentEvent.ShowMessage(e.message ?: "An error occurred"))
            }
        }
    }

    fun deletePayment() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            try {
                paymentRepository.deletePayment(paymentId)
                _events.emit(AddPaymentEvent.ShowMessage("Payment deleted successfully"))
                _events.emit(AddPaymentEvent.NavigateBack)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
                _events.emit(AddPaymentEvent.ShowMessage(e.message ?: "An error occurred"))
            }
        }
    }
}
