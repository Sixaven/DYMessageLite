package com.example.dymessagelite.ui.main

import android.os.Bundle
import android.view.ViewGroup

import androidx.appcompat.app.AppCompatActivity

import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dymessagelite.data.datasource.MegLocalDataSource

import com.example.dymessagelite.data.model.MegItem
import com.example.dymessagelite.data.repository.MegRepository
import com.example.dymessagelite.databinding.ActivityMainBinding
import com.example.dymessagelite.ui.main.adapter.MegListAdapter

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var megAdapter: MegListAdapter
    private lateinit var megRepository: MegRepository
    private var statusBarHeight = 0

    private var curPage = 1;
    private var pageSize = 20;
    private var isLoading = false;
    private var isLastPage = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initLateVal(dataSource = MegLocalDataSource(this))

        setContentView(binding.root)
        //增加搜索栏布局的外边距
        statusBarHeight = getStatusBarHeight();
        setLinearLayoutMarginTop(statusBarHeight)

        initRecyclerView()
        initMegList()
        setupRefreshAndLoadMore()
    }

    private fun initLateVal(dataSource: MegLocalDataSource) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        megAdapter = MegListAdapter()
        megRepository = MegRepository(dataSource)
    }

    private fun setupRefreshAndLoadMore(){
        binding.smartRefreshLayout.setOnRefreshListener { refreshLayout ->
            binding.root.postDelayed({
                refreshLayout.finishRefresh()
            }, 1200)
        }

        binding.smartRefreshLayout.setOnLoadMoreListener { refreshLayout ->
            loadMoreMeg()
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

        megRepository.fetchMeg(curPage, pageSize) { resMeg ->
            megAdapter.submitList(resMeg)
            isLoading = false;
        }
    }

    private fun loadMoreMeg() {
        if (isLoading) return

        curPage++;
        isLoading = true;

        megRepository.fetchMeg(curPage, pageSize) { resMeg ->
            if (resMeg.isEmpty()) {
                isLastPage = true;
                isLoading = false;
                binding.smartRefreshLayout.finishLoadMoreWithNoMoreData()
            } else {
                val curMegList = megAdapter.currentList.toMutableList()
                curMegList.addAll(resMeg)
                megAdapter.submitList(curMegList)
                isLoading = false;
                binding.smartRefreshLayout.finishLoadMore()
            }

        }
    }

    private fun getStatusBarHeight(): Int {
        var result = 0
        // 获取系统资源的标识符，"status_bar_height" 是系统定义的名字
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            // 如果找到了这个资源，就获取它的尺寸值（像素）
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    private fun setLinearLayoutMarginTop(topMargin: Int) {
        // a. 获取 Toolbar 的现有布局参数 (LayoutParams)
        // 因为 Toolbar 在 CollapsingToolbarLayout 中，所以它的 LayoutParams 类型是 CollapsingToolbarLayout.LayoutParams
        val params = binding.searchLinearLayout.layoutParams as ViewGroup.MarginLayoutParams

        // b. 修改布局参数的上边距
        params.topMargin += topMargin

        // c. 将修改后的布局参数重新设置给 Toolbar
        binding.searchLinearLayout.layoutParams = params
    }
}