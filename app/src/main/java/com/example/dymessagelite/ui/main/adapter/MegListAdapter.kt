package com.example.dymessagelite.ui.main.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dymessagelite.data.model.MegItem
import com.example.dymessagelite.databinding.ItemMessageBinding

class MegListAdapter (private val onItemClick: (MegItem) -> Unit)
    : ListAdapter<MegItem, MegListAdapter.MegViewHolder>(MegItemDiffCallback()){

    inner class MegViewHolder(val binding: ItemMessageBinding): RecyclerView.ViewHolder(binding.root){
        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition;
                if(position != RecyclerView.NO_POSITION){
                    val item = getItem(position);
                    onItemClick(item)
                }
            }
        }
        fun bind(item: MegItem) {

            binding.tvNickname.text = item.name
            binding.tvSummary.text = item.summary
            binding.tvTime.text = item.timestamp

            // 4. 根据未读数，控制角标的显示和内容
            if (item.unreadCount > 0) {
                binding.tvUnreadBadge.visibility = View.VISIBLE
                // 如果未读数大于99，显示"99+"，否则显示具体数字
                binding.tvUnreadBadge.text = if (item.unreadCount > 99) {
                    "99+"
                } else {
                    item.unreadCount.toString()
                }
            } else {
                // 如果未读数为0，则隐藏角标
                binding.tvUnreadBadge.visibility = View.GONE
            }

            // binding.ivAvatar.setImageResource(...)
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MegViewHolder {
        val binding = ItemMessageBinding.inflate(
            LayoutInflater.from(parent.context)
            ,parent
            ,false
        )
        return MegViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MegViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

}
class MegItemDiffCallback : DiffUtil.ItemCallback<MegItem>() {
    override fun areItemsTheSame(oldItem: MegItem, newItem: MegItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: MegItem, newItem: MegItem): Boolean {
        return oldItem == newItem
    }
}