package com.example.dymessagelite.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue

import androidx.appcompat.app.AppCompatActivity

import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dymessagelite.common.util.dpToPx
import com.example.dymessagelite.data.datasource.database.ChatDatabase
import com.example.dymessagelite.data.model.MegItem
import com.example.dymessagelite.data.repository.MegListRepository
import com.example.dymessagelite.databinding.ActivityMainBinding
import com.example.dymessagelite.ui.detail.MessageDetailActivity
import com.example.dymessagelite.ui.main.adapter.MegListAdapter

interface MessageListView{
    fun getMegListOrLoadMore(data: List<MegItem>)
    fun loadEmpty()
}
class MainActivity : AppCompatActivity() , MessageListView{
    private lateinit var binding: ActivityMainBinding
    private lateinit var megAdapter: MegListAdapter
    private lateinit var megControl: MegListControl


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        initAdapter()
        initRecyclerView()
        setupRefreshAndLoadMore()
        setSearchBarPadding()

        initControl()

        megControl.onStart()
    }

    override fun onDestroy() {
        super.onDestroy()
        megControl.onStop()
    }

    private fun initControl() {
        val database = ChatDatabase.getDatabase(this)
        val megDao = database.megDao()
        val megListRepository = MegListRepository(megDao)
        megControl = MegListControl(megListRepository,this)
    }
    private fun setupRefreshAndLoadMore(){
        binding.smartRefreshLayout.setOnRefreshListener { refreshLayout ->
            binding.root.postDelayed({
                refreshLayout.finishRefresh()
            }, 1200)
        }

        binding.smartRefreshLayout.setOnLoadMoreListener { refreshLayout ->
            megControl.loadMore()
        }
    }
    fun initAdapter(){
        megAdapter = MegListAdapter{ item ->
            val intent = Intent(this@MainActivity, MessageDetailActivity::class.java)
            intent.putExtra("nickname",item.name)
            intent.putExtra("headImage",item.headId)
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
            actionBarHeight =  TypedValue.complexToDimensionPixelSize(tv.data, this.resources.displayMetrics)
        }
        val leftPadding = binding.searchLinearLayout.paddingLeft
        val rightPadding = binding.searchLinearLayout.paddingRight

        // c. 将修改后的布局参数重新设置给 Toolbar
        binding.searchLinearLayout.setPadding(leftPadding,actionBarHeight+16.dpToPx(),rightPadding,0)
    }

    override fun getMegListOrLoadMore(data: List<MegItem>) {
        val curMegList = megAdapter.currentList.toMutableList()
        curMegList.addAll(data)
        megAdapter.submitList(curMegList)
        binding.smartRefreshLayout.finishLoadMore()

    }

    override fun loadEmpty() {
        binding.smartRefreshLayout.finishLoadMoreWithNoMoreData()
    }
}
