package com.hasibzero.bakikhata.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hasibzero.bakikhata.databinding.ItemDashboardCardBinding

class DashboardCardAdapter : ListAdapter<DashboardCard, DashboardCardAdapter.DashboardCardViewHolder>(DashboardCardDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardCardViewHolder {
        val binding = ItemDashboardCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DashboardCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DashboardCardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DashboardCardViewHolder(
        private val binding: ItemDashboardCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(card: DashboardCard) {
            binding.cardTitle.text = card.title
            binding.cardValue.text = card.value
        }
    }

    private class DashboardCardDiffCallback : DiffUtil.ItemCallback<DashboardCard>() {
        override fun areItemsTheSame(oldItem: DashboardCard, newItem: DashboardCard): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: DashboardCard, newItem: DashboardCard): Boolean {
            return oldItem == newItem
        }
    }
}
