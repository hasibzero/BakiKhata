package com.hasibzero.bakikhata.ui.customer

import android.content.Intent
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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.hasibzero.bakikhata.R
import com.hasibzero.bakikhata.databinding.FragmentCustomerDetailBinding
import com.hasibzero.bakikhata.ui.credit.CreditHistoryAdapter
import com.hasibzero.bakikhata.ui.payment.PaymentHistoryAdapter
import com.hasibzero.bakikhata.util.SmsHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CustomerDetailFragment : Fragment() {

    private var _binding: FragmentCustomerDetailBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: CustomerDetailViewModel by viewModels()
    
    private lateinit var creditHistoryAdapter: CreditHistoryAdapter
    private lateinit var paymentHistoryAdapter: PaymentHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupTabs()
        setupRecyclerViews()
        setupButtons()
        observeState()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewModel.selectTab(tab?.position ?: 0)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerViews() {
        creditHistoryAdapter = CreditHistoryAdapter(
            onItemClick = { /* Navigate to credit detail if needed */ },
            onEditClick = { creditEntry ->
                val action = CustomerDetailFragmentDirections
                    .actionCustomerDetailToAddCredit(
                        customerId = creditEntry.customerId,
                        creditId = creditEntry.id
                    )
                findNavController().navigate(action)
            }
        )
        binding.creditsRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = creditHistoryAdapter
        }
        
        paymentHistoryAdapter = PaymentHistoryAdapter(
            onItemClick = { /* Navigate to payment detail if needed */ },
            onEditClick = { payment ->
                val action = CustomerDetailFragmentDirections
                    .actionCustomerDetailToAddPayment(
                        customerId = payment.customerId,
                        paymentId = payment.id
                    )
                findNavController().navigate(action)
            }
        )
        binding.paymentsRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = paymentHistoryAdapter
        }
    }

    private fun setupButtons() {
        binding.editButton.setOnClickListener {
            viewModel.state.value.customer?.let { customer ->
                val action = CustomerDetailFragmentDirections
                    .actionCustomerListToAddCustomer(customerId = customer.id)
                findNavController().navigate(action)
            }
        }
        
        binding.sendReminderButton.setOnClickListener {
            viewModel.state.value.let { state ->
                state.customer?.let { customer ->
                    if (!customer.phoneNumber.isNullOrEmpty()) {
                        val message = getString(
                            R.string.sms_template,
                            customer.name,
                            state.dueAmount,
                            getString(R.string.app_name)
                        )
                        SmsHelper.sendSms(requireContext(), customer.phoneNumber, message)
                    }
                }
            }
        }
        
        binding.fabAddCredit.setOnClickListener {
            viewModel.state.value.customer?.let { customer ->
                val action = CustomerDetailFragmentDirections
                    .actionCustomerDetailToAddCredit(customerId = customer.id)
                findNavController().navigate(action)
            }
        }
        
        binding.fabAddPayment.setOnClickListener {
            viewModel.state.value.customer?.let { customer ->
                val action = CustomerDetailFragmentDirections
                    .actionCustomerDetailToAddPayment(customerId = customer.id)
                findNavController().navigate(action)
            }
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    binding.progressBar.isVisible = state.isLoading
                    
                    state.customer?.let { customer ->
                        binding.customerName.text = customer.name
                        binding.customerPhone.text = customer.phoneNumber ?: "No phone"
                        binding.customerAddress.text = customer.address ?: "No address"
                    }
                    
                    binding.totalCredit.text = "৳ %.2f".format(state.totalCredit)
                    binding.totalPaid.text = "৳ %.2f".format(state.totalPayment)
                    binding.remainingDue.text = "৳ %.2f".format(state.dueAmount)
                    
                    // Update tab content visibility
                    binding.creditsContainer.isVisible = state.selectedTab == 0
                    binding.paymentsContainer.isVisible = state.selectedTab == 1
                    
                    // Update credit list
                    creditHistoryAdapter.submitList(state.credits)
                    binding.emptyCredits.isVisible = state.credits.isEmpty()
                    
                    // Update payment list
                    paymentHistoryAdapter.submitList(state.payments)
                    binding.emptyPayments.isVisible = state.payments.isEmpty()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
