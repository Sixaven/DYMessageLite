package com.example.dymessagelite.data.repository

import android.os.Handler
import android.os.Looper
import com.example.dymessagelite.common.observer.EventType
import com.example.dymessagelite.common.observer.Observer
import com.example.dymessagelite.common.observer.Subject
import com.example.dymessagelite.data.datasource.json.MegLocalDataSource
import com.example.dymessagelite.data.model.MegItem

import kotlin.concurrent.thread
import kotlin.math.min

class MegListRepository(private val localDataSource: MegLocalDataSource): Subject<List<MegItem>>{

    private val mainHandler = Handler(Looper.getMainLooper())
    private var allMessages: List<MegItem>? = null

    private var observers: MutableList<Observer<List<MegItem>>> = mutableListOf()

    fun fetchMeg(page: Int,pageSize: Int) {
        thread {
            if (allMessages == null) {
                allMessages = localDataSource.loadAllMessage()
            }
            val resMessage = allMessages;
            if (resMessage.isNullOrEmpty()) {
                mainHandler.post {
                    notifyObservers(emptyList(), EventType.LOAD_IS_EMPTY)
                }
                return@thread
            }

            val startItemIndex = (page - 1) * pageSize;
            if (startItemIndex >= resMessage.size) {
                mainHandler.post {
                    notifyObservers(emptyList(),EventType.LOAD_IS_EMPTY)
                }
                return@thread
            }
            val endItemIndex = min(startItemIndex + pageSize, resMessage.size)
            val subList = resMessage.subList(startItemIndex, endItemIndex)
            mainHandler.post {
                notifyObservers(subList,EventType.LOAD_OR_GET_MESSAGE)
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
            it.update(data,eventType)
        }

    }
}