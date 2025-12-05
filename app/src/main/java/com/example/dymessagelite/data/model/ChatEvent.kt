package com.example.dymessagelite.data.model

data class ChatEvent(
    val historyChat: List<ChatEntity>?,
    val sendChat: ChatEntity?,
    val updateMeg: MegEntity?
)