package com.hasibzero.bakikhata.ui.payment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hasibzero.bakikhata.data.db.entity.Payment
import com.hasibzero.bakikhata.databinding.ItemPaymentBinding
import com.hasibzero.bakikhata.util.DateUtils

class PaymentHistoryAdapter(
    private val onItemClick: (Payment) -> Unit,
    private val onEditClick: (Payment) -> Unit
) : ListAdapter<Payment, PaymentHistoryAdapter.PaymentViewHolder>(PaymentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val binding = ItemPaymentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PaymentViewHolder(binding, onItemClick, onEditClick)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PaymentViewHolder(
        private val binding: ItemPaymentBinding,
        private val onItemClick: (Payment) -> Unit,
        private val onEditClick: (Payment) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(payment: Payment) {
            binding.date.text = DateUtils.formatDate(payment.date)
            binding.amount.text = "৳ %.2f".format(payment.amount)
            
            if (payment.notes.isNullOrBlank()) {
                binding.notes.isVisible = false
            } else {
                binding.notes.isVisible = true
                binding.notes.text = payment.notes
            }
            
            binding.root.setOnClickListener {
                onItemClick(payment)
            }
            
            binding.root.setOnLongClickListener {
                onEditClick(payment)
                true
            }
        }
    }

    private class PaymentDiffCallback : DiffUtil.ItemCallback<Payment>() {
        override fun areItemsTheSame(oldItem: Payment, newItem: Payment): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Payment, newItem: Payment): Boolean {
            return oldItem == newItem
        }
    }
}
