package com.example.dymessagelite.data.model.detail

data class MegDetailCell(
    val id: Int,
    val content: String,
    val timestamp: String,
    val isMine: Boolean,
    val type: Int,
    var isDisplay: Boolean,
    val isClick: Boolean,
    var isRead: Boolean
)

object ChatMarkType{
    const val DISPLAY_OR_READ: Int = 0
    const val CLICK = 1
}

