package com.example.dymessagelite.ui.detail

import com.example.dymessagelite.common.observer.EventType
import com.example.dymessagelite.common.observer.Observer
import com.example.dymessagelite.common.toMegDetailCell
import com.example.dymessagelite.common.toMegDetailCellList
import com.example.dymessagelite.common.tracker.AppStateTracker
import com.example.dymessagelite.data.datasource.database.ChatDatabase
import com.example.dymessagelite.data.model.ChatEntity
import com.example.dymessagelite.data.model.ChatEvent
import com.example.dymessagelite.data.model.MegDetailCell
import com.example.dymessagelite.data.model.MegDispatcherEvent
import com.example.dymessagelite.data.repository.ChatRepository
import com.example.dymessagelite.data.repository.MegDispatcherRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch





class MegDetailControl(
    private val senderId: String,
    private val chatRepository: ChatRepository,
    private val megDispatcherRepository: MegDispatcherRepository,
    private val view: MessageDetailView
) {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(job + Dispatchers.IO)

    private val chatObserver = object : Observer<ChatEvent>{
        override fun update(data: ChatEvent, eventType: EventType) {
            when (eventType) {
                EventType.UPDATE_ALL_CHAT -> {
                    data.historyChat?.apply {
                        val viewDataList = data.historyChat
                        view.displayChatList(viewDataList.toMegDetailCellList())
                    }
                }

                EventType.SEND_CHAT_MINE -> {
                    data.sendChat?.apply {
                        val viewData = data.sendChat
                        view.displaySendMeg(viewData.toMegDetailCell())
                    }
                }
                else -> {

                }
            }
        }
    }
    private val dispatcherObserver = object : Observer<MegDispatcherEvent>{
        override fun update(data: MegDispatcherEvent, eventType: EventType) {
            val viewData = data.chat.toMegDetailCell()
            when (eventType) {
                EventType.SEND_CHAT_OTHER -> {
                    sendChatOther(viewData)
                }
                else -> {

                }
            }
        }
    }

    fun onStart() {
        chatRepository.addObserver(this.chatObserver)
        megDispatcherRepository.addObserver(this.dispatcherObserver)
        scope.launch {
            chatRepository.getChatList(senderId)
        }
    }

    fun sendMessage(content: String) {
        scope.launch {
            val newMessage = ChatEntity(
                content = content,
                timestamp = System.currentTimeMillis(),
                isMine = true,
                senderId = senderId
            )
            chatRepository.sendMeg(newMessage)
        }
    }

    fun sendChatOther(data: MegDetailCell){
        val curActivity = AppStateTracker.getCurActivity()
        when(curActivity){
            AppStateTracker.CurrentActivity.MESSAGE_DETAIL -> {
                val senderId = AppStateTracker.getCurDetailSenderId();
                senderId?.apply {
                    if(senderId == this@MegDetailControl.senderId){
                        view.displaySendMeg(data)
                    }
                }
            }
            else -> {}
        }
    }

    fun onStop(){
        job.cancel()
        chatRepository.removeObserver(this.chatObserver)
        megDispatcherRepository.removeObserver(this.dispatcherObserver)
    }

}