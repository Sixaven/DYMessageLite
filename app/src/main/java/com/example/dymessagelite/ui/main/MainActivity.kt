package com.example.dymessagelite.ui.main

import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity

import androidx.recyclerview.widget.LinearLayoutManager

import com.example.dymessagelite.data.model.MegItem
import com.example.dymessagelite.databinding.ActivityMainBinding
import com.example.dymessagelite.ui.main.adapter.MegListAdapter

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var megAdapter: MegListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        // 每个对象都包含 id, headId, 和 name 三个属性
        val dummyMessages = listOf(
            MegItem(id = "1", headId = "avatar_1", name = "K."),
            MegItem(id = "2", headId = "avatar_2", name = "Android 大师"),
            MegItem(id = "3", headId = "avatar_3", name = "产品经理"),
            MegItem(id = "4", headId = "avatar_4", name = "UI 设计师"),
            MegItem(id = "5", headId = "avatar_5", name = "测试工程师"),
            MegItem(id = "6", headId = "avatar_6", name = "另一个好友"),
            MegItem(id = "7", headId = "avatar_7", name = "摸鱼的同事"),
            MegItem(id = "8", headId = "avatar_8", name = "楼下小卖部老板"),
            MegItem(id = "9", headId = "avatar_9", name = "住在隔壁的王叔叔"),
            MegItem(id = "10", headId = "avatar_10", name = "喜欢开源的大佬"),
            MegItem(id = "11", headId = "avatar_11", name = "热爱学习的你")
        )

        // 【关键】使用 submitList 方法提交新的数据列表。
        // ListAdapter 会在后台自动计算差异并以动画形式更新列表，非常高效！
        megAdapter.submitList(dummyMessages)
    }
}