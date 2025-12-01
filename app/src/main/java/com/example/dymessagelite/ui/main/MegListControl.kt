package com.example.dymessagelite.ui.main

import com.example.dymessagelite.common.observer.EventType
import com.example.dymessagelite.common.observer.Observer
import com.example.dymessagelite.data.model.MegItem
import com.example.dymessagelite.data.repository.MegListRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MegListControl(
    private val megListRepository: MegListRepository,
    private val view: MessageListView
): Observer<List<MegItem>> {
    private var curPage = 1;
    private var pageSize = 20;
    private var isLoading = false;
    private var isLastPage = false;
    private val job = SupervisorJob()
    private val scope = CoroutineScope(job + Dispatchers.IO)


    fun onStart(){
        megListRepository.addObserver(this)
        isLoading = true;
        isLastPage = false;
        curPage = 1;
        scope.launch {
            megListRepository.fetchMeg(curPage, pageSize)
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
        megListRepository.removeObserver(this)
    }


    override fun update(data: List<MegItem>,eventType: EventType) {
        when(eventType){
            EventType.LOAD_OR_GET_MESSAGE -> {
                isLoading = false
                view.getMegListOrLoadMore(data)
            }
            EventType.LOAD_IS_EMPTY -> {
                isLastPage = true;
                isLoading = false;
                view.loadEmpty()
            }
            else -> {

            }
        }
    }

}