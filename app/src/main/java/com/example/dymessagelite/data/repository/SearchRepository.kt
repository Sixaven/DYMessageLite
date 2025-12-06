package com.example.dymessagelite.data.repository

import com.example.dymessagelite.common.observer.EventType
import com.example.dymessagelite.common.observer.Observer
import com.example.dymessagelite.common.observer.Subject
import com.example.dymessagelite.data.datasource.dao.ChatDao
import com.example.dymessagelite.data.datasource.dao.MegDao
import com.example.dymessagelite.data.model.list.MegEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchRepository(
    private val megDao: MegDao,
    private val chatDao: ChatDao

) : Subject<List<MegEntity>> {
    private val observers: MutableList<Observer<List<MegEntity>>> = mutableListOf()


    suspend fun search(keyword: String) {
        val resMegList = megDao.searchMegsByName(keyword)
        val resChatList = chatDao.searchChatsByContent(keyword)
        var chatToMegList: List<MegEntity> = listOf()
        if(resChatList != null) {
            chatToMegList = resChatList.map {
                val meg = megDao.getMegBySenderId(it.senderId)!!
                MegEntity(
                    id = meg.id,
                    avatar = meg.avatar,
                    name = meg.name,
                    latestMessage = it.content,
                    timestamp = it.timestamp,
                    unreadCount = meg.unreadCount,
                    type = meg.type
                )
            }
        }
        val tempMap = if(resMegList == null){
            chatToMegList.associateBy { it.id }
        }else{
            resMegList.associateBy { it.id } +
                    chatToMegList.associateBy { it.id }
        }

        val resList = tempMap.values.toList()
        val sortedList = resList.sortedByDescending { it.timestamp }
        withContext(Dispatchers.Main){
            notifyObservers(sortedList, EventType.SEARCH_MESSAGE)
        }
    }


    override fun addObserver(observer: Observer<List<MegEntity>>) {
        observers.add(observer)
    }

    override fun removeObserver(observer: Observer<List<MegEntity>>) {
        observers.remove(observer)
    }

    override fun notifyObservers(data: List<MegEntity>, eventType: EventType) {
        observers.forEach {
            it.update(data, eventType)
        }
    }
}