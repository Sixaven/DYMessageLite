package com.example.dymessagelite.ui.detail.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.dymessagelite.data.model.detail.ChatType
import com.example.dymessagelite.data.model.detail.MegDetailCell
import com.example.dymessagelite.data.model.list.DisplayListItem
import com.example.dymessagelite.databinding.ItemDetailButtonOtherBinding
import com.example.dymessagelite.databinding.ItemDetailImageMineBinding
import com.example.dymessagelite.databinding.ItemDetailImageOtherBinding

import com.example.dymessagelite.databinding.ItemDetailTextMineBinding
import com.example.dymessagelite.databinding.ItemDetailTextOtherBinding
import com.example.dymessagelite.ui.detail.MegDetailControl


interface OnClickDetailAdapterListener{
    fun onItemClick(item: MegDetailCell)
}

class MegDetailAdapter(
    private val myAvatarId: Int,
    private val otherAvatarId: Int,
    private val megDetailControl: MegDetailControl
) : ListAdapter<MegDetailCell, RecyclerView.ViewHolder>(ChatDiffCallback()) {
    companion object {
        private const val VIEW_TYPE_MINE_TEXT = 1
        private const val VIEW_TYPE_MINE_IMAGE = 2
        private const val VIEW_TYPE_OTHER_TEXT = 3
        private const val VIEW_TYPE_OTHER_IMAGE = 4
        private const val VIEW_TYPE_OTHER_BUTTON = 5
    }

    inner class MineTextViewHolder(val binding: ItemDetailTextMineBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MegDetailCell) {
            binding.ivMyAvatar.setImageResource(myAvatarId)
            binding.tvMyMessageContent.text = item.content
        }
    }

    inner class MineImageViewHolder(val binding: ItemDetailImageMineBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MegDetailCell) {
            binding.ivMyAvatar.setImageResource(myAvatarId)
            binding.ivSummaryImage.setImageResource(myAvatarId)
            // 使用 Glide 或其他库加载图片
            // Glide.with(itemView.context).load(item.imageUrl).into(binding.ivMyMessageImage)
        }
    }

    inner class OtherTextViewHolder(val binding: ItemDetailTextOtherBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MegDetailCell) {
            binding.ivOtherAvatar.setImageResource(otherAvatarId)
            binding.tvOtherMessageContent.text = item.content
        }
    }

    inner class OtherImageViewHolder(val binding: ItemDetailImageOtherBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MegDetailCell) {
            binding.ivOtherAvatar.setImageResource(otherAvatarId)
            binding.ivSummaryImage.setImageResource(otherAvatarId)
            // 使用 Glide 或其他库加载图片
            // Glide.with(itemView.context).load(item.imageUrl).into(binding.ivOtherMessageImage)
        }
    }

    inner class OtherButtonViewHolder(val binding: ItemDetailButtonOtherBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MegDetailCell) {
            binding.ivOtherAvatar.setImageResource(otherAvatarId)
            binding.tvOtherActionContent.text = item.content
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        // 根据 viewType 加载不同的布局，创建不同的 ViewHolder
        return when (viewType) {
            VIEW_TYPE_MINE_TEXT -> {
                val binding = ItemDetailTextMineBinding.inflate(inflater, parent, false)
                MineTextViewHolder(binding)
            }
            VIEW_TYPE_MINE_IMAGE -> {
                val binding = ItemDetailImageMineBinding.inflate(inflater, parent, false)
                MineImageViewHolder(binding)
            }
            VIEW_TYPE_OTHER_TEXT -> {
                val binding = ItemDetailTextOtherBinding.inflate(inflater, parent, false)
                OtherTextViewHolder(binding)
            }
            VIEW_TYPE_OTHER_IMAGE -> {
                val binding = ItemDetailImageOtherBinding.inflate(inflater, parent, false)
                OtherImageViewHolder(binding)
            }
            VIEW_TYPE_OTHER_BUTTON -> {
                val binding = ItemDetailButtonOtherBinding.inflate(inflater, parent, false)
                OtherButtonViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type") // 异常处理
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is MineTextViewHolder -> holder.bind(item)
            is MineImageViewHolder -> holder.bind(item)
            is OtherTextViewHolder -> holder.bind(item)
            is OtherImageViewHolder -> holder.bind(item)
            is OtherButtonViewHolder -> holder.bind(item)
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        val position = holder.bindingAdapterPosition
        if(position != RecyclerView.NO_POSITION){
            val item = getItem(position)
            if(!item.isDisplay){
                megDetailControl.markAsDisplay(item.id)
                megDetailControl.markAsRead(item.id)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val cell = getItem(position)
        return if (cell.isMine) {
            when(cell.type){
                ChatType.TEXT -> VIEW_TYPE_MINE_TEXT
                else -> VIEW_TYPE_MINE_IMAGE
            }
        } else {
            when(cell.type){
                ChatType.TEXT -> VIEW_TYPE_OTHER_TEXT
                ChatType.IMAGE -> VIEW_TYPE_OTHER_IMAGE
                else -> VIEW_TYPE_OTHER_BUTTON
            }
        }
    }

}

class ChatDiffCallback : DiffUtil.ItemCallback<MegDetailCell>() {
    override fun areItemsTheSame(oldItem: MegDetailCell, newItem: MegDetailCell): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: MegDetailCell, newItem: MegDetailCell): Boolean {
        return oldItem == newItem
    }
}