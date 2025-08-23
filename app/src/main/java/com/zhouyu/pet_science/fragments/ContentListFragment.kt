package com.zhouyu.pet_science.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.adapter.PersonalVideoAdapter
import com.zhouyu.pet_science.databinding.FragmentContentListBinding
import com.zhouyu.pet_science.network.ContentHttpUtils.getLikeList
import com.zhouyu.pet_science.network.ContentHttpUtils.getUserVideoList


class ContentListFragment : BaseFragment() {
    private var _binding: FragmentContentListBinding? = null
    private val binding get() = _binding!!
    private var adapter: PersonalVideoAdapter? = null
    private var pageType = "works"
    private var userId: Int = -1
    private var refreshRunnable: Runnable? = null

    // 通过接口获取 PersonalCenterFragment 的引用
    private var personalCenterFragment: PersonalCenterFragment? = null

    // 设置 PersonalCenterFragment 引用的方法
    fun setPersonalCenterFragment(fragment: PersonalCenterFragment) {
        this.personalCenterFragment = fragment
    }

    companion object {
        fun newInstance(pageType: String, userId: Int): ContentListFragment {
            val fragment = ContentListFragment()
            val bundle = Bundle()
            bundle.putString("pageType", pageType)
            bundle.putInt("userId", userId)
            fragment.arguments = bundle
            return fragment
        }

        var refreshLikeList = false
        var refreshWorksList = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContentListBinding.inflate(inflater, container, false)

        binding.recyclerView.apply {
            val spanCount = 3
            layoutManager = GridLayoutManager(context, spanCount)
            this@ContentListFragment.adapter = PersonalVideoAdapter(context)
            adapter = this@ContentListFragment.adapter
        }

        //获取传入的参数
        pageType = arguments?.getString("pageType") ?: "works"
        userId = arguments?.getInt("userId") ?: -1
        initView()

        refreshLikeList = true
        refreshWorksList = true
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 移除延迟任务，防止内存泄漏和 NPE
        refreshRunnable?.let { runnable ->
            _binding?.recyclerView?.removeCallbacks(runnable)
        }
        refreshRunnable = null
        _binding = null
    }


    class NonScrollLinearLayoutManager(context: Context?) : LinearLayoutManager(context) {
        override fun canScrollVertically(): Boolean {
            return false
        }
    }

    private fun initView() {
        refreshLikeList = true
        refreshRunnable = object : Runnable {
            override fun run() {
                // 检查 binding 是否还存在，防止 NPE
                if (_binding == null) return

                if (refreshLikeList && pageType == "likes") {
                    refreshLikeList = false
                    loadList()
                }
                if (refreshWorksList && pageType == "works") {
                    refreshWorksList = false
                    loadList()
                }
                // 再次检查 binding 是否还存在
                _binding?.recyclerView?.postDelayed(this, 500)
            }
        }
        // 安全地调用 post 方法，确保 binding 不为 null
        refreshRunnable?.let { runnable ->
            _binding?.recyclerView?.post(runnable)
        }
    }

    private var isRefreshing = false
    private fun loadList() {
        if (isRefreshing) {
            return
        }
        isRefreshing = true
        //获取点赞列表
        executeThread {
            val list = if (pageType == "likes") {
                getLikeList(userId)
            } else {
                val userVideoList = getUserVideoList(userId)
                if(userVideoList != null){
                    personalCenterFragment?.setUserVideoListSize(userVideoList.data.size)
                }
                userVideoList
            }
            if (list != null) {
                val videoList = list.data
                // 使用 BaseFragment 的 runUiThread 方法，它包含了安全检查
                runUiThread {
                    // 再次检查 Fragment 是否还存在
                    if (_binding != null && isAdded) {
                        adapter?.setVideos(videoList)
                    }
                    // 无论如何都要重置 isRefreshing 状态
                    isRefreshing = false
                }
            } else {
                // 如果获取数据失败，也要重置 isRefreshing 状态
                runUiThread {
                    isRefreshing = false
                }
            }
        }
    }
}