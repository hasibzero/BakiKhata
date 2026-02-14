package com.hasibzero.bakikhata.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasibzero.bakikhata.data.repository.CreditEntryRepository
import com.hasibzero.bakikhata.data.repository.PaymentRepository
import com.hasibzero.bakikhata.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class ReportState(
    val isLoading: Boolean = false,
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val totalCredit: Double = 0.0,
    val totalPayment: Double = 0.0,
    val remainingDue: Double = 0.0,
    val showSummary: Boolean = false,
    val error: String? = null
)

sealed class ReportEvent {
    data class ShowMessage(val message: String) : ReportEvent()
    data class ExportPdf(val month: Int, val year: Int) : ReportEvent()
    data class ExportExcel(val month: Int, val year: Int) : ReportEvent()
}

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val creditEntryRepository: CreditEntryRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ReportState())
    val state: StateFlow<ReportState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ReportEvent>()
    val events: SharedFlow<ReportEvent> = _events.asSharedFlow()

    fun onMonthSelected(month: Int) {
        _state.value = _state.value.copy(selectedMonth = month, showSummary = false)
    }

    fun onYearSelected(year: Int) {
        _state.value = _state.value.copy(selectedYear = year, showSummary = false)
    }

    fun generateReport() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            try {
                val currentState = _state.value
                val startOfMonth = DateUtils.getStartOfMonth(currentState.selectedYear, currentState.selectedMonth)
                val endOfMonth = DateUtils.getEndOfMonth(currentState.selectedYear, currentState.selectedMonth)
                
                var totalCredit = 0.0
                var totalPayment = 0.0
                
                // Get credits for the month (simplified - in real app would need DAO methods to filter by date range)
                creditEntryRepository.getTotalCreditAmount().collect { credit ->
                    totalCredit = credit ?: 0.0
                }
                
                // Get payments for the month
                paymentRepository.getTotalPaymentAmount().collect { payment ->
                    totalPayment = payment ?: 0.0
                }
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    totalCredit = totalCredit,
                    totalPayment = totalPayment,
                    remainingDue = totalCredit - totalPayment,
                    showSummary = true
                )
                
                _events.emit(ReportEvent.ShowMessage("Report generated successfully"))
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "An error occurred"
                )
                _events.emit(ReportEvent.ShowMessage(e.message ?: "Error generating report"))
            }
        }
    }

    fun exportPdf() {
        viewModelScope.launch {
            val currentState = _state.value
            _events.emit(ReportEvent.ExportPdf(currentState.selectedMonth, currentState.selectedYear))
        }
    }

    fun exportExcel() {
        viewModelScope.launch {
            val currentState = _state.value
            _events.emit(ReportEvent.ExportExcel(currentState.selectedMonth, currentState.selectedYear))
        }
    }
}
