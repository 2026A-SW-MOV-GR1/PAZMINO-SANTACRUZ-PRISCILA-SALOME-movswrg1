package com.example.netflixclone.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.netflixclone.R
import com.example.netflixclone.model.CategoryChip

class ChipAdapter(
    private val onChipClick: (CategoryChip) -> Unit
) : ListAdapter<CategoryChip, ChipAdapter.ChipViewHolder>(DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<CategoryChip>() {
        override fun areItemsTheSame(oldItem: CategoryChip, newItem: CategoryChip): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CategoryChip, newItem: CategoryChip): Boolean {
            return oldItem == newItem
        }
    }

    inner class ChipViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_chip, parent, false)
    ) {
        private val tvChip: TextView = itemView.findViewById(R.id.tvChip)

        fun bind(item: CategoryChip) {
            tvChip.text = item.name

            itemView.setOnClickListener {
                itemView.animate()
                    .scaleX(0.94f)
                    .scaleY(0.94f)
                    .setDuration(80)
                    .withEndAction {
                        itemView.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(80)
                            .start()
                    }
                    .start()

                onChipClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChipViewHolder {
        return ChipViewHolder(parent)
    }

    override fun onBindViewHolder(holder: ChipViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}