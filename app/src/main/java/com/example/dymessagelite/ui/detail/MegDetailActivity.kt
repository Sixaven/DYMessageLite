package com.example.dymessagelite.ui.detail

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dymessagelite.R
import com.example.dymessagelite.data.datasource.database.ChatDatabase
import com.example.dymessagelite.data.model.MegDetailCell
import com.example.dymessagelite.data.repository.ChatRepository
import com.example.dymessagelite.databinding.ActivityMessageDetailBinding
import com.example.dymessagelite.ui.detail.adapter.MegDetailAdapter
import java.lang.Exception


interface MessageDetailView {
    fun displayChatList(chatList: List<MegDetailCell>)

    //List中只有一个MegDetailCell
    fun displaySendMeg(chat: List<MegDetailCell>)
}

class MessageDetailActivity : AppCompatActivity(), View.OnClickListener, MessageDetailView {
    private lateinit var binding: ActivityMessageDetailBinding
    private lateinit var chatRepository: ChatRepository
    private lateinit var megDetailAdapter: MegDetailAdapter
    private lateinit var megDetailControl: MegDetailControl

    private lateinit var senderId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recevieIntent()
        initToolBar()
        initRecycleView()

        initRepository()
        initControl()
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
        megDetailAdapter = MegDetailAdapter(R.drawable.myhead, R.drawable.myhead)
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

    fun initRepository() {
        val chatDatabase = ChatDatabase.getDatabase(this)
        val chatDao = chatDatabase.chatDao()
        chatRepository = ChatRepository(chatDao)
    }

    fun initControl() {
        megDetailControl = MegDetailControl(senderId, chatRepository, this);
    }

    fun initToolBar() {
        binding.toolbarDetail.setNavigationOnClickListener {
            finish()
        }
    }

    fun setInputAndButtonListener() {
//        binding.etInputMessage.setOnFocusChangeListener { view, hasFocus ->
//            if (hasFocus) {
//                view.postDelayed({
//                    scrollToBottom()
//                }, 200)
//            }
//        }
        binding.etInputMessage.addOnLayoutChangeListener { view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            view.postDelayed({
                scrollToBottom()
            }, 200)
        }
        binding.btnSend.setOnClickListener(this)
    }

    fun recevieIntent() {
        val nickname = intent.getStringExtra("nickname");
        senderId = nickname.toString()
        binding.tvDetailNickname.text = nickname;
        val headImageStr = intent.getStringExtra("headImage");
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

    override fun displayChatList(chatList: List<MegDetailCell>) {
        megDetailAdapter.submitList(chatList)
    }

    override fun displaySendMeg(chat: List<MegDetailCell>) {
        val curList = megDetailAdapter.currentList.toMutableList()
        curList.addAll(chat)
        megDetailAdapter.submitList(curList)
    }

    fun scrollToBottom() {
        val itemCount = megDetailAdapter.itemCount
        if (itemCount > 0) {
            binding.recyclerViewChat.smoothScrollToPosition(itemCount - 1)
        }
    }
}

