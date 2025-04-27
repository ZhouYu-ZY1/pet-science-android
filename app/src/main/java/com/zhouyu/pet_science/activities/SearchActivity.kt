package com.zhouyu.pet_science.activities

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.zhouyu.pet_science.activities.base.BaseActivity
import com.zhouyu.pet_science.databinding.ActivitySearchBinding
import com.zhouyu.pet_science.databinding.ItemHistorySearchBinding
import com.zhouyu.pet_science.fragments.SearchProductFragment
import com.zhouyu.pet_science.fragments.SearchUserFragment
import com.zhouyu.pet_science.utils.StorageUtils
import com.zhouyu.pet_science.views.dialog.MyDialog

class SearchActivity : BaseActivity() {
    private lateinit var binding: ActivitySearchBinding
    private val fragments = ArrayList<Fragment>()
    private val tabTitles = arrayOf("产品","用户")
    private val searchHistoryList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setTopBarView(binding.main, true)
        
        initView()
        setupViewPager()
        setupSearchListener()
        setupHistorySearch()
        
        // 初始状态隐藏搜索结果区域，显示历史搜索
        binding.searchResultContainer.visibility = View.INVISIBLE
        binding.historyContainer.visibility = View.VISIBLE
    }

    private fun initView() {
        binding.searchInput.requestFocus() // 输入框获得焦点
    }
    
    private fun setupViewPager() {

        // 添加两个搜索Fragment
        fragments.add(SearchProductFragment())
        fragments.add(SearchUserFragment())
        
        // 设置ViewPager适配器
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = fragments.size
            
            override fun createFragment(position: Int): Fragment = fragments[position]
        }
        binding.viewPager.offscreenPageLimit = fragments.size

        // 连接TabLayout和ViewPager
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        val searchType = intent.getStringExtra("type") // 默认搜索方式 user或者product

        // 根据传入的搜索类型设置默认选中的Tab
        val defaultTab = if (searchType == "product") 0 else 1
        binding.viewPager.post{
            binding.viewPager.currentItem = defaultTab
        }
        
        // 添加页面切换监听器
        binding.viewPager.registerOnPageChangeCallback(object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // 如果搜索结果界面已显示，则检查当前页面是否需要搜索
                if (binding.searchResultContainer.visibility == View.VISIBLE && binding.searchInput.text.isNotEmpty()) {
                    val keyword = binding.searchInput.text.toString()
                    updateSearchResults(keyword)
                }
            }
        })
    }
    
    private fun setupSearchListener() {
        // 监听输入框回车事件
        binding.searchInput.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || 
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                val keyword = binding.searchInput.text.toString().trim()
                if (keyword.isNotEmpty()) {
                    performSearch(keyword)
                    return@setOnEditorActionListener true
                }
            }
            false
        }
        
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                // 只有在搜索结果界面显示时才更新搜索结果
                if (binding.searchResultContainer.visibility == View.VISIBLE) {
                    val keyword = binding.searchInput.text.toString()
                    updateSearchResults(keyword)
                }
            }
            
            override fun afterTextChanged(editable: Editable) {}
        })
    }
    
    private fun setupHistorySearch() {
        // 从SharedPreferences加载搜索历史
        loadSearchHistory()
        // 加载历史搜索列表
        refreshHistoryList()

        // 清空历史按钮点击事件
        binding.clearHistorySearch.setOnClickListener {
            MyDialog(this).apply {
                 setTitle("清空历史搜索")
                 setMessage("确定要清空历史搜索记录吗？")
                 setYesOnclickListener("确定") {
                     clearSearchHistory()
                     dismiss()
                 }
                 setNoOnclickListener("取消") {
                     dismiss()
                 }
                 show()
            }
        }
    }

    private fun refreshHistoryList() {
        binding.historySearchList.removeAllViews() // 清空历史搜索列表
        for (s in searchHistoryList) {
            val itemHistorySearchBinding = ItemHistorySearchBinding.inflate(LayoutInflater.from(this), binding.historySearchList, false)
            itemHistorySearchBinding.historySearchText.text = s
            itemHistorySearchBinding.root.setOnClickListener {
                binding.searchInput.setText(s)
                performSearch(s)
            }
            binding.historySearchList.addView(itemHistorySearchBinding.root)
        }
    }

    private fun performSearch(keyword: String) {
        // 隐藏键盘
        hideKeyboard()
        
        // 显示搜索结果区域，隐藏历史搜索
        binding.searchResultContainer.visibility = View.VISIBLE
        binding.historyContainer.visibility = View.INVISIBLE

        // 保存搜索关键词到历史
        saveSearchKeyword(keyword)

        // 更新搜索结果
        binding.viewPager.post{
            updateSearchResults(keyword)
        }
    }
    
    // 添加隐藏键盘的方法
    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.searchInput.windowToken, 0)
    }
    
    private fun updateSearchResults(keyword: String) {
        // 通知当前显示的Fragment更新搜索结果
        val currentFragment = fragments[binding.viewPager.currentItem]
        // 检查当前Fragment是否需要搜索该关键词
        if (currentFragment is SearchUserFragment && !currentFragment.hasSearched(keyword)) {
            currentFragment.updateSearchResults(keyword)
        } else if (currentFragment is SearchProductFragment && !currentFragment.hasSearched(keyword)) {
            currentFragment.updateSearchResults(keyword)
        }
    }
    
    private fun loadSearchHistory() {
        searchHistoryList.clear()

        // 加载历史记录
        if(StorageUtils.contains("search_history")) {
            searchHistoryList.addAll(StorageUtils.get("search_history"))
        }
    }
    
    @SuppressLint("NotifyDataSetChanged")
    private fun saveSearchKeyword(keyword: String) {
        if (keyword.isBlank()) return
        
        // 从列表中移除已存在的相同关键词（如果有）
        searchHistoryList.remove(keyword)
        // 添加到列表开头
        searchHistoryList.add(0, keyword)
        // 保持列表最多10项
        if (searchHistoryList.size > 10) {
            searchHistoryList.removeAt(searchHistoryList.size - 1)
        }

        // 更新历史搜索列表
        refreshHistoryList()
        
        // 保存历史搜索记录
        StorageUtils.put("search_history", searchHistoryList)
    }
    
    @SuppressLint("NotifyDataSetChanged")
    private fun clearSearchHistory() {
        searchHistoryList.clear()
        binding.historySearchList.removeAllViews() // 清空历史搜索列表
        // 清空历史记录
        StorageUtils.delete("search_history")
    }
}