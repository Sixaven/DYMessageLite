package com.example.dymessagelite.data.model.dashboard

data class DashboardData(
    val unreadCount: Int,
    val ctr: String,
    val textRecallRate: String,
    val imageRecallRate: String,
    val actionRecallRate: String
)