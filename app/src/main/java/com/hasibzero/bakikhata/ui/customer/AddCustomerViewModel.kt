package com.hasibzero.bakikhata.ui.customer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasibzero.bakikhata.data.db.entity.Customer
import com.hasibzero.bakikhata.data.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddCustomerState(
    val isLoading: Boolean = false,
    val customer: Customer? = null,
    val name: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val nameError: String? = null,
    val isEditMode: Boolean = false
)

sealed class AddCustomerEvent {
    data class ShowMessage(val message: String) : AddCustomerEvent()
    object NavigateBack : AddCustomerEvent()
}

@HiltViewModel
class AddCustomerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val customerId: Long = savedStateHandle.get<Long>("customerId") ?: -1L

    private val _state = MutableStateFlow(AddCustomerState())
    val state: StateFlow<AddCustomerState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<AddCustomerEvent>()
    val events: SharedFlow<AddCustomerEvent> = _events.asSharedFlow()

    init {
        if (customerId != -1L) {
            loadCustomer()
        }
    }

    private fun loadCustomer() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            try {
                customerRepository.getCustomerById(customerId).collect { customer ->
                    customer?.let {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            customer = it,
                            name = it.name,
                            phoneNumber = it.phoneNumber ?: "",
                            address = it.address ?: "",
                            isEditMode = true
                        )
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
                _events.emit(AddCustomerEvent.ShowMessage(e.message ?: "An error occurred"))
            }
        }
    }

    fun onNameChanged(name: String) {
        _state.value = _state.value.copy(name = name, nameError = null)
    }

    fun onPhoneNumberChanged(phoneNumber: String) {
        _state.value = _state.value.copy(phoneNumber = phoneNumber)
    }

    fun onAddressChanged(address: String) {
        _state.value = _state.value.copy(address = address)
    }

    fun saveCustomer() {
        val currentState = _state.value
        
        // Validation
        if (currentState.name.isBlank()) {
            _state.value = currentState.copy(nameError = "Customer name is required")
            return
        }

        viewModelScope.launch {
            _state.value = currentState.copy(isLoading = true)
            
            try {
                if (currentState.isEditMode) {
                    // Update existing customer
                    currentState.customer?.let { customer ->
                        val updatedCustomer = customer.copy(
                            name = currentState.name,
                            phoneNumber = currentState.phoneNumber.ifBlank { null },
                            address = currentState.address.ifBlank { null },
                            updatedAt = System.currentTimeMillis()
                        )
                        customerRepository.updateCustomer(updatedCustomer)
                        _events.emit(AddCustomerEvent.ShowMessage("Customer updated successfully"))
                    }
                } else {
                    // Insert new customer
                    val newCustomer = Customer(
                        name = currentState.name,
                        phoneNumber = currentState.phoneNumber.ifBlank { null },
                        address = currentState.address.ifBlank { null }
                    )
                    customerRepository.insertCustomer(newCustomer)
                    _events.emit(AddCustomerEvent.ShowMessage("Customer added successfully"))
                }
                
                _events.emit(AddCustomerEvent.NavigateBack)
            } catch (e: Exception) {
                _state.value = currentState.copy(isLoading = false)
                _events.emit(AddCustomerEvent.ShowMessage(e.message ?: "An error occurred"))
            }
        }
    }

    fun deleteCustomer() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            try {
                customerRepository.deleteCustomer(customerId)
                _events.emit(AddCustomerEvent.ShowMessage("Customer deleted successfully"))
                _events.emit(AddCustomerEvent.NavigateBack)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
                _events.emit(AddCustomerEvent.ShowMessage(e.message ?: "An error occurred"))
            }
        }
    }
}
