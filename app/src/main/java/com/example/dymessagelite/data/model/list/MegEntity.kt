package com.example.dymessagelite.data.model.list

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
object MegType {
    const val TEXT = 0
    const val IMAGE = 1
    const val ACTION = 2
}


@Entity
data class MegEntity (
    @PrimaryKey
    val id: String,
    val avatar: String,
    val name: String,
    val latestMessage: String,
    val timestamp: Long,
    var unreadCount: Int,
    @ColumnInfo(defaultValue = "0")
    val type: Int,
    @ColumnInfo(defaultValue = "NULL")
    val remark: String? = null
)


