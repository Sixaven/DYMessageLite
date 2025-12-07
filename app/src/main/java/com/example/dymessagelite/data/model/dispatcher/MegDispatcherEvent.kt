package com.example.dymessagelite.data.model.dispatcher

import com.example.dymessagelite.data.model.detail.ChatEntity
import com.example.dymessagelite.data.model.list.MegEntity

data class MegDispatcherEvent(
    val meg: MegEntity,
    val chat: ChatEntity
)