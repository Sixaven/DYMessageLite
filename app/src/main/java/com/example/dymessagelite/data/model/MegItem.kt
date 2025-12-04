package com.example.dymessagelite.data.model

data class MegItem(
    val id: String,
    val avatar: String,
    val name: String,
    val summary: String,
    val timestamp: String,
    var unreadCount: Int
)