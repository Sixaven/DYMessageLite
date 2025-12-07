package com.example.dymessagelite.data.repository

import android.icu.text.DecimalFormat
import android.icu.util.Calendar
import android.util.Log
import com.example.dymessagelite.common.observer.EventType
import com.example.dymessagelite.common.observer.Observer
import com.example.dymessagelite.common.observer.Subject
import com.example.dymessagelite.data.datasource.dao.ChatDao
import com.example.dymessagelite.data.datasource.dao.MegDao
import com.example.dymessagelite.data.model.detail.ChatType
import com.example.dymessagelite.data.model.dashboard.DashboardData
import com.example.dymessagelite.data.model.dashboard.DashboardEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class DashboardRepository private constructor(
    private val chatDao: ChatDao,
    private val megDao: MegDao
): Subject<DashboardEvent>{
    private val scope = CoroutineScope( Dispatchers.IO)
    private val observers: MutableList<Observer<DashboardEvent>> = mutableListOf()

     fun saveRemark(remark:String, senderId: String){
         scope.launch {
             val oldMeg = megDao.getMegBySenderId(senderId)
             if(oldMeg != null){
                 val newMeg = oldMeg.copy(remark = remark)
                 megDao.insertOrUpdateMeg(newMeg)
                 withContext(Dispatchers.Main){
                     val data = DashboardEvent(null,remark,senderId)
                     notifyObservers(data,EventType.UPDATE_NICKNAME)
                 }
             }else{
                 throw Exception("senderId不存在")
             }
         }
    }
     fun getDashboardData(senderId: String) {
         scope.launch {
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
             Log.e("[dash]","当前的sender：${senderId} 显示总数：${totalDisplayed} 点击总数: $totalClick" +
                     "未读数：$unreadCount")
             withContext(Dispatchers.Main){
                 val data = DashboardEvent(data,null,senderId)
                 notifyObservers(data,EventType.GET_DASHBOARD_DATA)
             }
         }
    }
    private suspend fun calculateRecallRateForType(senderId: String,type: Int): String {
        val displayed = chatDao.getDisplayCountBySenderIdAndType(senderId,type)
        val all = chatDao.getAllCountBySenderIdAndType(senderId,type)
        Log.e("[dash]","sender:$senderId type: $type 消息总数：$all 显示总数：$displayed ")
        return calculateRate(displayed, all)
    }
    private fun calculateRate(numerator: Int, denominator: Int): String {
        if (denominator == 0 ) {
            return "0.00%"
        }
        val rate = (numerator.toDouble() / denominator.toDouble()) * 100
        return DecimalFormat("0.00'%'").format(rate)
    }

    override fun addObserver(observer: Observer<DashboardEvent>) {
        observers.add(observer)
    }

    override fun removeObserver(observer: Observer<DashboardEvent>) {
        observers.remove(observer)
    }

    override fun notifyObservers(data: DashboardEvent, eventType: EventType) {
        observers.forEach {
            it.update(data,eventType)
        }
    }
    companion object {
        private var INSTANCE: DashboardRepository? = null;
        fun getInstance(chatDao: ChatDao, megDao: MegDao): DashboardRepository {
            return INSTANCE ?: DashboardRepository(chatDao, megDao).also { INSTANCE = it }
        }
    }
}