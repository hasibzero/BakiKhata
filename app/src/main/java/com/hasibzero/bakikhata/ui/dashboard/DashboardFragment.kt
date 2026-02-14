package com.hasibzero.bakikhata.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.hasibzero.bakikhata.databinding.FragmentDashboardBinding
import com.hasibzero.bakikhata.ui.credit.CreditHistoryAdapter
import com.hasibzero.bakikhata.ui.payment.PaymentHistoryAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: DashboardViewModel by viewModels()
    
    private lateinit var dashboardCardAdapter: DashboardCardAdapter
    private lateinit var creditHistoryAdapter: CreditHistoryAdapter
    private lateinit var paymentHistoryAdapter: PaymentHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerViews()
        observeState()
    }

    private fun setupRecyclerViews() {
        // Dashboard cards
        dashboardCardAdapter = DashboardCardAdapter()
        binding.dashboardCardsRecycler.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = dashboardCardAdapter
        }
        
        // Recent credits
        creditHistoryAdapter = CreditHistoryAdapter(
            onItemClick = { /* Navigate to credit detail if needed */ },
            onEditClick = { /* Navigate to edit credit if needed */ }
        )
        binding.recentCreditsRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = creditHistoryAdapter
        }
        
        // Recent payments
        paymentHistoryAdapter = PaymentHistoryAdapter(
            onItemClick = { /* Navigate to payment detail if needed */ },
            onEditClick = { /* Navigate to edit payment if needed */ }
        )
        binding.recentPaymentsRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = paymentHistoryAdapter
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    binding.progressBar.isVisible = state.isLoading
                    
                    dashboardCardAdapter.submitList(state.cards)
                    
                    creditHistoryAdapter.submitList(state.recentCredits)
                    binding.emptyCredits.isVisible = state.recentCredits.isEmpty()
                    
                    paymentHistoryAdapter.submitList(state.recentPayments)
                    binding.emptyPayments.isVisible = state.recentPayments.isEmpty()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
