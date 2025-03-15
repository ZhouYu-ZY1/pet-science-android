package com.zhouyu.pet_science.fragments.shop

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.application.Application
import com.zhouyu.pet_science.fragments.BaseFragment


class ShopFragment : BaseFragment() {
    private var searchBar: LinearLayout? = null
    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager2? = null
    private var categoryTitles: ArrayList<String>? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layoutView = inflater.inflate(R.layout.fragment_shop, container, false)
        initViews()
        initData()
        return layoutView
    }

    private fun initViews() {
        searchBar = findViewById(R.id.search_bar)
        tabLayout = findViewById(R.id.tab_layout)
        viewPager = findViewById(R.id.view_pager)

        // 设置TabLayout的指示器宽度
        tabLayout!!.setSelectedTabIndicator(R.drawable.tab_indicator)
        tabLayout!!.isTabIndicatorFullWidth = false

        // 设置ViewPager2的页面切换监听器，实现文字大小渐变
        viewPager?.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)

                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    // 滑动结束，重置状态
                    // 重置所有Tab的样式
                    for (i in 0 until tabLayout!!.tabCount) {
                        if(i == viewPager?.currentItem){
                            continue
                        }
                        val tab = tabLayout!!.getTabAt(i)
                        if (tab != null && tab.customView != null) {
                            val textView = tab.customView!!.findViewById<TextView>(android.R.id.text1)
                            if (textView != null) {
                                textView.scaleX = 1.0f
                                textView.scaleY = 1.0f
                                textView.setTypeface(null, Typeface.NORMAL)
                            }
                        }
                    }
                }
            }
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)

                // 获取当前Tab和下一个Tab
                val currentTab = tabLayout!!.getTabAt(position)
                val nextTab = tabLayout!!.getTabAt(position + 1)

                if (currentTab != null) {
                    // 确保当前Tab有自定义视图
                    if (currentTab.customView == null) {
                        currentTab.setCustomView(R.layout.custom_tab)
                    }
                    val currentText =
                        currentTab.customView!!.findViewById<TextView>(android.R.id.text1)
                    if (currentText != null) {
                        // 当前Tab文字大小从1.2倍缩小到1.0倍
                        val currentScale = 1.2f - 0.2f * positionOffset
                        currentText.scaleX = currentScale
                        currentText.scaleY = currentScale
                        // 根据滑动进度设置文字粗细
                        currentText.setTypeface(
                            null,
                            if (positionOffset < 0.5f) Typeface.BOLD else Typeface.NORMAL
                        )
                    }
                }
                if (nextTab != null) {
                    // 确保下一个Tab有自定义视图
                    if (nextTab.customView == null) {
                        nextTab.setCustomView(R.layout.custom_tab)
                    }
                    val nextText = nextTab.customView!!.findViewById<TextView>(android.R.id.text1)
                    if (nextText != null) {
                        // 下一个Tab文字大小从1.0倍放大到1.2倍
                        val nextScale = 1.0f + 0.2f * positionOffset
                        nextText.scaleX = nextScale
                        nextText.scaleY = nextScale
                        // 根据滑动进度设置文字粗细
                        nextText.setTypeface(
                            null,
                            if (positionOffset >= 0.5f) Typeface.BOLD else Typeface.NORMAL
                        )
                    }
                }
            }
        })
    }

    @SuppressLint("InflateParams")
    private fun initData() {
        // 初始化分类数据
        categoryTitles = ArrayList()
        categoryTitles!!.add("推荐")
        categoryTitles!!.add("鞋类")
        categoryTitles!!.add("潮服")
        categoryTitles!!.add("女装")
        categoryTitles!!.add("包袋")
        categoryTitles!!.add("美妆")
        categoryTitles!!.add("AA")
        categoryTitles!!.add("BB")
        categoryTitles!!.add("CC")
        categoryTitles!!.add("CC")
        categoryTitles!!.add("CC")
        val fragments: MutableList<Fragment> = ArrayList()
        // 为每个分类创建Tab
        for (title in categoryTitles!!) {
            tabLayout!!.addTab(tabLayout!!.newTab().setText(title))
            fragments.add(ShopCategoryFragment.newInstance(title))
        }
        viewPager!!.setOffscreenPageLimit(categoryTitles!!.size)
        // 设置ViewPager2的适配器
        viewPager!!.setAdapter(object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int): Fragment {
                return fragments[position]
            }

            override fun getItemCount(): Int {
                return fragments.size
            }
        })

        // 关联TabLayout和ViewPager2
        TabLayoutMediator(
            tabLayout!!,
            viewPager!!
        ) { tab: TabLayout.Tab, position: Int ->
            tab.setText(categoryTitles!![position])
            val customView: View =
                LayoutInflater.from(Application.context).inflate(R.layout.custom_tab, null)
            val textView = customView.findViewById<TextView>(android.R.id.text1)
            textView.text = categoryTitles!![position]
            textView.setTypeface(null, Typeface.NORMAL) // 初始化为非粗体
            tab.setCustomView(customView)
        }.attach()
    }
}