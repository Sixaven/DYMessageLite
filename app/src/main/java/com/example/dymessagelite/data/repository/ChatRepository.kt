package com.example.dymessagelite.data.repository

import com.example.dymessagelite.common.observer.EventType
import com.example.dymessagelite.common.observer.Observer
import com.example.dymessagelite.common.observer.Subject
import com.example.dymessagelite.data.datasource.dao.ChatDao
import com.example.dymessagelite.data.datasource.dao.MegDao
import com.example.dymessagelite.data.datasource.database.ChatDatabase
import com.example.dymessagelite.data.model.detail.ChatEntity
import com.example.dymessagelite.data.model.detail.ChatEvent
import com.example.dymessagelite.data.model.detail.ChatMarkType
import com.example.dymessagelite.data.model.list.MegEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatRepository private constructor(
    private val chatDao: ChatDao,
    private val megDao: MegDao
) : Subject<ChatEvent> {
    private var observers: MutableList<Observer<ChatEvent>> = mutableListOf()

    private val scope = CoroutineScope(Dispatchers.IO)


    fun getChatList(senderId: String) {
        scope.launch {
            chatDao.getChatList("");
            ChatDatabase.isDatabaseCreated.first { isDatabaseCreated -> isDatabaseCreated }
            val res = chatDao.getChatList(senderId)
            val chatEvent = ChatEvent(res, null, null)
            notifyObservers(chatEvent, EventType.UPDATE_ALL_CHAT)
        }
    }

    fun sendMeg(meg: ChatEntity) {
        scope.launch {
            val oldMeg = megDao.getMegBySenderId(meg.senderId);
            oldMeg?.apply {

                val newMeg = MegEntity(
                    id = oldMeg.id,
                    avatar = oldMeg.avatar,
                    name = oldMeg.name,
                    latestMessage = meg.content,
                    timestamp = meg.timestamp,
                    unreadCount = oldMeg.unreadCount,
                    type = meg.type,
                    remark = oldMeg.remark
                )
                chatDao.insertChat(meg)
                megDao.insertOrUpdateMeg(newMeg)
                val chatEvent = ChatEvent(null, meg, newMeg)
                withContext(Dispatchers.Main) {
                    notifyObservers(chatEvent, EventType.SEND_CHAT_MINE)
                }
            }
        }
    }

    fun markChat(chatId: Int, markType: Int) {
        scope.launch {
            val oldChat = chatDao.getChatById(chatId)
            if (oldChat != null) {
                when (markType) {

                    ChatMarkType.CLICK -> {
                        val newChat = oldChat.copy(isClick = true)
                        chatDao.updateChat(newChat)
                    }

                    ChatMarkType.DISPLAY_OR_READ -> {
                        val newChat = oldChat.copy(isRead = true, isDisplay = true)
                        chatDao.updateChat(newChat)
                    }
                    else -> {}
                }
                withContext(Dispatchers.Main){
                    notifyObservers(
                        ChatEvent(null, null, null),
                        EventType.DASHBOARD_DATA_UPDATE
                    )
                }
            } else {
                throw Exception("Chat not found")
            }
        }
    }

    override fun addObserver(observer: Observer<ChatEvent>) {
        observers.add(observer)
    }

    override fun removeObserver(observer: Observer<ChatEvent>) {
        observers.remove(observer)
    }

    override fun notifyObservers(data: ChatEvent, eventType: EventType) {
        observers.forEach {
            it.update(data, eventType)
        }
    }

    companion object {
        private var INSTANCE: ChatRepository? = null;
        fun getInstance(chatDao: ChatDao, megDao: MegDao): ChatRepository {
            return INSTANCE ?: ChatRepository(chatDao, megDao).also { INSTANCE = it }
        }
    }
}