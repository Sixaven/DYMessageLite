package com.example.dymessagelite.ui.dashboard

import android.graphics.drawable.ColorDrawable
import android.os.Bundle

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Color

import com.example.dymessagelite.R
import com.example.dymessagelite.data.datasource.database.ChatDatabase
import com.example.dymessagelite.data.model.dashboard.DashboardData
import com.example.dymessagelite.data.repository.ChatRepository
import com.example.dymessagelite.data.repository.DashboardRepository
import com.example.dymessagelite.databinding.ActivityDashboardBinding
import com.example.dymessagelite.databinding.DialogSetRemarkBinding
import java.lang.Exception
import androidx.core.graphics.drawable.toDrawable
import com.example.dymessagelite.data.datasource.dao.MegDao

interface DashboardView {
    fun displayDashboardData(data: DashboardData)

    fun updateNickName(remark: String)
}


class DashboardActivity : AppCompatActivity(), DashboardView{
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var dashboardControl: DashboardControl
    private lateinit var senderId: String

    private var remark: String? = null
    private lateinit var megDao: MegDao


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        recevieIntent()

        initControl()

        dashboardControl.onStart()

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.tvNickname.setOnClickListener {
            showSetNicknameDialog()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        dashboardControl.onStop()
    }

    private fun initControl(){
        val database = ChatDatabase.getDatabase(this)
        val chatDao = database.chatDao()
        megDao = database.megDao()
        val dashboardRepository = DashboardRepository.getInstance(chatDao,megDao)
        dashboardControl = DashboardControl(
            dashboardRepository,
            senderId,
            this
        )
    }
    fun recevieIntent() {
        val nickname = intent.getStringExtra("nickname");
        val remark = intent.getStringExtra("remark")
        val headImageStr = intent.getStringExtra("headImage");
        senderId = nickname.toString()
        if(remark != null){
            binding.tvNickname.text = remark;
        }else{
            binding.tvNickname.text = nickname;
        }
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
    private fun showSetNicknameDialog() {
        // 初始化弹窗布局的Binding
        val dialogBinding = DialogSetRemarkBinding.inflate(layoutInflater)

        // 创建Dialog
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root) // 绑定弹窗根布局
            .create()

        // 弹窗样式配置（圆角+宽度）
        dialog.window?.apply {
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            val params = attributes
            params.width = (resources.displayMetrics.widthPixels * 0.85).toInt()
            attributes = params
        }

        // 取消按钮（通过Binding获取控件）
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // 确定按钮（通过Binding获取输入框和按钮）
        dialogBinding.btnConfirm.setOnClickListener {
            val newNickname = dialogBinding.etNickname.text.toString().trim()
            if (newNickname.isNotEmpty()) {
                dashboardControl.saveRemark(newNickname, senderId)
                dialog.dismiss()
            }
        }
        dialog.show()
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

    override fun updateNickName(remark: String) {
        binding.tvNickname.text = remark
    }

}