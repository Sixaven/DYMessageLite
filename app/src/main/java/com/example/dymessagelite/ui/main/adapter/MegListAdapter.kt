package com.example.dymessagelite.ui.main.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dymessagelite.data.model.MegItem
import com.example.dymessagelite.databinding.ItemMessageBinding

class MegListAdapter (
    private val megList: MutableList<MegItem> = mutableListOf(),
    private val onItemClick: (MegItem) -> Unit
)
    : RecyclerView.Adapter<MegListAdapter.MegViewHolder>(){

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

    override fun getItemCount(): Int {
        return megList.size
    }
    fun updateDataAndMoveTop(newItem: MegItem){
        val oldIndex = megList.indexOfFirst { it.id == newItem.id }

        if(oldIndex != -1){
            megList.removeAt(oldIndex)
            megList.add(0,newItem)
            notifyItemMoved(oldIndex,0);
            notifyItemChanged(0);
        }else{
            megList.add(0,newItem)
            notifyItemInserted(0)
        }
    }

    fun updateUnreadPlace(newItem: MegItem){
        val oldIndex = megList.indexOfFirst { it.id == newItem.id }
        if(oldIndex != -1){
            megList[oldIndex] = newItem
            notifyItemChanged(oldIndex)
        }else{
            throw IllegalArgumentException("旧的发送者居然没有找到 干鸡毛呢")
        }
    }

    fun addMoreData(newItems: List<MegItem>) {
        // 为了高效地检查重复，先创建一个包含现有所有 item ID 的 Set
        val existingIds = megList.map { it.id }.toSet()
        // 过滤传入的新数据，只保留那些 ID 不在现有 Set 中的 item
        val uniqueNewItems = newItems.filter { it.id !in existingIds }

        // 如果确实有新的、不重复的数据需要添加
        if (uniqueNewItems.isNotEmpty()) {
            val startPosition = megList.size
            megList.addAll(uniqueNewItems)
            notifyItemRangeInserted(startPosition, uniqueNewItems.size)
        }
    }
    fun getItem(position: Int): MegItem {
        return megList[position]
    }

    fun setData(data: List<MegItem>){
        megList.clear()
        megList.addAll(data)
        notifyDataSetChanged()
    }
}