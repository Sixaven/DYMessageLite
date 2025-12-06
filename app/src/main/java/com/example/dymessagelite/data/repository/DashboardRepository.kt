package com.example.dymessagelite.data.repository

import android.icu.text.DecimalFormat
import android.icu.util.Calendar
import com.example.dymessagelite.common.observer.EventType
import com.example.dymessagelite.common.observer.Observer
import com.example.dymessagelite.common.observer.Subject
import com.example.dymessagelite.data.datasource.dao.ChatDao
import com.example.dymessagelite.data.model.detail.ChatType
import com.example.dymessagelite.data.model.dashboard.DashboardData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class DashboardRepository (
    private val chatDao: ChatDao
): Subject<DashboardData>{

    private val observers: MutableList<Observer<DashboardData>> = mutableListOf()
    suspend fun getDashboardData(senderId: String) {
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val unreadCount = chatDao.getTodayUnreadCountBySenderId(senderId, startOfToday)

        val totalDisplayed = chatDao.getDisplayCountBySenderId(senderId)
        val totalClick = chatDao.getClickCountBySenderId(senderId)

        val ctr = calculateRate(totalClick, totalDisplayed)
        val textRecallRate = calculateRecallRateForType(senderId, ChatType.TEXT)
        val imageRecallRate = calculateRecallRateForType(senderId,ChatType.IMAGE)
        val actionRecallRate = calculateRecallRateForType(senderId,ChatType.ACTION)
        val data = DashboardData(
            unreadCount = unreadCount,
            ctr = ctr,
            textRecallRate = textRecallRate,
            imageRecallRate = imageRecallRate,
            actionRecallRate = actionRecallRate
        )
        withContext(Dispatchers.Main){
            notifyObservers(data,EventType.GET_DASHBOARD_DATA)
        }
    }
    private suspend fun calculateRecallRateForType(senderId: String,type: Int): String {
        val displayed = chatDao.getDisplayCountBySenderIdAndType(senderId,type)
        val all = chatDao.getAllCountBySenderIdAndType(senderId,type)
        return calculateRate(displayed, all)
    }
    private fun calculateRate(numerator: Int, denominator: Int): String {
        if (denominator == 0) {
            return "0.00%"
        }
        val rate = (numerator.toDouble() / denominator.toDouble()) * 100
        return DecimalFormat("0.00'%'").format(rate)
    }

    override fun addObserver(observer: Observer<DashboardData>) {
        observers.add(observer)
    }

    override fun removeObserver(observer: Observer<DashboardData>) {
        observers.remove(observer)
    }

    override fun notifyObservers(data: DashboardData, eventType: EventType) {
        observers.forEach {
            it.update(data,eventType)
        }
    }
}