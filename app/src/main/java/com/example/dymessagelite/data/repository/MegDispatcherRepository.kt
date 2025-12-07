package com.example.dymessagelite.data.repository

import android.content.Context
import com.example.dymessagelite.common.observer.EventType
import com.example.dymessagelite.common.observer.Observer
import com.example.dymessagelite.common.observer.Subject
import com.example.dymessagelite.common.tracker.AppStateTracker
import com.example.dymessagelite.common.util.JsonUtils
import com.example.dymessagelite.data.datasource.dao.ChatDao
import com.example.dymessagelite.data.datasource.dao.MegDao
import com.example.dymessagelite.data.model.detail.ChatEntity
import com.example.dymessagelite.data.model.detail.ChatType
import com.example.dymessagelite.data.model.dispatcher.MegDispatcherEvent
import com.example.dymessagelite.data.model.list.MegEntity
import com.example.dymessagelite.data.model.list.MegType
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.random.Random

class MegDispatcherRepository private constructor(
    private val megDao: MegDao,
    private val chatDao: ChatDao,
    private val senderIdes: List<String>
) : Subject<MegDispatcherEvent> {
    private var count: Int = 0;
    private val chatTypes = listOf(ChatType.IMAGE, ChatType.TEXT)
    private val megTypes = listOf(MegType.IMAGE, MegType.TEXT, MegType.ACTION)
    private var observers: MutableList<Observer<MegDispatcherEvent>> = mutableListOf()

    suspend fun onStart(){
        while (true){
            delay(3000)
            withContext(NonCancellable){
                var content: String =  ""
                val randomSenderId = senderIdes[Random.nextInt(senderIdes.size)]
                val randomMegType = megTypes[Random.nextInt(megTypes.size)]
                when(randomMegType){
                    MegType.ACTION -> content = "点击领取"
                    MegType.TEXT -> content = "这是第${count++}条新产生的消息"
                    MegType.IMAGE -> content = "这是图片"
                }

                val oldMeg = megDao.getMegBySenderId(randomSenderId)
                oldMeg?.apply {
                    var isRead = false;
                    val curActivity = AppStateTracker.getCurActivity()
                    if(curActivity == AppStateTracker.CurrentActivity.MESSAGE_DETAIL){
                        val curSenderId = AppStateTracker.getCurDetailSenderId()
                        curSenderId?.apply {
                            if(curSenderId == oldMeg.name){
                                isRead = true
                            }
                        }
                    }
                    if(!isRead){
                        oldMeg.unreadCount++;
                    }
                    val megEntity = MegEntity(
                        id = oldMeg.id ,
                        avatar = oldMeg.avatar,
                        name = oldMeg.name,
                        latestMessage = content,
                        timestamp = System.currentTimeMillis(),
                        unreadCount = oldMeg.unreadCount,
                        type = randomMegType,
                        remark = oldMeg.remark
                    )
                    val chatEntity = ChatEntity(
                        senderId = oldMeg.name,
                        isMine = false,
                        timestamp = System.currentTimeMillis(),
                        content = content,
                        type = randomMegType,
                        isRead = false,
                        isClick = false,
                        isDisplay = false
                    )

                    megDao.insertOrUpdateMeg(megEntity)
                    chatDao.insertChat(chatEntity)

                    val megDispatcherEvent = MegDispatcherEvent(megEntity,chatEntity)
                    withContext(Dispatchers.Main){
                        notifyObservers(megDispatcherEvent, EventType.SEND_CHAT_OTHER)
                    }
                }
            }
        }
    }

    override fun addObserver(observer:Observer<MegDispatcherEvent>) {
        observers.add(observer)
    }

    override fun removeObserver(observer: Observer<MegDispatcherEvent>) {
        observers.remove(observer)
    }

    override fun notifyObservers(data: MegDispatcherEvent, eventType: EventType) {
        observers.forEach {
            it.update(data, eventType)
        }
    }

    companion object{
        private var INSTANCE: MegDispatcherRepository? = null;

        private fun loadSenderIdes(context: Context):List<String>{
            val senderIdes: List<String> = JsonUtils.loadJsonData(
                context,
                "megData.json",
                object : TypeToken<List<MegEntity>>() {}
            ).map{it.name}
            return senderIdes;
        }
        fun getInstance(megDao: MegDao, chatDao: ChatDao, context: Context): MegDispatcherRepository {
            return INSTANCE ?: run {
                val senderIdes = loadSenderIdes(context)
                MegDispatcherRepository(megDao,chatDao,senderIdes).also{INSTANCE = it}
            }
        }
    }
}