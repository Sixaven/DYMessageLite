package com.example.dymessagelite.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ChatEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val senderId: String,
    val isMine: Boolean,
    val timestamp: Long,
    val content: String
)