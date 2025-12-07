package com.example.dymessagelite.ui.main

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.activity.enableEdgeToEdge

import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener

import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dymessagelite.common.toDisplayListItems
import com.example.dymessagelite.common.tracker.AppStateTracker
import com.example.dymessagelite.common.util.dpToPx
import com.example.dymessagelite.data.datasource.database.ChatDatabase
import com.example.dymessagelite.data.model.list.DisplayListItem
import com.example.dymessagelite.data.model.list.DisplayType
import com.example.dymessagelite.data.model.list.MegItem
import com.example.dymessagelite.data.repository.ChatRepository
import com.example.dymessagelite.data.repository.DashboardRepository
import com.example.dymessagelite.data.repository.MegDispatcherRepository
import com.example.dymessagelite.data.repository.MegListRepository
import com.example.dymessagelite.data.repository.SearchRepository
import com.example.dymessagelite.databinding.ActivityMainBinding
import com.example.dymessagelite.ui.dashboard.DashboardActivity
import com.example.dymessagelite.ui.detail.MegDetailControl
import com.example.dymessagelite.ui.detail.MessageDetailActivity
import com.example.dymessagelite.ui.main.adapter.MegListAdapter
import com.example.dymessagelite.ui.main.adapter.OnClickListAdapterListener
import com.hjq.toast.Toaster

interface MessageListView {
    fun firstGetMegList(data: List<MegItem>)
    fun loadMoreMegList(data: List<MegItem>)
    fun loadEmpty()
    fun receiveMegChangeByOther(data: List<MegItem>)
    fun jumpDetail(data: List<MegItem>)
    fun receiveMegChangeByMine(data: List<MegItem>)
    fun displaySearchResult(resList: List<MegItem>,keyword: String)
    fun backFromSearch(data: List<MegItem>)
    fun updateNickName(data: List<MegItem>)
}

class MainActivity : AppCompatActivity(), MessageListView, OnClickListAdapterListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var megAdapter: MegListAdapter
    private lateinit var megControl: MegListControl
    private lateinit var megDispatcherRepository: MegDispatcherRepository
    private lateinit var megDispatcherControl: MegDispatcherControl

    private lateinit var chatRepository: ChatRepository
    private lateinit var dashboardRepository: DashboardRepository



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

        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        initControl()
        initAdapter()
        initRecyclerView()
        setupRefreshAndLoadMore()
        setSearchBarPadding()
        setSwitchListener()
        setSearchBarListener()



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
        val searchRepository = SearchRepository(
            megDao,
            chatDao
        )
        dashboardRepository = DashboardRepository.getInstance(chatDao,megDao)
        chatRepository = ChatRepository.getInstance(chatDao, megDao)

        megDispatcherRepository = MegDispatcherRepository.getInstance(megDao, chatDao, this)

        megControl = MegListControl(
            megListRepository,
            megDispatcherRepository,
            chatRepository,
            searchRepository,
            dashboardRepository,
            this
        )
        megDispatcherControl = MegDispatcherControl(
            megDispatcherRepository
        )
    }

    private fun setupRefreshAndLoadMore() {
        binding.smartRefreshLayout.setOnRefreshListener { refreshLayout ->
            megControl.firstGet()
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
        megAdapter = MegListAdapter(this)
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

    override fun onAvatarClick(item: DisplayListItem) {
        binding.searchEditText.text.clear()
        val intent = Intent(this@MainActivity, DashboardActivity::class.java)
        intent.putExtra("nickname", item.name)
        intent.putExtra("headImage", item.avatar)
        if(item.remark != null){
         intent.putExtra("remark",item.remark)
        }
        startActivity(intent)
    }

    override fun onButtonActionClick(item: DisplayListItem) {
        binding.searchEditText.text.clear()
        val intent = Intent(this@MainActivity, DashboardActivity::class.java)
        intent.putExtra("nickname", item.name)
        intent.putExtra("headImage", item.avatar)
        if(item.remark != null){
            intent.putExtra("remark",item.remark)
        }
        startActivity(intent)
    }

    override fun onItemClick(item: DisplayListItem) {
        binding.searchEditText.text.clear()
        val intent = Intent(this@MainActivity, MessageDetailActivity::class.java)
        megControl.jumpDetail(item.name)
        intent.putExtra("nickname", item.name)
        intent.putExtra("headImage", item.avatar)
        if(item.remark != null){
            intent.putExtra("remark",item.remark)
        }
        startActivity(intent)
    }

    //View方法重写
    override fun firstGetMegList(data: List<MegItem>) {
        if(data.isEmpty()){
            showEmptyState("暂无消息")
            binding.searchEditText.visibility = View.GONE
        }else{
            hideEmptyState()
            val displayList = data.toDisplayListItems(DisplayType.DEFAULT)
            megAdapter.submitList(displayList.toList())
        }
        binding.smartRefreshLayout.finishRefresh()
    }
    override fun loadMoreMegList(data: List<MegItem>) {
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

    override fun displaySearchResult(resList: List<MegItem>, keyword: String) {
        val displayList = resList.toDisplayListItems(DisplayType.SEARCH)
        if(displayList.isEmpty()){
            showEmptyState("找不到相关内容")
        }else{
            hideEmptyState()
            megAdapter.setHighlightKeyword(keyword)
            megAdapter.submitList(displayList.toList())
        }
    }

    override fun updateNickName(data: List<MegItem>) {
        val displayList = data.toDisplayListItems(DisplayType.DEFAULT)
        megAdapter.submitList(displayList.toList())
    }

    override fun backFromSearch(data: List<MegItem>) {
        hideEmptyState()
        val displayList = data.toDisplayListItems(DisplayType.DEFAULT)
        megAdapter.submitList(displayList.toList())
    }


    private fun showEmptyState(message: String) {
        // 现在是控制 RecyclerView 和 empty_state_layout 的可见性
        // 而它们的父容器 SmartRefreshLayout 保持可见
        binding.recyclerViewMessages.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.VISIBLE
        binding.emptyText.text = message
    }
    private fun hideEmptyState() {
        binding.recyclerViewMessages.visibility = View.VISIBLE
        binding.emptyStateLayout.visibility = View.GONE
    }

}
