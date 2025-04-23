package com.zhouyu.pet_science.fragments.shop

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.provider.Telephony.Mms.Intents
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.activities.SearchActivity
import com.zhouyu.pet_science.application.Application
import com.zhouyu.pet_science.databinding.FragmentShopBinding
import com.zhouyu.pet_science.fragments.BaseFragment
import com.zhouyu.pet_science.model.Category
import com.zhouyu.pet_science.network.ProductHttpUtils
import com.zhouyu.pet_science.tools.StorageTool

class ShopFragment : BaseFragment() {
    private var _binding: FragmentShopBinding? = null
    private val binding get() = _binding!!
    companion object {
        private const val VIEW_TYPE_PRODUCT = 100  // 产品视图类型常量
    }
    
    // 创建共享的 RecycledViewPool
    private val sharedPool = RecyclerView.RecycledViewPool().apply {
        // 为每种类型的视图预设缓存大小，提高性能
        setMaxRecycledViews(VIEW_TYPE_PRODUCT, 20)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopBinding.inflate(inflater, container, false)
        initViews()
        initData()
        return binding.root
    }

    private fun initViews() {
        // 设置TabLayout的指示器宽度
        binding.tabLayout.setSelectedTabIndicator(R.drawable.tab_indicator)
        binding.tabLayout.isTabIndicatorFullWidth = false

        binding.searchView.setOnClickListener{
            val intent = Intent(context, SearchActivity::class.java)
            intent.putExtra("type", "product")
            startActivity(intent)
        }
        initTabLayoutAnim()
    }

    @SuppressLint("InflateParams")
    private fun initData() {
        // 先从本地缓存加载分类列表
        val cachedCategoryList = StorageTool.get<List<Category>>("category_list")
        if (cachedCategoryList != null) {
            // 如果有缓存数据，先使用缓存数据初始化UI
            initCateGory(cachedCategoryList)
        }
        
        // 然后再从网络获取最新数据
        executeThread {
            val categoryList = ProductHttpUtils.getCategoryList()
            if (categoryList != null) {
                (categoryList as ArrayList<Category>).add(0, Category(-1, "all","全部")) // 添加"全部"分类
                val needUpdateUI = if (cachedCategoryList != null) {
                    // 比较网络数据和缓存数据是否有差异
                    val isDifferent = categoryList.size != cachedCategoryList.size || 
                            !categoryList.containsAll(cachedCategoryList) ||
                            !cachedCategoryList.containsAll(categoryList)
                    
                    if (isDifferent) {
                        // 如果有差异，更新本地缓存
                        StorageTool.put("category_list", categoryList)
                        true
                    } else {
                        false
                    }
                } else {
                    // 如果缓存为空，保存数据到本地缓存
                    StorageTool.put("category_list", categoryList)
                    true
                }
                
                // 如果需要更新UI，在主线程中更新
                if (needUpdateUI) {
                    runUiThread {
                        initCateGory(categoryList)
                    }
                }
            }
        }
    }

    private fun initCateGory(categoryList: List<Category>?) {
        if (!categoryList.isNullOrEmpty()) {
            val fragments: MutableList<Fragment> = ArrayList()
            // 为每个分类创建Tab
            for (category in categoryList) {
                binding.tabLayout.addTab(binding.tabLayout.newTab().setText(category.categoryName))
                // 传递共享的 RecycledViewPool 给 Fragment
                fragments.add(ShopCategoryFragment.newInstance(category, sharedPool))
            }
            binding.viewPager.offscreenPageLimit = categoryList.size

            // 设置ViewPager2的适配器
            binding.viewPager.adapter = object : FragmentStateAdapter(this) {
                override fun createFragment(position: Int): Fragment {
                    return fragments[position]
                }

                override fun getItemCount(): Int {
                    return fragments.size
                }
            }

            // 关联TabLayout和ViewPager2
            TabLayoutMediator(
                binding.tabLayout,
                binding.viewPager
            ) { tab: TabLayout.Tab, position: Int ->
                val categoryName = categoryList[position].categoryName
                tab.text = categoryName
                val customView: View =
                    LayoutInflater.from(Application.context).inflate(R.layout.item_shop_category, null)
                val textView = customView.findViewById<TextView>(R.id.category_name)
                textView.text = categoryName
                textView.setTypeface(null, Typeface.NORMAL) // 初始化为非粗体
                tab.customView = customView
            }.attach()
        }
    }

    /**
     * 初始化TabLayout的动画效果
     */
    private fun initTabLayoutAnim() {
        // 设置ViewPager2的页面切换监听器，实现文字大小渐变
        binding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)

                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    // 滑动结束，重置状态
                    // 重置所有Tab的样式
                    for (i in 0 until binding.tabLayout.tabCount) {
                        if (i == binding.viewPager.currentItem) {
                            continue
                        }
                        val tab = binding.tabLayout.getTabAt(i)
                        if (tab != null && tab.customView != null) {
                            val textView = tab.customView!!.findViewById<TextView>(R.id.category_name)
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
                val currentTab = binding.tabLayout.getTabAt(position)
                val nextTab = binding.tabLayout.getTabAt(position + 1)

                if (currentTab != null) {
                    // 确保当前Tab有自定义视图
                    if (currentTab.customView == null) {
                        currentTab.setCustomView(R.layout.custom_tab)
                    }
                    val currentText =
                        currentTab.customView!!.findViewById<TextView>(R.id.category_name)
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
                    val nextText = nextTab.customView!!.findViewById<TextView>(R.id.category_name)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}