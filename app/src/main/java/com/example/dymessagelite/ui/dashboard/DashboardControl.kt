package com.example.dymessagelite.ui.dashboard

import com.example.dymessagelite.common.observer.EventType
import com.example.dymessagelite.common.observer.Observer
import com.example.dymessagelite.data.model.dashboard.DashboardData
import com.example.dymessagelite.data.model.dashboard.DashboardEvent
import com.example.dymessagelite.data.model.detail.ChatEvent
import com.example.dymessagelite.data.repository.ChatRepository

import com.example.dymessagelite.data.repository.DashboardRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DashboardControl(
    private val dashboardRepository: DashboardRepository,
    private val senderId: String,
    private val view: DashboardView
) {



    private val dashObserver = object : Observer<DashboardEvent> {
        override fun update(data: DashboardEvent, eventType: EventType) {
            when (eventType) {
                EventType.GET_DASHBOARD_DATA -> {
                    data.dashboardData?.apply {
                        view.displayDashboardData(data.dashboardData)
                    }
                }
                EventType.UPDATE_NICKNAME -> {
                    data.remark?.apply {
                        view.updateNickName(data.remark)
                    }
                }

                else -> {}
            }
        }
    }

    fun saveRemark(remark: String, senderId: String) {
        dashboardRepository.saveRemark(remark, senderId)
    }

    fun onStart() {
        dashboardRepository.addObserver(this.dashObserver)
        getDashboardData()
    }

    fun onStop() {
        dashboardRepository.removeObserver(dashObserver)
    }

    fun getDashboardData() {
        dashboardRepository.getDashboardData(senderId)
    }

}