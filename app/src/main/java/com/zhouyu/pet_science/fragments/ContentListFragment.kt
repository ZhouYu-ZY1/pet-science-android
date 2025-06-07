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
import com.zhouyu.pet_science.network.ContentHttpUtils.getLikeList
import com.zhouyu.pet_science.network.ContentHttpUtils.getUserVideoList


class ContentListFragment(private val personalCenterFragment: PersonalCenterFragment) : BaseFragment() {
    private var recyclerView: RecyclerView? = null
    private var adapter: PersonalVideoAdapter? = null
    private var pageType = "works"
    private var userId: Int = -1
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_content_list, container, false)
        recyclerView = view.findViewById<RecyclerView?>(R.id.recycler_view).apply {
            val spanCount = 3
            setLayoutManager(GridLayoutManager(context, spanCount))
            this@ContentListFragment.adapter = PersonalVideoAdapter(context)
            adapter = this@ContentListFragment.adapter
        }

        //获取传入的参数
        pageType = arguments?.getString("pageType") ?: "works"
        userId = arguments?.getInt("userId") ?: -1
        initView()

        refreshLikeList = true
        refreshWorksList = true
        return view
    }


    class NonScrollLinearLayoutManager(context: Context?) : LinearLayoutManager(context) {
        override fun canScrollVertically(): Boolean {
            return false
        }
    }

    private fun initView() {
        refreshLikeList = true
        recyclerView!!.post(object : Runnable {
            override fun run() {
                if (refreshLikeList && pageType == "likes") {
                    refreshLikeList = false
                    loadList()
                }
                if (refreshWorksList && pageType == "works") {
                    refreshWorksList = false
                    loadList()
                }
                recyclerView!!.postDelayed(this, 500)
            }
        })
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
                    personalCenterFragment.setUserVideoListSize(userVideoList.data.size)
                }
                userVideoList
            }
            if (list != null) {
                val videoList = list.data
                requireActivity().runOnUiThread {
                    adapter?.setVideos(videoList)
                    isRefreshing = false
                }
            }
        }
    }

    companion object {
        var refreshLikeList = false
        var refreshWorksList = false
    }
}