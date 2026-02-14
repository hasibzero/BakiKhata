package com.hasibzero.bakikhata.ui.customer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasibzero.bakikhata.data.db.entity.CreditEntry
import com.hasibzero.bakikhata.data.db.entity.Customer
import com.hasibzero.bakikhata.data.db.entity.Payment
import com.hasibzero.bakikhata.data.repository.CreditEntryRepository
import com.hasibzero.bakikhata.data.repository.CustomerRepository
import com.hasibzero.bakikhata.data.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CustomerDetailState(
    val isLoading: Boolean = false,
    val customer: Customer? = null,
    val totalCredit: Double = 0.0,
    val totalPayment: Double = 0.0,
    val dueAmount: Double = 0.0,
    val credits: List<CreditEntry> = emptyList(),
    val payments: List<Payment> = emptyList(),
    val selectedTab: Int = 0, // 0 for credits, 1 for payments
    val error: String? = null
)

@HiltViewModel
class CustomerDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val customerRepository: CustomerRepository,
    private val creditEntryRepository: CreditEntryRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val customerId: Long = savedStateHandle.get<Long>("customerId") ?: -1L

    private val _state = MutableStateFlow(CustomerDetailState())
    val state: StateFlow<CustomerDetailState> = _state.asStateFlow()

    init {
        loadCustomerDetail()
    }

    private fun loadCustomerDetail() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            try {
                combine(
                    customerRepository.getCustomerById(customerId),
                    creditEntryRepository.getTotalCreditByCustomer(customerId),
                    paymentRepository.getTotalPaymentByCustomer(customerId),
                    creditEntryRepository.getCreditEntriesByCustomer(customerId),
                    paymentRepository.getPaymentsByCustomer(customerId)
                ) { customer, totalCredit, totalPayment, credits, payments ->
                    
                    val credit = totalCredit ?: 0.0
                    val payment = totalPayment ?: 0.0
                    
                    CustomerDetailState(
                        isLoading = false,
                        customer = customer,
                        totalCredit = credit,
                        totalPayment = payment,
                        dueAmount = credit - payment,
                        credits = credits,
                        payments = payments,
                        selectedTab = _state.value.selectedTab
                    )
                }.collect { newState ->
                    _state.value = newState
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "An error occurred"
                )
            }
        }
    }

    fun selectTab(tabIndex: Int) {
        _state.value = _state.value.copy(selectedTab = tabIndex)
    }

    fun refresh() {
        loadCustomerDetail()
    }
}
