package com.hasibzero.bakikhata.ui.credit

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hasibzero.bakikhata.data.db.entity.CreditEntry
import com.hasibzero.bakikhata.databinding.ItemCreditEntryBinding
import com.hasibzero.bakikhata.util.DateUtils

class CreditHistoryAdapter(
    private val onItemClick: (CreditEntry) -> Unit,
    private val onEditClick: (CreditEntry) -> Unit
) : ListAdapter<CreditEntry, CreditHistoryAdapter.CreditViewHolder>(CreditDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreditViewHolder {
        val binding = ItemCreditEntryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CreditViewHolder(binding, onItemClick, onEditClick)
    }

    override fun onBindViewHolder(holder: CreditViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CreditViewHolder(
        private val binding: ItemCreditEntryBinding,
        private val onItemClick: (CreditEntry) -> Unit,
        private val onEditClick: (CreditEntry) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(credit: CreditEntry) {
            binding.productName.text = credit.productName
            binding.quantityPrice.text = "Qty: ${credit.quantity} x ৳ ${credit.price}"
            binding.date.text = DateUtils.formatDate(credit.date)
            binding.totalAmount.text = "৳ %.2f".format(credit.totalAmount)
            
            binding.root.setOnClickListener {
                onItemClick(credit)
            }
            
            binding.root.setOnLongClickListener {
                onEditClick(credit)
                true
            }
        }
    }

    private class CreditDiffCallback : DiffUtil.ItemCallback<CreditEntry>() {
        override fun areItemsTheSame(oldItem: CreditEntry, newItem: CreditEntry): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CreditEntry, newItem: CreditEntry): Boolean {
            return oldItem == newItem
        }
    }
}
