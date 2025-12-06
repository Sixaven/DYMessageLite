package com.example.dymessagelite.ui.main.adapter

import android.graphics.Color
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

import androidx.recyclerview.widget.RecyclerView
import com.example.dymessagelite.R

import com.example.dymessagelite.data.model.list.DisplayListItem
import com.example.dymessagelite.data.model.list.DisplayType

import com.example.dymessagelite.data.model.list.MegType


import com.example.dymessagelite.databinding.ItemMessageButtonBinding
import com.example.dymessagelite.databinding.ItemMessageImageBinding
import com.example.dymessagelite.databinding.ItemMessageSearchBinding
import com.example.dymessagelite.databinding.ItemMessageTextBinding
import java.util.regex.Pattern
interface OnClickListAdapterListener{
    fun onItemClick(item: DisplayListItem)
    fun onAvatarClick(item: DisplayListItem)
    fun onButtonActionClick(item: DisplayListItem)
}
class MegListAdapter (
    private val listener: OnClickListAdapterListener
)
    : ListAdapter<DisplayListItem,RecyclerView.ViewHolder>(DiffCallback()){

    private var searchKeyword: String = ""

    companion object {

        private const val VIEW_TYPE_TEXT = 0
        private const val VIEW_TYPE_IMAGE = 1
        private const val VIEW_TYPE_BUTTON = 2
        private const val VIEW_SEARCH_RESULT = 3
    }

    inner class TextViewHolder(val binding: ItemMessageTextBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            // 为每个 ViewHolder 设置点击事件
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(position))
                }
            }
            binding.ivAvatar.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onAvatarClick(getItem(position))
                }
            }
        }

        fun bind(item: DisplayListItem) {
            binding.tvNickname.text = item.name
            binding.tvSummary.text = item.context // 这是文本摘要
            binding.tvTime.text = item.timestamp
            updateBadge(binding.root, item.unreadCount) // 使用一个辅助方法来更新角标
        }
    }

    // 图片 ViewHolder
    inner class ImageViewHolder(val binding: ItemMessageImageBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(position))
                }
            }
            binding.ivAvatar.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onAvatarClick(getItem(position))
                }
            }
        }

        fun bind(item: DisplayListItem) {
            binding.tvNickname.text = item.name
            // 这里可以加载图片，例如使用 Glide
            // Glide.with(itemView.context).load(item.imageUrl).into(binding.ivSummaryImage)
            //binding.ivSummaryImage.setImageResource()
            binding.tvTime.text = item.timestamp
            updateBadge(binding.root, item.unreadCount)
        }
    }

    // 按钮 ViewHolder
    inner class ButtonViewHolder(val binding: ItemMessageButtonBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(position))
                }
            }
            binding.ivAvatar.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onAvatarClick(getItem(position))
                }
            }
            binding.btnSummaryAction.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onButtonActionClick(getItem(position))
                }
            }

        }

        fun bind(item: DisplayListItem) {
            binding.tvNickname.text = item.name
            binding.btnSummaryAction.text = item.context // 按钮上的文字
            binding.tvTime.text = item.timestamp
            updateBadge(binding.root, item.unreadCount)
        }
    }
    inner class SearchResultViewHolder(val binding: ItemMessageSearchBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(position))
                }
            }
            binding.ivAvatar.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onAvatarClick(getItem(position))
                }
            }
        }
        fun bind(item: DisplayListItem) {
            binding.tvNickname.text = highlightKeyword(item.name,searchKeyword, Color.RED)
            // 这里可以加载图片，例如使用 Glide
            // Glide.with(itemView.context).load(item.imageUrl).into(binding.ivSummaryImage)
            binding.tvContent.text = highlightKeyword(item.context,searchKeyword, Color.RED)
            binding.tvTime.text = item.timestamp
        }
    }
    // 辅助方法，用于更新未读角标，避免在每个 bind 方法中重复代码
    private fun updateBadge(view: View, unreadCount: Int) {
        val badge = view.findViewById<android.widget.TextView>(R.id.tv_unread_badge)
        if (unreadCount > 0) {
            badge.visibility = View.VISIBLE
            badge.text = if (unreadCount > 99) "99+" else unreadCount.toString()
        } else {
            badge.visibility = View.GONE
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            VIEW_TYPE_TEXT -> {
                val binding = ItemMessageTextBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                TextViewHolder(binding)
            }
            VIEW_TYPE_IMAGE -> {
                val binding = ItemMessageImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ImageViewHolder(binding)
            }
            VIEW_TYPE_BUTTON -> {
                val binding = ItemMessageButtonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ButtonViewHolder(binding)
            }
            VIEW_SEARCH_RESULT -> {
                val binding = ItemMessageSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                SearchResultViewHolder(binding)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when(holder){
            is TextViewHolder -> holder.bind(item)
            is ImageViewHolder -> holder.bind(item)
            is ButtonViewHolder -> holder.bind(item)
            is SearchResultViewHolder -> holder.bind(item)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return if(item.displayType == DisplayType.DEFAULT){
            when(item.contentType){
                MegType.TEXT -> VIEW_TYPE_TEXT
                MegType.IMAGE -> VIEW_TYPE_IMAGE
                else -> VIEW_TYPE_BUTTON
            }
        }else if(item.displayType == DisplayType.SEARCH){
            VIEW_SEARCH_RESULT
        }else{
            throw IllegalArgumentException("Invalid view type")
        }
    }
    fun setHighlightKeyword(keyword: String) {
        this.searchKeyword = keyword
    }
    private fun highlightKeyword(text: String, keyword: String, highlightColor: Int): SpannableString {
        val spannableString = SpannableString(text)
        if (keyword.isBlank() || !text.contains(keyword, ignoreCase = true)) {
            return spannableString // 如果关键词为空或文本不包含关键词，直接返回
        }

        // 使用正则表达式进行不区分大小写的匹配
        val pattern = Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(text)

        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()
            // 设置高亮效果
            spannableString.setSpan(ForegroundColorSpan(highlightColor), start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        return spannableString
    }
}
class DiffCallback : DiffUtil.ItemCallback<DisplayListItem>() {
    // 判断两个对象是否代表同一个Item。通常用唯一的ID来比较。
    override fun areItemsTheSame(oldItem: DisplayListItem, newItem: DisplayListItem): Boolean {
        return oldItem.id == newItem.id
    }

    // 判断两个Item的内容是否完全相同。
    // Kotlin的data class会自动生成equals()方法，非常适合用于此场景。
    override fun areContentsTheSame(oldItem: DisplayListItem, newItem: DisplayListItem): Boolean {
        return oldItem == newItem
    }
}