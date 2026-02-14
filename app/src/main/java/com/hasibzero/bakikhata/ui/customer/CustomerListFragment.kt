package com.hasibzero.bakikhata.ui.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.hasibzero.bakikhata.R
import com.hasibzero.bakikhata.databinding.FragmentCustomerListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CustomerListFragment : Fragment() {

    private var _binding: FragmentCustomerListBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: CustomerListViewModel by viewModels()
    
    private lateinit var customerAdapter: CustomerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupSearch()
        setupFab()
        observeState()
    }

    private fun setupRecyclerView() {
        customerAdapter = CustomerAdapter(
            onItemClick = { customer ->
                val action = CustomerListFragmentDirections
                    .actionCustomerListToCustomerDetail(customer.customer.id)
                findNavController().navigate(action)
            }
        )
        
        binding.customersRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = customerAdapter
        }
    }

    private fun setupSearch() {
        binding.searchBar.doOnTextChanged { text, _, _, _ ->
            viewModel.search(text?.toString() ?: "")
        }
    }

    private fun setupFab() {
        binding.fabAddCustomer.setOnClickListener {
            findNavController().navigate(R.id.action_customerList_to_addCustomer)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    binding.progressBar.isVisible = state.isLoading
                    binding.emptyView.isVisible = state.customers.isEmpty() && !state.isLoading
                    binding.customersRecycler.isVisible = state.customers.isNotEmpty()
                    
                    customerAdapter.submitList(state.customers)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
