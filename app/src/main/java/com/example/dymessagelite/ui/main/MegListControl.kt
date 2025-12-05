package com.example.dymessagelite.ui.main

import com.example.dymessagelite.common.observer.EventType
import com.example.dymessagelite.common.observer.Observer
import com.example.dymessagelite.common.toMegDetailCell
import com.example.dymessagelite.common.toMegItem
import com.example.dymessagelite.common.toMegItems
import com.example.dymessagelite.common.tracker.AppStateTracker
import com.example.dymessagelite.data.datasource.database.ChatDatabase
import com.example.dymessagelite.data.model.ChatEntity
import com.example.dymessagelite.data.model.ChatEvent
import com.example.dymessagelite.data.model.MegDispatcherEvent
import com.example.dymessagelite.data.model.MegEntity
import com.example.dymessagelite.data.model.MegItem
import com.example.dymessagelite.data.repository.ChatRepository
import com.example.dymessagelite.data.repository.MegDispatcherRepository
import com.example.dymessagelite.data.repository.MegListRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch



class MegListControl(
    private val megListRepository: MegListRepository,
    private val megDispatcherRepository: MegDispatcherRepository,
    private val chatRepository: ChatRepository,
    private val view: MessageListView
){
    private var curPage = 1;
    private var pageSize = 20;
    private var isLoading = false;
    private var isLastPage = false;
    private val job = SupervisorJob()
    private val scope = CoroutineScope(job + Dispatchers.IO)
    private var senderId: String? = null;
    private var allMegList: MutableList<MegItem> = mutableListOf()
    private val searchList: MutableList<MegItem> = mutableListOf()

    private val listObserver = object : Observer<List<MegEntity>> {
        override fun update(data: List<MegEntity>, eventType: EventType) {
            val viewDataList = data.toMegItems()
            when(eventType) {
                EventType.LOAD_OR_GET_MESSAGE -> {
                    isLoading = false
                    val existingIds = allMegList.map { it.id }.toSet()
                    val newItems = viewDataList.filter { it.id !in existingIds }
                    allMegList.addAll(newItems)
                    view.getMegListOrLoadMore(allMegList)
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
                EventType.SEARCH_MESSAGE -> {
                    searchList.clear()
                    searchList.addAll(viewDataList)
                    view.displaySearchResult(searchList)
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

    fun onStart(){
        megListRepository.addObserver(this.listObserver)
        megDispatcherRepository.addObserver(this.dispatcherObserver)
        chatRepository.addObserver(this.chatObserver)
        isLoading = true;
        isLastPage = false;
        curPage = 1;
        scope.launch {
            megListRepository.fetchMeg(curPage, pageSize)
            megListRepository.getAllMeg { megEntities ->
                allMegList.addAll(megEntities.map { it.toMegItem() })
            }
        }
    }
    fun loadMore(){
        if (isLoading || isLastPage) return
        curPage++;
        isLoading = true;
        scope.launch {
            megListRepository.fetchMeg(curPage, pageSize)
        }
    }
    fun onStop(){
        job.cancel()
        megListRepository.removeObserver(this.listObserver)
        megDispatcherRepository.removeObserver(this.dispatcherObserver)
        chatRepository.removeObserver(this.chatObserver)
    }
    fun jumpDetail(senderId: String){
        this.senderId = senderId;
        scope.launch {
            megListRepository.jumpDetail(senderId)
        }
    }
    fun searchMeg(keyword: String){
        scope.launch {
            megListRepository.search(keyword)
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

}