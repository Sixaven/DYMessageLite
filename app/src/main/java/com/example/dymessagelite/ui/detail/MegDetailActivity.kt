package com.example.dymessagelite.ui.detail

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dymessagelite.R
import com.example.dymessagelite.common.tracker.AppStateTracker
import com.example.dymessagelite.data.datasource.database.ChatDatabase
import com.example.dymessagelite.data.model.detail.MegDetailCell
import com.example.dymessagelite.data.repository.ChatRepository
import com.example.dymessagelite.data.repository.DashboardRepository
import com.example.dymessagelite.data.repository.MegDispatcherRepository
import com.example.dymessagelite.databinding.ActivityMessageDetailBinding
import com.example.dymessagelite.ui.dashboard.DashboardActivity
import com.example.dymessagelite.ui.detail.adapter.MegDetailAdapter
import com.example.dymessagelite.ui.detail.adapter.OnClickDetailAdapterListener
import com.google.android.material.snackbar.Snackbar
import com.hjq.toast.Toaster
import java.lang.Exception


interface MessageDetailView {
    fun displayChatList(chatList: List<MegDetailCell>)

    //List中只有一个MegDetailCell
    fun displaySendMeg(chat: MegDetailCell)

    fun updateNickName(remark: String)
}

class MessageDetailActivity :
    AppCompatActivity(), View.OnClickListener, MessageDetailView, OnClickDetailAdapterListener {
    private lateinit var binding: ActivityMessageDetailBinding
    private lateinit var megDispatcherRepository: MegDispatcherRepository
    private lateinit var megDetailAdapter: MegDetailAdapter
    private lateinit var megDetailControl: MegDetailControl

    private lateinit var senderId: String
    private lateinit var avatarId: String
    private var remark: String? = null



    override fun onResume() {
        super.onResume()
        AppStateTracker.onActivityResumed(
            AppStateTracker.CurrentActivity.MESSAGE_DETAIL,
            senderId
        )
    }

    override fun onPause() {
        super.onPause()
        AppStateTracker.onActivityPaused()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Toaster.init(applicationContext as Application?);
        enableEdgeToEdge()
        binding = ActivityMessageDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        recevieIntent()
        initControl()
        initToolBar()
        initRecycleView()
        setInputAndButtonListener()

        megDetailControl.onStart()

    }

    override fun onDestroy() {
        super.onDestroy()
        megDetailControl.onStop()
    }

    override fun onClick(v: View?) {
        v?.apply {
            when (v.id) {
                R.id.btn_send -> {
                    sendMeg()
                }

                R.id.chat_bar -> {
                    jumpToDash()
                }
            }
        }
    }

    fun sendMeg() {
        val content = binding.etInputMessage.text.toString()
        if (content.isNotEmpty()) {
            megDetailControl.sendMessage(content)
            binding.etInputMessage.text.clear()
        }
    }

    fun initRecycleView() {
        megDetailAdapter = MegDetailAdapter(
            R.drawable.myhead,
            R.drawable.myhead,
            megDetailControl,
            this
        )
        megDetailAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                scrollToBottom()
            }
        })
        binding.recyclerViewChat.apply {
            layoutManager = LinearLayoutManager(this@MessageDetailActivity)
            adapter = megDetailAdapter
        }
    }


    fun initControl() {
        val database = ChatDatabase.getDatabase(this)
        val chatDao = database.chatDao()
        val megDao = database.megDao()
        val chatRepository = ChatRepository.getInstance(chatDao, megDao)
        val dashboardRepository = DashboardRepository.getInstance(chatDao,megDao)
        megDispatcherRepository = MegDispatcherRepository.getInstance(megDao, chatDao, this)
        megDetailControl = MegDetailControl(
            senderId,
            chatRepository,
            megDispatcherRepository,
            dashboardRepository,
            this
        );
    }

    fun initToolBar() {
        binding.toolbarDetail.setNavigationOnClickListener {
            finish()
        }
        binding.chatBar.setOnClickListener(this)
    }

    fun setInputAndButtonListener() {
        binding.etInputMessage.addOnLayoutChangeListener { view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            binding.recyclerViewChat.postDelayed({
                scrollToBottom()
            }, 200)
        }
        binding.btnSend.setOnClickListener(this)
    }

    fun recevieIntent() {
        val nickname = intent.getStringExtra("nickname");
        val headImageStr = intent.getStringExtra("headImage");
        remark = intent.getStringExtra("remark")
        if(remark != null){
            binding.tvDetailNickname.text = remark
        }else{
            binding.tvDetailNickname.text = nickname;
        }

        senderId = nickname.toString()
        avatarId = headImageStr.toString()
        try {
            val imageId = resources.getIdentifier(
                headImageStr,
                "drawable",
                packageName
            )
            if (imageId != 0) {
                binding.ivDetailAvatar.setImageResource(imageId)
            } else {
                binding.ivDetailAvatar.setImageResource(R.drawable.myhead)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            binding.ivDetailAvatar.setImageResource(R.drawable.myhead)
        }
    }

    override fun onItemClick(item: MegDetailCell) {
        if (!item.isClick) {
            megDetailControl.markAsClick(item.id)
        }
        Toaster.show("已点击")
    }

    override fun onActionClick(item: MegDetailCell) {
        if (!item.isClick) {
            megDetailControl.markAsClick(item.id)
        }
        jumpToDash()
    }

    override fun displayChatList(chatList: List<MegDetailCell>) {
        megDetailAdapter.submitList(chatList)
    }

    override fun displaySendMeg(chat: MegDetailCell) {
        val curList = megDetailAdapter.currentList.toMutableList()
        curList.add(chat)
        megDetailAdapter.submitList(curList)
    }

    override fun updateNickName(remark: String) {
        binding.tvDetailNickname.text = remark
    }

    private fun jumpToDash() {
        val intent = Intent(this@MessageDetailActivity, DashboardActivity::class.java)
        intent.putExtra("nickname", senderId)
        intent.putExtra("headImage", avatarId)
        intent.putExtra("remark", remark )
        startActivity(intent)
    }

    fun scrollToBottom() {
        val itemCount = megDetailAdapter.itemCount
        if (itemCount > 0) {
            binding.recyclerViewChat.smoothScrollToPosition(itemCount - 1)
        }
    }

}

