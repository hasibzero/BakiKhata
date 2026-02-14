package com.hasibzero.bakikhata.ui.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasibzero.bakikhata.data.db.entity.Customer
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

data class CustomerWithBalance(
    val customer: Customer,
    val totalCredit: Double,
    val totalPayment: Double,
    val dueAmount: Double
)

data class CustomerListState(
    val isLoading: Boolean = false,
    val customers: List<CustomerWithBalance> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null
)

@HiltViewModel
class CustomerListViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val creditEntryRepository: CreditEntryRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CustomerListState())
    val state: StateFlow<CustomerListState> = _state.asStateFlow()

    init {
        loadCustomers()
    }

    private fun loadCustomers(searchQuery: String = "") {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            try {
                val customersFlow = if (searchQuery.isEmpty()) {
                    customerRepository.getAllCustomers()
                } else {
                    customerRepository.searchCustomers(searchQuery)
                }
                
                customersFlow.collect { customers ->
                    val customersWithBalance = customers.map { customer ->
                        var totalCredit = 0.0
                        var totalPayment = 0.0
                        
                        creditEntryRepository.getTotalCreditByCustomer(customer.id).collect { credit ->
                            totalCredit = credit ?: 0.0
                        }
                        
                        paymentRepository.getTotalPaymentByCustomer(customer.id).collect { payment ->
                            totalPayment = payment ?: 0.0
                        }
                        
                        CustomerWithBalance(
                            customer = customer,
                            totalCredit = totalCredit,
                            totalPayment = totalPayment,
                            dueAmount = totalCredit - totalPayment
                        )
                    }
                    
                    _state.value = _state.value.copy(
                        isLoading = false,
                        customers = customersWithBalance,
                        searchQuery = searchQuery
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "An error occurred"
                )
            }
        }
    }

    fun search(query: String) {
        loadCustomers(query)
    }

    fun refresh() {
        loadCustomers(_state.value.searchQuery)
    }
}
