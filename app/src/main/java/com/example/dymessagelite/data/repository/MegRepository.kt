package com.example.dymessagelite.data.repository

import android.os.Handler
import android.os.Looper
import com.example.dymessagelite.data.datasource.MegLocalDataSource
import com.example.dymessagelite.data.model.MegItem

import kotlin.concurrent.thread
import kotlin.math.min

class MegRepository(private val localDataSource: MegLocalDataSource) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var allMessages: List<MegItem>? = null

    fun fetchMeg(page: Int,pageSize: Int,callback: (List<MegItem>) -> Unit) {
        thread {
            if (allMessages == null) {
                allMessages = localDataSource.loadAllMessage()
            }
            val resMessage = allMessages;
            if (resMessage.isNullOrEmpty()) {
                mainHandler.post {
                    callback(emptyList())
                }
                return@thread
            }

            val startItemIndex = (page - 1) * pageSize;
            if (startItemIndex >= resMessage.size) {
                mainHandler.post {
                    callback(emptyList())
                }
                return@thread
            }
            val endItemIndex = min(startItemIndex + pageSize, resMessage.size)
            val subList = resMessage.subList(startItemIndex, endItemIndex)
            mainHandler.post {
                callback(subList)
            }
        }
    }
}