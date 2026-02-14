package com.hasibzero.bakikhata.ui.customer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hasibzero.bakikhata.R
import com.hasibzero.bakikhata.databinding.ItemCustomerBinding

class CustomerAdapter(
    private val onItemClick: (CustomerWithBalance) -> Unit
) : ListAdapter<CustomerWithBalance, CustomerAdapter.CustomerViewHolder>(CustomerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val binding = ItemCustomerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CustomerViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CustomerViewHolder(
        private val binding: ItemCustomerBinding,
        private val onItemClick: (CustomerWithBalance) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(customerWithBalance: CustomerWithBalance) {
            val customer = customerWithBalance.customer
            val dueAmount = customerWithBalance.dueAmount
            
            binding.customerName.text = customer.name
            binding.customerPhone.text = customer.phoneNumber ?: "No phone"
            binding.dueAmount.text = "৳ %.2f".format(dueAmount)
            
            // Set status indicator color based on due amount
            val statusColor = when {
                dueAmount <= 0 -> R.color.paid_indicator
                else -> R.color.due_indicator
            }
            binding.statusIndicator.setBackgroundColor(
                ContextCompat.getColor(binding.root.context, statusColor)
            )
            
            binding.root.setOnClickListener {
                onItemClick(customerWithBalance)
            }
        }
    }

    private class CustomerDiffCallback : DiffUtil.ItemCallback<CustomerWithBalance>() {
        override fun areItemsTheSame(
            oldItem: CustomerWithBalance,
            newItem: CustomerWithBalance
        ): Boolean {
            return oldItem.customer.id == newItem.customer.id
        }

        override fun areContentsTheSame(
            oldItem: CustomerWithBalance,
            newItem: CustomerWithBalance
        ): Boolean {
            return oldItem == newItem
        }
    }
}
