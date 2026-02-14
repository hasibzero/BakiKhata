package com.hasibzero.bakikhata.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasibzero.bakikhata.data.db.entity.CreditEntry
import com.hasibzero.bakikhata.data.db.entity.Payment
import com.hasibzero.bakikhata.data.repository.CreditEntryRepository
import com.hasibzero.bakikhata.data.repository.CustomerRepository
import com.hasibzero.bakikhata.data.repository.PaymentRepository
import com.hasibzero.bakikhata.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardCard(
    val title: String,
    val value: String
)

data class DashboardState(
    val isLoading: Boolean = false,
    val cards: List<DashboardCard> = emptyList(),
    val recentCredits: List<CreditEntry> = emptyList(),
    val recentPayments: List<Payment> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val creditEntryRepository: CreditEntryRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            try {
                combine(
                    customerRepository.getCustomerCount(),
                    creditEntryRepository.getTotalCreditAmount(),
                    paymentRepository.getTotalPaymentAmount(),
                    creditEntryRepository.getTodaysCreditAmount(
                        DateUtils.getStartOfDay(System.currentTimeMillis()),
                        DateUtils.getEndOfDay(System.currentTimeMillis())
                    ),
                    creditEntryRepository.getRecentCreditEntries(5),
                    paymentRepository.getRecentPayments(5)
                ) { customerCount, totalCredit, totalPayment, todaysCredit, recentCredits, recentPayments ->
                    
                    val totalDue = (totalCredit ?: 0.0) - (totalPayment ?: 0.0)
                    
                    val cards = listOf(
                        DashboardCard(
                            title = "Total Customers",
                            value = customerCount.toString()
                        ),
                        DashboardCard(
                            title = "Total Due",
                            value = "৳ %.2f".format(totalDue)
                        ),
                        DashboardCard(
                            title = "Total Credit",
                            value = "৳ %.2f".format(totalCredit ?: 0.0)
                        ),
                        DashboardCard(
                            title = "Today's Credit",
                            value = "৳ %.2f".format(todaysCredit ?: 0.0)
                        )
                    )
                    
                    DashboardState(
                        isLoading = false,
                        cards = cards,
                        recentCredits = recentCredits,
                        recentPayments = recentPayments
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

    fun refresh() {
        loadDashboardData()
    }
}
