package com.example.dymessagelite.data.model.detail

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

object ChatType {
    const val TEXT = 0
    const val IMAGE = 1
    const val ACTION = 2
}

@Entity
data class ChatEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val senderId: String,
    val isMine: Boolean,
    val timestamp: Long,
    val content: String,
    @ColumnInfo(defaultValue = "0")
    val type: Int,
    @ColumnInfo(defaultValue = "false")
    val isRead: Boolean,
    @ColumnInfo(defaultValue = "false")
    val isDisplay: Boolean,
    @ColumnInfo(defaultValue = "false")
    val isClick: Boolean
)


