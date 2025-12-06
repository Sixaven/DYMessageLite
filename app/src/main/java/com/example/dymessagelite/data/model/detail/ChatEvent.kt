package com.example.dymessagelite.data.model.detail

import com.example.dymessagelite.data.model.list.MegEntity

data class ChatEvent(
    val historyChat: List<ChatEntity>?,
    val sendChat: ChatEntity?,
    val updateMeg: MegEntity?
)