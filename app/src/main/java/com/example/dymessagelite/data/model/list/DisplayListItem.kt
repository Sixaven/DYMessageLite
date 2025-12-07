package com.example.dymessagelite.data.model.list

data class DisplayListItem(
    val id: String,
    val avatar: String,
    val name: String,
    val context: String,
    val timestamp: String,
    val contentType: Int,
    val unreadCount: Int,
    val displayType: Int,
    val remark: String?
)
object DisplayType {
    const val DEFAULT = 0
    const val SEARCH = 1
}



