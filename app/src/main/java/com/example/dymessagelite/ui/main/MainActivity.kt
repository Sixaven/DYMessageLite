package com.example.dymessagelite.ui.main

import android.os.Bundle
import android.view.ViewGroup

import androidx.appcompat.app.AppCompatActivity

import androidx.recyclerview.widget.LinearLayoutManager

import com.example.dymessagelite.data.model.MegItem
import com.example.dymessagelite.databinding.ActivityMainBinding
import com.example.dymessagelite.ui.main.adapter.MegListAdapter

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var megAdapter: MegListAdapter
    private var statusBarHeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //增加搜索栏布局的外边距
        statusBarHeight = getStatusBarHeight();
        setLinearLayoutMarginTop(statusBarHeight)

        initRecyclerView()

        loadAndSubmitData()

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.root.postDelayed({
                binding.swipeRefreshLayout.isRefreshing = false
            },2000)
        }

    }
    private fun initRecyclerView(){
        megAdapter = MegListAdapter()

        binding.recyclerViewMessages.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)

            adapter = megAdapter;
        }
    }
    private fun loadAndSubmitData() {
        // 创建一个 MegItem 对象的列表作为模拟数据
        // 每个对象都包含所有必要的字段，以匹配新的数据类和UI
        val dummyMessages = listOf(
            MegItem(id = "1", headId = "avatar_1", name = "K.", summary = "晚上一起吃饭吗？", time = "18:30", unreadCount = 2),
            MegItem(id = "2", headId = "avatar_2", name = "Android 大师", summary = "这个Bug我解决了，你更新一下代码。", time = "17:45", unreadCount = 1),
            MegItem(id = "3", headId = "avatar_3", name = "产品经理", summary = "[文件] 最新的需求文档.pdf", time = "15:10", unreadCount = 0),
            MegItem(id = "4", headId = "avatar_4", name = "UI 设计师", summary = "[图片]", time = "14:22", unreadCount = 1),
            MegItem(id = "5", headId = "avatar_5", name = "测试工程师", summary = "你这个页面在低版本系统上崩了！", time = "11:05", unreadCount = 99),
            MegItem(id = "6", headId = "avatar_6", name = "另一个好友", summary = "哈哈哈哈哈哈哈哈", time = "昨天", unreadCount = 0),
            MegItem(id = "7", headId = "avatar_7", name = "摸鱼的同事", summary = "收到", time = "昨天", unreadCount = 0),
            MegItem(id = "8", headId = "avatar_8", name = "楼下小卖部老板", summary = "可乐还有，要几瓶？", time = "星期一", unreadCount = 0),
            MegItem(id = "9", headId = "avatar_9", name = "住在隔壁的王叔叔", summary = "好的，没问题。", time = "星期一", unreadCount = 0),
            MegItem(id = "10", headId = "avatar_10", name = "喜欢开源的大佬", summary = "看下我新开源的项目，给个Star呗！", time = "11/15", unreadCount = 120),
            MegItem(id = "11", headId = "avatar_11", name = "热爱学习的你", summary = "今天也要加油学习安卓！", time = "11/14", unreadCount = 0)
            // 你可以按此格式继续添加更多数据...
        )

        // 使用 submitList 方法提交新的数据列表
        megAdapter.submitList(dummyMessages)
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