package com.zhouyu.pet_science.fragments

import Product
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.zhouyu.pet_science.adapter.ProductAdapter
import com.zhouyu.pet_science.databinding.FragmentSearchResultBinding
import com.zhouyu.pet_science.network.ProductHttpUtils

class SearchProductFragment : BaseFragment() {
    private var _binding: FragmentSearchResultBinding? = null
    private val binding get() = _binding!!
    private var isLoading = false
    private var productItems: MutableList<Product> = ArrayList()
    private var lastSearchKeyword: String = "" // 添加记录上次搜索关键词的变量
    
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
        
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerView.adapter = ProductAdapter(productItems)
    }
    
    fun updateSearchResults(keyword: String) {
        if (isLoading) return
        if (keyword == lastSearchKeyword) return // 如果关键词与上次相同，不重复搜索
        
        isLoading = true
        lastSearchKeyword = keyword // 记录本次搜索的关键词
        
        executeThread {
            productItems = ProductHttpUtils.searchProduct(1,10,keyword)
            // 检查Fragment是否仍然附加到Activity
            if (activity != null && !isDetached) {
                activity?.runOnUiThread {
                    // 再次检查，确保在UI线程执行时Fragment仍然有效
                    if (!isDetached && _binding != null) {
                        binding.recyclerView.adapter = ProductAdapter(productItems)
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