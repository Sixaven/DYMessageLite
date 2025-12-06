package com.example.dymessagelite.ui.main

import com.example.dymessagelite.data.repository.MegDispatcherRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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