package com.example.dymessagelite.ui.main

import com.example.dymessagelite.common.observer.EventType
import com.example.dymessagelite.common.observer.Observer
import com.example.dymessagelite.data.model.MegItem
import com.example.dymessagelite.data.repository.MegListRepository

class MegListControl(
    private val megListRepository: MegListRepository,
    private val view: MessageListView
): Observer<List<MegItem>> {
    private var curPage = 1;
    private var pageSize = 20;
    private var isLoading = false;
    private var isLastPage = false;

    fun onStart(){
        megListRepository.addObserver(this)
        isLoading = true;
        isLastPage = false;
        curPage = 1;
        megListRepository.fetchMeg(curPage, pageSize)
    }
    fun loadMore(){
        if (isLoading || isLastPage) return
        curPage++;
        isLoading = true;
        megListRepository.fetchMeg(curPage, pageSize)
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