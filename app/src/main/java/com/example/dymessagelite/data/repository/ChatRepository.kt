package com.example.dymessagelite.data.repository

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.dymessagelite.common.observer.EventType
import com.example.dymessagelite.common.observer.Observer
import com.example.dymessagelite.common.observer.Subject
import com.example.dymessagelite.data.datasource.dao.ChatDao
import com.example.dymessagelite.data.datasource.dao.MegDao
import com.example.dymessagelite.data.datasource.database.ChatDatabase
import com.example.dymessagelite.data.model.ChatEntity
import com.example.dymessagelite.data.model.ChatEvent
import com.example.dymessagelite.data.model.MegDetailCell
import com.example.dymessagelite.data.model.MegEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.concurrent.thread

class ChatRepository private constructor(
    private val chatDao: ChatDao,
    private val megDao: MegDao
) : Subject<ChatEvent> {
    private var observers: MutableList<Observer<ChatEvent>> = mutableListOf()


    suspend fun getChatList(senderId: String) {
        chatDao.getChatList("");
        ChatDatabase.isDatabaseCreated.first { isDatabaseCreated -> isDatabaseCreated}
        val res = chatDao.getChatList(senderId)
        val chatEvent = ChatEvent(res, null,null)
        notifyObservers(chatEvent, EventType.UPDATE_ALL_CHAT)
    }

    suspend fun sendMeg(meg: ChatEntity) {
        val oldMeg = megDao.getMegBySenderId(meg.senderId);
        oldMeg?.apply {

            val newMeg = MegEntity(
                id = oldMeg.id,
                avatar = oldMeg.avatar,
                name = oldMeg.name,
                latestMessage = meg.content,
                timestamp = meg.timestamp,
                unreadCount = oldMeg.unreadCount,
                type = meg.type
            )
            chatDao.insertChat(meg)
            megDao.insertOrUpdateMeg(newMeg)
            val chatEvent = ChatEvent(null, meg,newMeg)
            withContext(Dispatchers.Main) {
                notifyObservers(chatEvent, EventType.SEND_CHAT_MINE)
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
    companion object{
        private var INSTANCE: ChatRepository? = null;
        fun getInstance(chatDao: ChatDao, megDao: MegDao): ChatRepository {
            return INSTANCE ?:
            ChatRepository(chatDao,megDao).also{INSTANCE = it}
        }
    }
}