package com.example.dymessagelite.ui.dashboard

import com.example.dymessagelite.common.observer.EventType
import com.example.dymessagelite.common.observer.Observer
import com.example.dymessagelite.data.model.dashboard.DashboardData

import com.example.dymessagelite.data.repository.DashboardRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DashboardControl(
    private val dashboardRepository: DashboardRepository,
    private val senderId: String,
    private val view: DashboardView
): Observer<DashboardData> {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(job + Dispatchers.IO)

    fun getDashboardData() {
        scope.launch {
            dashboardRepository.getDashboardData(senderId)
        }
    }

    override fun update(data: DashboardData, eventType: EventType) {
        when(eventType){
            EventType.GET_DASHBOARD_DATA -> {
                view.displayDashboardData(data)
            }
            else -> {}
        }
    }
}