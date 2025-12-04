package com.example.dymessagelite.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MegEntity (
    @PrimaryKey
    val id: String,
    val avatar: String,
    val name: String,
    val latestMessage: String,
    val timestamp: Long,
    var unreadCount: Int
)


