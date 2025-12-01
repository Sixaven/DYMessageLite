package com.example.dymessagelite.data.repository

import android.os.Handler
import android.os.Looper
import com.example.dymessagelite.common.observer.EventType
import com.example.dymessagelite.common.observer.Observer
import com.example.dymessagelite.common.observer.Subject
import com.example.dymessagelite.data.datasource.dao.MegDao
import com.example.dymessagelite.data.model.MegEntity
import com.example.dymessagelite.data.model.MegItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import kotlin.concurrent.thread
import kotlin.math.min

fun MegEntity.toMegItem(): MegItem {
    return MegItem(
        id = friendId,
        headId = headId,
        name = friendName,
        summary = latestMessage,
        timestamp = timestamp.toString(),
        unreadCount = unreadCount
    )
}

fun List<MegEntity>.toMegItems(): List<MegItem> {
    return this.map {
        it.toMegItem()
    }
}

class MegListRepository(
    private val megDao: MegDao
) : Subject<List<MegItem>> {

    private var observers: MutableList<Observer<List<MegItem>>> = mutableListOf()


    suspend fun fetchMeg(page: Int, pageSize: Int) {

        val megEntities: List<MegEntity> = megDao.getMegList(pageSize, (page - 1) * pageSize)
        if (megEntities.isEmpty()) {
            withContext(Dispatchers.Main) {
                notifyObservers(emptyList(), EventType.LOAD_IS_EMPTY)
            }
        } else {
            val megItems = megEntities.toMegItems();
            withContext(Dispatchers.Main) {
                notifyObservers(megItems, EventType.LOAD_OR_GET_MESSAGE)
            }
        }

    }

    override fun addObserver(observer: Observer<List<MegItem>>) {
        observers.add(observer)
    }

    override fun removeObserver(observer: Observer<List<MegItem>>) {
        observers.remove(observer)
    }

    override fun notifyObservers(data: List<MegItem>, eventType: EventType) {
        observers.forEach {
            it.update(data, eventType)
        }

    }
}