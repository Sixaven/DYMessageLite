package com.example.dymessagelite.ui.detail

import com.example.dymessagelite.common.observer.EventType
import com.example.dymessagelite.common.observer.Observer
import com.example.dymessagelite.common.toMegDetailCell
import com.example.dymessagelite.common.toMegDetailCellList
import com.example.dymessagelite.common.tracker.AppStateTracker
import com.example.dymessagelite.data.model.dashboard.DashboardEvent
import com.example.dymessagelite.data.model.detail.ChatEntity
import com.example.dymessagelite.data.model.detail.ChatEvent
import com.example.dymessagelite.data.model.detail.ChatMarkType
import com.example.dymessagelite.data.model.detail.ChatType
import com.example.dymessagelite.data.model.detail.MegDetailCell
import com.example.dymessagelite.data.model.dispatcher.MegDispatcherEvent
import com.example.dymessagelite.data.repository.ChatRepository
import com.example.dymessagelite.data.repository.DashboardRepository
import com.example.dymessagelite.data.repository.MegDispatcherRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


class MegDetailControl(
    private val senderId: String,
    private val chatRepository: ChatRepository,
    private val megDispatcherRepository: MegDispatcherRepository,
    private val dashboardRepository: DashboardRepository,
    private val view: MessageDetailView
) {


    private val chatObserver = object : Observer<ChatEvent> {
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
    private val dispatcherObserver = object : Observer<MegDispatcherEvent> {
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

    private val dashboardObserver = object : Observer<DashboardEvent> {
        override fun update(data: DashboardEvent, eventType: EventType) {
            when (eventType) {
                EventType.UPDATE_NICKNAME -> {
                    data.remark?.apply {
                        view.updateNickName(data.remark)
                    }
                }
                else -> {}
            }
        }
    }

    fun onStart() {
        chatRepository.addObserver(this.chatObserver)
        megDispatcherRepository.addObserver(this.dispatcherObserver)
        dashboardRepository.addObserver(dashboardObserver)

        chatRepository.getChatList(senderId)
    }

    fun sendMessage(content: String) {

        val newMessage = ChatEntity(
            content = content,
            timestamp = System.currentTimeMillis(),
            isMine = true,
            senderId = senderId,
            type = ChatType.TEXT,
            isDisplay = false,
            isClick = false,
            isRead = false
        )
        chatRepository.sendMeg(newMessage)

    }

    fun sendChatOther(data: MegDetailCell) {
        val curActivity = AppStateTracker.getCurActivity()
        when (curActivity) {
            AppStateTracker.CurrentActivity.MESSAGE_DETAIL -> {
                val senderId = AppStateTracker.getCurDetailSenderId();
                senderId?.apply {
                    if (senderId == this@MegDetailControl.senderId) {
                        view.displaySendMeg(data)
                    }
                }
            }

            else -> {}
        }
    }

    fun markAsDisplayAndRead(chatId: Int) {

        chatRepository.markChat(chatId, ChatMarkType.DISPLAY_OR_READ)

    }


    fun markAsClick(chatId: Int) {

        chatRepository.markChat(chatId, ChatMarkType.CLICK)

    }

    fun onStop() {

        chatRepository.removeObserver(this.chatObserver)
        megDispatcherRepository.removeObserver(this.dispatcherObserver)
        dashboardRepository.removeObserver(dashboardObserver)
    }

}