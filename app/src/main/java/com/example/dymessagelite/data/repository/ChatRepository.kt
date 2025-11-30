package com.example.dymessagelite.data.repository

import android.os.Handler
import android.os.Looper
import com.example.dymessagelite.common.observer.EventType
import com.example.dymessagelite.common.observer.Observer
import com.example.dymessagelite.common.observer.Subject
import com.example.dymessagelite.data.datasource.dao.ChatDao
import com.example.dymessagelite.data.model.ChatEntity
import com.example.dymessagelite.data.model.MegDetailCell
import kotlin.concurrent.thread

fun ChatEntity.toMegDetailCell(): MegDetailCell {
    return MegDetailCell(
        id = this.id,
        content = this.content,
        timestamp = this.timestamp,
        isMine = this.isMine
    )
}

fun List<ChatEntity>.toMegDetailCellList(): List<MegDetailCell> {
    return this.map {
        it.toMegDetailCell()
    }
}

class ChatRepository(private val chatDao: ChatDao) : Subject<List<MegDetailCell>> {
    private var observers: MutableList<Observer<List<MegDetailCell>>> = mutableListOf()
    private val mainHandler = Handler(Looper.getMainLooper())
    fun getChatList(senderId: String) {
        thread {
            val res = chatDao.getChatList(senderId)
            val chatList = res.toMegDetailCellList()
            mainHandler.post {
                notifyObservers(chatList, EventType.UPDATE_ALL_CHAT)
            }
        }
    }

    fun sendMeg(meg: ChatEntity) {
        thread {
            chatDao.insertChat(meg)
            val chat = meg.toMegDetailCell()
            val chatList = listOf(chat)
            mainHandler.post {
                notifyObservers(chatList, EventType.NEW_CHAT_SEND)
            }
        }
    }

    override fun addObserver(observer: Observer<List<MegDetailCell>>) {
        observers.add(observer)
    }

    override fun removeObserver(observer: Observer<List<MegDetailCell>>) {
        observers.remove(observer)
    }

    override fun notifyObservers(data: List<MegDetailCell>, eventType: EventType) {
        observers.forEach {
            it.update(data, eventType)
        }
    }
}