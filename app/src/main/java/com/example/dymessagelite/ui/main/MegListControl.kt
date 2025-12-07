package com.example.dymessagelite.ui.main

import com.example.dymessagelite.common.observer.EventType
import com.example.dymessagelite.common.observer.Observer
import com.example.dymessagelite.common.toMegItem
import com.example.dymessagelite.common.toMegItems
import com.example.dymessagelite.common.tracker.AppStateTracker
import com.example.dymessagelite.data.model.dashboard.DashboardEvent
import com.example.dymessagelite.data.model.detail.ChatEvent
import com.example.dymessagelite.data.model.dispatcher.MegDispatcherEvent
import com.example.dymessagelite.data.model.list.MegEntity
import com.example.dymessagelite.data.model.list.MegItem
import com.example.dymessagelite.data.repository.ChatRepository
import com.example.dymessagelite.data.repository.DashboardRepository
import com.example.dymessagelite.data.repository.MegDispatcherRepository
import com.example.dymessagelite.data.repository.MegListRepository
import com.example.dymessagelite.data.repository.SearchRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch



class MegListControl(
    private val megListRepository: MegListRepository,
    private val megDispatcherRepository: MegDispatcherRepository,
    private val chatRepository: ChatRepository,
    private val searchRepository: SearchRepository,
    private val dashboardRepository: DashboardRepository,
    private val view: MessageListView
){
    private var curPage = 1;
    private var pageSize = 20;
    private var isLoading = false;
    private var isLastPage = false;

    private var keyword: String = ""
    private val job = SupervisorJob()
    private val scope = CoroutineScope(job + Dispatchers.IO)
    private var senderId: String? = null;
    private var allMegList: MutableList<MegItem> = mutableListOf()
    private val searchList: MutableList<MegItem> = mutableListOf()

    private val listObserver = object : Observer<List<MegEntity>> {
        override fun update(data: List<MegEntity>, eventType: EventType) {
            val viewDataList = data.toMegItems()
            if(viewDataList.isNotEmpty()) curPage++;
            when(eventType) {
                EventType.FIRST_GET_MESSAGE -> {
                    isLoading = false
                    val existingIds = allMegList.map { it.id }.toSet()
                    val newItems = viewDataList.filter { it.id !in existingIds }
                    allMegList.addAll(newItems)
                    view.firstGetMegList(allMegList)
                }
                EventType.LOAD_MORE_MESSAGE -> {
                    isLoading = false
                    isLoading = false
                    val existingIds = allMegList.map { it.id }.toSet()
                    val newItems = viewDataList.filter { it.id !in existingIds }
                    allMegList.addAll(newItems)
                    view.loadMoreMegList(allMegList)
                }
                EventType.LOAD_IS_EMPTY -> {
                    isLastPage = true;
                    isLoading = false;
                    view.loadEmpty()
                }
                EventType.JUMP_TO_DETAIL -> {
                    val meg = viewDataList[0];
                    updateItemInPlace(meg)
                    view.jumpDetail(allMegList)
                }
                else -> {}
            }
        }
    }

    private val dispatcherObserver = object : Observer<MegDispatcherEvent> {
        override fun update(data: MegDispatcherEvent, eventType: EventType) {
            val viewDate = data.meg.toMegItem()
            when(eventType){
                EventType.SEND_CHAT_OTHER -> {
                    sendChatOther(viewDate)
                }
                else -> {

                }
            }
        }
    }

    private val chatObserver = object : Observer<ChatEvent>{
        override fun update(data: ChatEvent, eventType: EventType) {
            when (eventType){
                EventType.SEND_CHAT_MINE -> {
                    data.updateMeg?.apply {
                        val viewData = data.updateMeg.toMegItem()
                        sendChatMine(viewData)
                    }
                }
                else -> {}
            }
        }
    }

    private val searchObserver = object : Observer<List<MegEntity>>{
        override fun update(data: List<MegEntity>, eventType: EventType) {
            when(eventType){
                EventType.SEARCH_MESSAGE -> {
                    searchList.clear()
                    searchList.addAll(data.toMegItems())
                    view.displaySearchResult(searchList,keyword)
                }
                else -> {}
            }
        }
    }
    
    private val dashboardObserver = object : Observer<DashboardEvent>{
        override fun update(data: DashboardEvent, eventType: EventType) {
            when (eventType) {
                EventType.UPDATE_NICKNAME -> {
                    val oldIndex = allMegList.indexOfFirst { it.name == data.senderId }
                    if(oldIndex != -1){
                        allMegList[oldIndex].remark = data.remark
                        view.updateNickName(allMegList)
                    }else{
                        throw Exception("updateItemBySenderId error: item with id ${data.senderId} not found")
                    }
                }
                else -> {}
            }
        }
    }

    fun onStart(){
        megListRepository.addObserver(this.listObserver)
        megDispatcherRepository.addObserver(this.dispatcherObserver)
        chatRepository.addObserver(this.chatObserver)
        searchRepository.addObserver(this.searchObserver)
        dashboardRepository.addObserver(dashboardObserver)
        isLoading = true;
        isLastPage = false;
        curPage = 1;
        firstGet()
    }
    fun firstGet(){
        scope.launch {
            megListRepository.fetchMeg(1, pageSize)
        }
    }
    fun loadMore(){
        if (isLoading || isLastPage) return
        isLoading = true;
        scope.launch {
            megListRepository.fetchMeg(curPage, pageSize)
        }
    }
    fun onStop(){
        job.cancel()
        megListRepository.removeObserver(this.listObserver)
        searchRepository.removeObserver(this.searchObserver)
        megDispatcherRepository.removeObserver(this.dispatcherObserver)
        chatRepository.removeObserver(this.chatObserver)
        dashboardRepository.removeObserver(dashboardObserver)
    }
    fun jumpDetail(senderId: String){
        this.senderId = senderId;
        scope.launch {
            megListRepository.jumpDetail(senderId)
        }
    }
    fun searchMeg(keyword: String){
        this.keyword = keyword
        scope.launch {
            searchRepository.search(this@MegListControl.keyword)
        }
    }
    fun backFromSearch(){
        view.backFromSearch(allMegList)
    }
    fun sendChatOther(data: MegItem){
        val curActivity = AppStateTracker.getCurActivity()
        when(curActivity){
            AppStateTracker.CurrentActivity.MESSAGE_DETAIL -> {
                val senderId = AppStateTracker.getCurDetailSenderId();
                senderId?.apply {
                    if(senderId != data.name){
                        updateAndMoveToTop(data)
                        view.receiveMegChangeByOther(allMegList)
                    }
                }
            }

            AppStateTracker.CurrentActivity.MESSAGE_LIST->{
                updateAndMoveToTop(data)
                view.receiveMegChangeByOther(allMegList)
            }
            else -> {

            }
        }
    }
    fun sendChatMine(data: MegItem){
        updateItemInPlace(data)
        view.receiveMegChangeByMine(allMegList)
    }

    fun updateItemInPlace(newItem: MegItem) {
        val index = allMegList.indexOfFirst { it.id == newItem.id }
        if (index != -1) {
            allMegList[index] = newItem
        }else{
            throw Exception("updateItemInPlace error")
        }
    }
    fun updateAndMoveToTop( newItem: MegItem) {
        val oldIndex = allMegList.indexOfFirst { it.id == newItem.id }
        if (oldIndex != -1) {
            allMegList.removeAt(oldIndex)
        }
        allMegList.add(0, newItem)
    }

    fun updateItemBySenderId(senderId: String, newSummary: String? = null, newTimestamp: String? = null, newUnreadCount: Int? = null) {
        // 1. 根据 senderId 找到 item 在列表中的索引
        val index = allMegList.indexOfFirst { it.id == senderId }

        // 2. 如果找到了 (index != -1)
        if (index != -1) {
            // 3. 获取旧的 item
            val oldItem = allMegList[index]

            // 4. 使用 copy 方法创建新 item，只修改需要变的属性
            //    如果某个参数为 null，copy 方法会保留旧值
            val newItem = oldItem.copy(
                summary = newSummary ?: oldItem.summary,
                timestamp = newTimestamp ?: oldItem.timestamp,
                unreadCount = newUnreadCount ?: oldItem.unreadCount
            )

            // 5. 用新 item 替换掉列表中的旧 item
            allMegList[index] = newItem
        } else {
            // 如果没找到，可以根据需要抛出异常或打印日志
            // throw Exception("updateItemBySenderId error: item with id $senderId not found")
        }
    }

}
