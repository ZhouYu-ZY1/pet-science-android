package com.zhouyu.pet_science.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.zhouyu.pet_science.activities.SearchActivity
import com.zhouyu.pet_science.adapter.UserAdapter
import com.zhouyu.pet_science.databinding.FragmentSearchResultBinding
import com.zhouyu.pet_science.model.User
import com.zhouyu.pet_science.network.UserHttpUtils

class SearchUserFragment : BaseFragment() {
    private var _binding: FragmentSearchResultBinding? = null
    private val binding get() = _binding!!
    private var isLoading = false
    private var lastSearchKeyword: String = "" // 添加记录上次搜索关键词的变量
    private val adapter by lazy {
        UserAdapter(requireContext(), emptyList())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchResultBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        // 预先设置固定大小可以提高RecyclerView性能
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.adapter = adapter
    }
    
    @SuppressLint("NotifyDataSetChanged")
    fun updateSearchResults(keyword: String) {
        if (isLoading) return
        if (keyword == lastSearchKeyword) return // 如果关键词与上次相同，不重复搜索
        
        isLoading = true
        lastSearchKeyword = keyword // 记录本次搜索的关键词
        
        executeThread {
            val userList = UserHttpUtils.searchUser(keyword)
            // 检查Fragment是否仍然附加到Activity
            if (activity != null && !isDetached) {
                activity?.runOnUiThread {
                    // 再次检查，确保在UI线程执行时Fragment仍然有效
                    if (!isDetached && _binding != null) {
                        adapter.updateData(userList) // 更新适配器数据
                        adapter.notifyDataSetChanged() // 通知适配器数据已更改
                        isLoading = false
                    }
                }
            } else {
                // Fragment已分离，只需重置加载状态
                isLoading = false
            }
        }
    }
    
    // 添加判断是否已搜索过该关键词的方法
    fun hasSearched(keyword: String): Boolean {
        return keyword == lastSearchKeyword && !isLoading
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}