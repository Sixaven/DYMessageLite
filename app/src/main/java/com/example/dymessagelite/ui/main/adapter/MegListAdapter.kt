package com.example.dymessagelite.ui.main.adapter

import android.os.Message
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dymessagelite.data.model.MegItem
import com.example.dymessagelite.databinding.ItemMessageBinding

class MegListAdapter : ListAdapter<MegItem, MegListAdapter.MegViewHolder>(MessageDiffCallback()){

    inner class MegViewHolder(val binding: ItemMessageBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: MegItem){
            binding.tvNickname.text = "这是昵称"

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
class MessageDiffCallback : DiffUtil.ItemCallback<MegItem>() {
    override fun areItemsTheSame(oldItem: MegItem, newItem: MegItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: MegItem, newItem: MegItem): Boolean {
        return oldItem == newItem
    }
}