package com.example.dymessagelite.ui.main

import com.example.dymessagelite.common.observer.EventType
import com.example.dymessagelite.common.observer.Observer
import com.example.dymessagelite.common.toMegItem
import com.example.dymessagelite.data.model.ChatEntity
import com.example.dymessagelite.data.model.MegEntity
import com.example.dymessagelite.data.repository.MegDispatcherRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MegDispatcherControl(
    private val megDispatcherRepository: MegDispatcherRepository
) {
    private val job = SupervisorJob()
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + job)

    fun startSending(){
        scope.launch {
            megDispatcherRepository.onStart()
        }
    }

    fun stopSending(){
        job.cancel()
    }
}