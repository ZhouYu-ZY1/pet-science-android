package com.zhouyu.pet_science.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.activities.base.BaseActivity
import com.zhouyu.pet_science.databinding.ActivityMyOrdersBinding
import com.zhouyu.pet_science.fragments.OrderListFragment

class MyOrdersActivity : BaseActivity() {

    private lateinit var binding: ActivityMyOrdersBinding

    // 订单状态列表
    private val orderStatusList = arrayOf("全部", "待付款", "待发货", "待收货", "已完成")
    private val orderStatusValues = arrayOf("", "pending", "paid", "shipped", "completed")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setTopBarView(binding.toolbar, true)

        binding.viewPager.offscreenPageLimit = orderStatusList.size // 设置ViewPager的预加载页面数

        // 返回按钮点击事件
        binding.btnBack.setOnClickListener {
            finish()
        }

        // 搜索按钮点击事件
        binding.btnSearch.setOnClickListener {
            // TODO: 实现订单搜索功能
        }

        // 初始化ViewPager和TabLayout
        setupViewPager()
        
        // 处理传入的状态参数，选择相应的tab
        val initialStatus = intent.getStringExtra("status")
        if (initialStatus != null) {
            val index = orderStatusValues.indexOf(initialStatus)
            if (index != -1) {
                binding.viewPager.currentItem = index
            }
        }
    }

    private fun setupViewPager() {
        // 设置ViewPager适配器
        val pagerAdapter = OrderPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        // 将TabLayout与ViewPager关联
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = orderStatusList[position]
        }.attach()
    }

    // ViewPager适配器
    private inner class OrderPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = orderStatusList.size

        override fun createFragment(position: Int): Fragment {
            return OrderListFragment.newInstance(orderStatusValues[position])
        }
    }
} 