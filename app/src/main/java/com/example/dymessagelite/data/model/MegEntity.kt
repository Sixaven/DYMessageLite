package com.example.dymessagelite.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MegEntity (
    @PrimaryKey
    val friendId: String,
    val headId: String,
    val friendName: String,
    val latestMessage: String,
    val timestamp: Long,
    val unreadCount: Int
)


