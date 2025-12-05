package com.example.dymessagelite.data.model

import androidx.room.ColumnInfo

data class MegDetailCell(
    val id: Int,
    val content: String,
    val timestamp: String,
    val isMine: Boolean,
    val type: Int
)