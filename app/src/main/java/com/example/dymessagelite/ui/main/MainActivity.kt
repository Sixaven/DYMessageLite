package com.example.dymessagelite.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.ViewGroup

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat

import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dymessagelite.common.observer.Observer
import com.example.dymessagelite.common.util.dpToPx
import com.example.dymessagelite.data.datasource.MegLocalDataSource

import com.example.dymessagelite.data.model.MegItem
import com.example.dymessagelite.data.repository.MegRepository
import com.example.dymessagelite.databinding.ActivityMainBinding
import com.example.dymessagelite.ui.detail.MessageDetailActivity
import com.example.dymessagelite.ui.main.adapter.MegListAdapter
import com.scwang.smart.refresh.layout.api.RefreshLayout

class MainActivity : AppCompatActivity() , Observer<List<MegItem>>{
    private lateinit var binding: ActivityMainBinding
    private lateinit var megAdapter: MegListAdapter
    private lateinit var megRepository: MegRepository
    private var curPage = 1;
    private var pageSize = 20;
    private var isLoading = false;
    private var isLastPage = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initLateVal(dataSource = MegLocalDataSource(this))

        setContentView(binding.root)

        initRecyclerView()
        setupRefreshAndLoadMore()
        setSearchBarPadding()

        registerObserver()

        initMegList()
    }

    private fun initLateVal(dataSource: MegLocalDataSource) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        megAdapter = MegListAdapter{ item ->
            val intent = Intent(this, MessageDetailActivity::class.java)
            intent.putExtra("nickname",item.name)
            intent.putExtra("headImage",item.headId)
            startActivity(intent)
        }
        megRepository = MegRepository(dataSource)
    }

    private fun registerObserver() {
        megRepository.addObserver(this)
    }

    private fun setupRefreshAndLoadMore(){
        binding.smartRefreshLayout.setOnRefreshListener { refreshLayout ->
            binding.root.postDelayed({
                refreshLayout.finishRefresh()
            }, 1200)
        }

        binding.smartRefreshLayout.setOnLoadMoreListener { refreshLayout ->
            loadMoreMeg(refreshLayout)
        }
    }
    private fun initRecyclerView() {

        binding.recyclerViewMessages.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = megAdapter;
        }
    }
    private fun initMegList() {
        isLoading = true;
        isLastPage = false;
        curPage = 1;

        megRepository.fetchMeg(curPage, pageSize)
    }

    private fun loadMoreMeg(refreshLayout: RefreshLayout) {
        if (isLoading) return

        curPage++;
        isLoading = true;

        megRepository.fetchMeg(curPage, pageSize)
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
        binding.searchLinearLayout.setPadding(leftPadding,actionBarHeight+16.dpToPx(),rightPadding,16.dpToPx())
    }

    override fun update(data: List<MegItem>) {
        if (data.isEmpty()) {
            isLastPage = true;
            isLoading = false;
            binding.smartRefreshLayout.finishLoadMoreWithNoMoreData()
        } else {
            val curMegList = megAdapter.currentList.toMutableList()
            curMegList.addAll(data)
            megAdapter.submitList(curMegList)
            isLoading = false;
            binding.smartRefreshLayout.finishLoadMore()
        }
    }
}