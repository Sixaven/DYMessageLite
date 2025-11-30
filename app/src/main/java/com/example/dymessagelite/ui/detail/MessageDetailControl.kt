package com.example.dymessagelite.ui.detail

import com.example.dymessagelite.common.observer.EventType
import com.example.dymessagelite.common.observer.Observer
import com.example.dymessagelite.data.model.ChatEntity
import com.example.dymessagelite.data.model.MegDetailCell
import com.example.dymessagelite.data.repository.ChatRepository

class MessageDetailControl(
    private val senderId: String,
    private val chatRep: ChatRepository,
    private val view: MessageDetailView
): Observer<List<MegDetailCell>>{

    fun onStart(){
        chatRep.addObserver(this)
        chatRep.getChatList(senderId)
    }
    fun sendMessage(content: String){
        val newMessage = ChatEntity(
            senderId = senderId,
            content = content,
            timestamp = System.currentTimeMillis(),
            isMine = true
        )
        chatRep.sendMeg(newMessage)
    }

    override fun update(data: List<MegDetailCell>, eventType: EventType) {
        when(eventType){
            EventType.UPDATE_All_MESSAGE -> {
                view.displayChatList(data)
            }
            EventType.NEW_MESSAGE_SEND -> {
                view.displaySendMeg(data)
            }
            EventType.DEFAULT -> {

            }
        }
    }
}