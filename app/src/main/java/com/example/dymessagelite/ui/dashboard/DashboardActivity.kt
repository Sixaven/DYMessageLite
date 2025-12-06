package com.example.dymessagelite.ui.dashboard

import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.core.view.setPadding
import com.example.dymessagelite.R
import com.example.dymessagelite.data.datasource.database.ChatDatabase
import com.example.dymessagelite.data.model.dashboard.DashboardData
import com.example.dymessagelite.data.repository.DashboardRepository
import com.example.dymessagelite.databinding.ActivityDashboardBinding
import java.lang.Exception

interface DashboardView {
    fun displayDashboardData(data: DashboardData)
}


class DashboardActivity : AppCompatActivity(), DashboardView{
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var dashboardControl: DashboardControl
    private lateinit var senderId: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        recevieIntent()
        initControl()

        dashboardControl.getDashboardData()

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun initControl(){
        val database = ChatDatabase.getDatabase(this)
        val chatDao = database.chatDao()
        val dashboardRepository = DashboardRepository(chatDao)
        dashboardControl = DashboardControl(dashboardRepository, senderId,this)
        dashboardRepository.addObserver(dashboardControl)
    }
    fun recevieIntent() {
        val nickname = intent.getStringExtra("nickname");
        senderId = nickname.toString()
        binding.tvNickname.text = nickname;
        val headImageStr = intent.getStringExtra("headImage");
        try {
            val imageId = resources.getIdentifier(
                headImageStr,
                "drawable",
                packageName
            )
            if (imageId != 0) {
                binding.ivAvatar.setImageResource(imageId)
            } else {
                binding.ivAvatar.setImageResource(R.drawable.myhead)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            binding.ivAvatar.setImageResource(R.drawable.myhead)
        }
    }
    override fun displayDashboardData(data: DashboardData) {
        binding.tvUnreadCount.text = data.unreadCount.toString()

        // 2. 更新消息CTR
        binding.tvCtrRate.text = data.ctr

        // 3. 更新各类消息召回率
        binding.tvRecallRateText.text = data.textRecallRate
        binding.tvRecallRateImage.text = data.imageRecallRate
        binding.tvRecallRateAction.text = data.actionRecallRate
    }

}