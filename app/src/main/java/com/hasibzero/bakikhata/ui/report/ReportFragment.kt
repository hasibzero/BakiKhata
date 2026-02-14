package com.hasibzero.bakikhata.ui.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.hasibzero.bakikhata.databinding.FragmentReportBinding
import com.hasibzero.bakikhata.util.ExcelExporter
import com.hasibzero.bakikhata.util.PdfExporter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class ReportFragment : Fragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ReportViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupMonthYearPickers()
        setupButtons()
        observeState()
        observeEvents()
    }

    private fun setupMonthYearPickers() {
        // Month picker
        val months = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        val monthAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, months)
        binding.monthInput.setAdapter(monthAdapter)
        binding.monthInput.setText(months[Calendar.getInstance().get(Calendar.MONTH)], false)
        binding.monthInput.setOnItemClickListener { _, _, position, _ ->
            viewModel.onMonthSelected(position)
        }
        
        // Year picker
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currentYear - 5..currentYear + 1).map { it.toString() }
        val yearAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, years)
        binding.yearInput.setAdapter(yearAdapter)
        binding.yearInput.setText(currentYear.toString(), false)
        binding.yearInput.setOnItemClickListener { _, _, position, _ ->
            viewModel.onYearSelected(currentYear - 5 + position)
        }
    }

    private fun setupButtons() {
        binding.generateButton.setOnClickListener {
            viewModel.generateReport()
        }
        
        binding.exportPdfButton.setOnClickListener {
            viewModel.exportPdf()
        }
        
        binding.exportExcelButton.setOnClickListener {
            viewModel.exportExcel()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    binding.progressBar.isVisible = state.isLoading
                    binding.reportSummaryCard.isVisible = state.showSummary
                    
                    if (state.showSummary) {
                        binding.totalCreditText.text = "৳ %.2f".format(state.totalCredit)
                        binding.totalPaymentText.text = "৳ %.2f".format(state.totalPayment)
                        binding.remainingDueText.text = "৳ %.2f".format(state.remainingDue)
                    }
                }
            }
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is ReportEvent.ShowMessage -> {
                            Snackbar.make(binding.root, event.message, Snackbar.LENGTH_SHORT).show()
                        }
                        is ReportEvent.ExportPdf -> {
                            try {
                                // Call PDF exporter
                                Snackbar.make(binding.root, "Exporting PDF...", Snackbar.LENGTH_SHORT).show()
                                // pdfExporter.exportReport(...)
                            } catch (e: Exception) {
                                Snackbar.make(binding.root, "Error exporting PDF: ${e.message}", Snackbar.LENGTH_LONG).show()
                            }
                        }
                        is ReportEvent.ExportExcel -> {
                            try {
                                // Call Excel exporter
                                Snackbar.make(binding.root, "Exporting Excel...", Snackbar.LENGTH_SHORT).show()
                                // excelExporter.exportReport(...)
                            } catch (e: Exception) {
                                Snackbar.make(binding.root, "Error exporting Excel: ${e.message}", Snackbar.LENGTH_LONG).show()
                            }
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
