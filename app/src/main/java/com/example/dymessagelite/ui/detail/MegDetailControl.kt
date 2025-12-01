package com.example.dymessagelite.ui.detail

import com.example.dymessagelite.common.observer.EventType
import com.example.dymessagelite.common.observer.Observer
import com.example.dymessagelite.data.model.ChatEntity
import com.example.dymessagelite.data.model.MegDetailCell
import com.example.dymessagelite.data.repository.ChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MegDetailControl(
    private val senderId: String,
    private val chatRepository: ChatRepository,
    private val view: MessageDetailView
) : Observer<List<MegDetailCell>> {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(job + Dispatchers.IO)

    fun onStart() {
        chatRepository.addObserver(this)
        scope.launch {
            chatRepository.getChatList(senderId)
        }

    }

    fun sendMessage(content: String) {
        scope.launch {
            val newMessage = MegDetailCell(
                content = content,
                timestamp = System.currentTimeMillis(),
                isMine = true,
                id = 0
            )
            chatRepository.sendMeg(newMessage, senderId)
        }
    }

    fun onStop(){
        job.cancel()
        chatRepository.removeObserver(this)
    }

    override fun update(data: List<MegDetailCell>, eventType: EventType) {
        when (eventType) {
            EventType.UPDATE_ALL_CHAT -> {
                view.displayChatList(data)
            }

            EventType.NEW_CHAT_SEND -> {
                view.displaySendMeg(data)
            }

            else -> {

            }
        }
    }
}