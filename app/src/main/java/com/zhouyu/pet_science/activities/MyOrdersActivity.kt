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
import com.zhouyu.pet_science.fragments.OrderListFragment

class MyOrdersActivity : BaseActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    // 订单状态列表
    private val orderStatusList = arrayOf("全部", "待付款", "待发货", "待收货", "已完成")
    private val orderStatusValues = arrayOf("", "pending", "paid", "shipped", "completed")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_orders)

        setTopBarView(findViewById(R.id.toolbar),true)

        // 初始化视图
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)

        viewPager.offscreenPageLimit = orderStatusList.size // 设置ViewPager的预加载页面数
        // 返回按钮点击事件
        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish()
        }
        
        // 搜索按钮点击事件
        findViewById<View>(R.id.btnSearch).setOnClickListener {
            // TODO: 实现订单搜索功能
        }

        // 初始化ViewPager和TabLayout
        setupViewPager()
        
        // 处理传入的状态参数，选择相应的tab
        val initialStatus = intent.getStringExtra("status")
        if (initialStatus != null) {
            val index = orderStatusValues.indexOf(initialStatus)
            if (index != -1) {
                viewPager.currentItem = index
            }
        }
    }

    private fun setupViewPager() {
        // 设置ViewPager适配器
        val pagerAdapter = OrderPagerAdapter(this)
        viewPager.adapter = pagerAdapter

        // 将TabLayout与ViewPager关联
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
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