package com.example.dymessagelite.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.MotionEvent
import android.widget.EditText

import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener

import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dymessagelite.common.toDisplayListItems
import com.example.dymessagelite.common.tracker.AppStateTracker
import com.example.dymessagelite.common.util.JsonUtils
import com.example.dymessagelite.common.util.dpToPx
import com.example.dymessagelite.data.datasource.dao.MegDao
import com.example.dymessagelite.data.datasource.database.ChatDatabase
import com.example.dymessagelite.data.model.DisplayType
import com.example.dymessagelite.data.model.MegEntity
import com.example.dymessagelite.data.model.MegItem
import com.example.dymessagelite.data.model.MegType
import com.example.dymessagelite.data.repository.ChatRepository
import com.example.dymessagelite.data.repository.MegDispatcherRepository
import com.example.dymessagelite.data.repository.MegListRepository
import com.example.dymessagelite.databinding.ActivityMainBinding
import com.example.dymessagelite.ui.detail.MessageDetailActivity
import com.example.dymessagelite.ui.main.adapter.MegListAdapter
import com.google.gson.reflect.TypeToken

interface MessageListView {
    fun getMegListOrLoadMore(data: List<MegItem>)
    fun loadEmpty()
    fun receiveMegChangeByOther(data: List<MegItem>)
    fun jumpDetail(data: List<MegItem>)
    fun receiveMegChangeByMine(data: List<MegItem>)
    fun displaySearchResult(resList: List<MegItem>)
    fun backFromSearch(data: List<MegItem>)
}

class MainActivity : AppCompatActivity(), MessageListView {
    private lateinit var binding: ActivityMainBinding
    private lateinit var megAdapter: MegListAdapter
    private lateinit var megControl: MegListControl
    private lateinit var megDispatcherRepository: MegDispatcherRepository
    private lateinit var megDispatcherControl: MegDispatcherControl

    override fun onResume() {
        super.onResume()
        AppStateTracker.onActivityResumed(
            AppStateTracker.CurrentActivity.MESSAGE_LIST,
            null
        )
    }

    override fun onPause() {
        super.onPause()
        AppStateTracker.onActivityPaused()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        initAdapter()
        initRecyclerView()
        setupRefreshAndLoadMore()
        setSearchBarPadding()
        setSwitchListener()
        setSearchBarListener()

        initControl()

        megControl.onStart()
    }
    override fun onDestroy() {
        super.onDestroy()
        megControl.onStop()
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if(ev?.action == MotionEvent.ACTION_DOWN){
            val v = currentFocus
            if(v is EditText){
                val outRect = android.graphics.Rect();
                v.getGlobalVisibleRect(outRect);
                if(!outRect.contains(ev.rawX.toInt(),ev.rawY.toInt())){
                    v.clearFocus();
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }
    private fun initControl() {
        val database = ChatDatabase.getDatabase(this)
        val megDao = database.megDao()
        val chatDao = database.chatDao()

        val megListRepository = MegListRepository(megDao)
        val chatRepository = ChatRepository.getInstance(chatDao, megDao)

        megDispatcherRepository = MegDispatcherRepository.getInstance(megDao, chatDao, this)

        megControl = MegListControl(
            megListRepository,
            megDispatcherRepository,
            chatRepository,
            this
        )
        megDispatcherControl = MegDispatcherControl(
            megDispatcherRepository
        )
    }

    private fun setupRefreshAndLoadMore() {
        binding.smartRefreshLayout.setOnRefreshListener { refreshLayout ->
            binding.root.postDelayed({
                refreshLayout.finishRefresh()
            }, 1200)
        }

        binding.smartRefreshLayout.setOnLoadMoreListener { refreshLayout ->
            megControl.loadMore()
        }
    }

    private fun setSwitchListener() {
        binding.toolbarSyncSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                //开关开启
                megDispatcherControl.startSending()
            } else {
                //开关关闭
                megDispatcherControl.stopSending()
            }
        }
    }

    private fun initAdapter() {
        megAdapter = MegListAdapter { item ->
            if(item.contentType == MegType.ACTION) return@MegListAdapter

            val intent = Intent(this@MainActivity, MessageDetailActivity::class.java)
            megControl.jumpDetail(item.name)
            intent.putExtra("nickname", item.name)
            intent.putExtra("headImage", item.avatar)
            startActivity(intent)
        }
    }

    private fun initRecyclerView() {

        binding.recyclerViewMessages.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = megAdapter;
        }
    }

    private fun setSearchBarPadding() {

        val tv = TypedValue()
        var actionBarHeight = 0
        if (this.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight =
                TypedValue.complexToDimensionPixelSize(tv.data, this.resources.displayMetrics)
        }
        val leftPadding = binding.searchLinearLayout.paddingLeft
        val rightPadding = binding.searchLinearLayout.paddingRight

        // c. 将修改后的布局参数重新设置给 Toolbar
        binding.searchLinearLayout.setPadding(
            leftPadding,
            actionBarHeight + 16.dpToPx(),
            rightPadding,
            0
        )
    }

    private fun setSearchBarListener(){
        binding.searchEditText.addTextChangedListener{text ->
            val keyword = text.toString()
            if(keyword.isNotEmpty()){
                val keyword = binding.searchEditText.text.toString();
                megControl.searchMeg(keyword)
            }else{
                megControl.backFromSearch()
            }
        }
    }

    fun scrollToTop() {
        binding.recyclerViewMessages.smoothScrollToPosition(0)
    }

    override fun getMegListOrLoadMore(data: List<MegItem>) {
        val displayList = data.toDisplayListItems(DisplayType.DEFAULT)
        megAdapter.submitList(displayList.toList())
        binding.smartRefreshLayout.finishLoadMore()
    }

    override fun loadEmpty() {
        binding.smartRefreshLayout.finishLoadMoreWithNoMoreData()
    }

    override fun receiveMegChangeByOther(data: List<MegItem>) {
        val displayList = data.toDisplayListItems(DisplayType.DEFAULT)
        megAdapter.submitList(displayList.toList())
        binding.recyclerViewMessages.postDelayed({
            scrollToTop()
        }, 200)
    }

    override fun receiveMegChangeByMine(data: List<MegItem>) {
        val displayList = data.toDisplayListItems(DisplayType.DEFAULT)
        megAdapter.submitList(displayList.toList())
    }

    override fun jumpDetail(data: List<MegItem>) {
        val displayList = data.toDisplayListItems(DisplayType.DEFAULT)
        megAdapter.submitList(displayList.toList())
    }

    override fun displaySearchResult(resList: List<MegItem>) {
        val displayList = resList.toDisplayListItems(DisplayType.SEARCH)
        megAdapter.submitList(displayList.toList())
    }

    override fun backFromSearch(data: List<MegItem>) {
        val displayList = data.toDisplayListItems(DisplayType.DEFAULT)
        megAdapter.submitList(displayList.toList())
    }
}
