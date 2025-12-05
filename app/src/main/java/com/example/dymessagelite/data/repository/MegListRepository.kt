package com.example.dymessagelite.data.repository

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.dymessagelite.common.observer.EventType
import com.example.dymessagelite.common.observer.Observer
import com.example.dymessagelite.common.observer.Subject
import com.example.dymessagelite.data.datasource.dao.MegDao
import com.example.dymessagelite.data.datasource.database.ChatDatabase
import com.example.dymessagelite.data.model.MegEntity
import com.example.dymessagelite.data.model.MegItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import kotlin.concurrent.thread
import kotlin.math.min


class MegListRepository(
    private val megDao: MegDao
) : Subject<List<MegEntity>> {

    private var observers: MutableList<Observer<List<MegEntity>>> = mutableListOf()
    suspend fun getAllMeg(callback: (List<MegEntity>) -> Unit){
        val megEntities: List<MegEntity> = megDao.getMegList(0,0)
        withContext(Dispatchers.Main){
            callback(megEntities)
        }
    }
    suspend fun fetchMeg(page: Int, pageSize: Int) {
        megDao.getMegList(0,0)
        ChatDatabase.isDatabaseCreated.first { isDatabaseCreated -> isDatabaseCreated}
        Log.e("[databaseTest-get]",System.currentTimeMillis().toString())
        val megEntities: List<MegEntity> = megDao.getMegList(pageSize, (page - 1) * pageSize)
        if (megEntities.isEmpty()) {
            withContext(Dispatchers.Main) {
                notifyObservers(emptyList(), EventType.LOAD_IS_EMPTY)
            }
        } else {
            withContext(Dispatchers.Main) {
                notifyObservers(megEntities, EventType.LOAD_OR_GET_MESSAGE)
            }
        }
    }

    suspend fun jumpDetail(senderId: String){
        val oldMeg = megDao.getMegBySenderId(senderId)
        oldMeg?.let {
            it.unreadCount = 0
            megDao.insertOrUpdateMeg(oldMeg)
            val newMeg = listOf(oldMeg)
            withContext(Dispatchers.Main){
                notifyObservers(newMeg, eventType = EventType.JUMP_TO_DETAIL)
            }
        };
    }
    suspend fun search(keyword: String){
        val resList = megDao.searchMegsByName(keyword)
        withContext(Dispatchers.Main){
            notifyObservers(resList, EventType.SEARCH_MESSAGE)
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