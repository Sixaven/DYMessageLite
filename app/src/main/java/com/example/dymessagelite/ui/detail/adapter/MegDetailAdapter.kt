package com.example.dymessagelite.ui.detail.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.dymessagelite.data.model.MegDetailCell
import com.example.dymessagelite.databinding.ItemDetailMineBinding
import com.example.dymessagelite.databinding.ItemDetailOtherBinding

class MegDetailAdapter(
    private val myAvatarId: Int,
    private val otherAvatarId: Int
)
    : ListAdapter<MegDetailCell, RecyclerView.ViewHolder>(ChatDiffCallback())
{
    companion object {
        private const val VIEW_TYPE_MINE = 1
        private const val VIEW_TYPE_OTHER = 2
    }
    inner class MineViewHolder(val binding: ItemDetailMineBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: MegDetailCell){
            binding.ivMyAvatar.setImageResource(myAvatarId)
            binding.tvMyMessageContent.text = item.content
        }
    }
    inner class OtherViewHolder(val binding: ItemDetailOtherBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: MegDetailCell){
            binding.ivOtherAvatar.setImageResource(otherAvatarId)
            binding.tvOtherMessageContent.text = item.content
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if(viewType == VIEW_TYPE_MINE){
            val binding = ItemDetailMineBinding.inflate(inflater, parent, false)
            MineViewHolder(binding)
        }else{
            val binding = ItemDetailOtherBinding.inflate(inflater, parent, false)
            OtherViewHolder(binding)
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when(holder){
         is MineViewHolder -> holder.bind(item)
         is OtherViewHolder -> holder.bind(item)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val cell = getItem(position)
        return if(cell.isMine){
            VIEW_TYPE_MINE
        }else{
            VIEW_TYPE_OTHER
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