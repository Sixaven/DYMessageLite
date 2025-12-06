package com.example.dymessagelite.data.model.detail

data class MegDetailCell(
    val id: Int,
    val content: String,
    val timestamp: String,
    val isMine: Boolean,
    val type: Int,
    val isDisplay: Boolean,
    val isClick: Boolean,
    val isRead: Boolean
)

object ChatMarkType{
    const val DISPLAY: Int = 0
    const val CLICK = 1
    const val READ = 2
}

