package com.zhouyu.pet_science.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.zhouyu.pet_science.activities.base.BaseActivity
import com.zhouyu.pet_science.adapter.VideoAdapter
import com.zhouyu.pet_science.databinding.FragmentRecommendVideoBinding
import com.zhouyu.pet_science.network.ContentHttpUtils
import com.zhouyu.pet_science.pojo.Video
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VideoPlayFragment : BaseFragment {
    private var activity: BaseActivity? = null
    private var binding: FragmentRecommendVideoBinding? = null
    private var videoAdapter: VideoAdapter? = null
    private var videoList: MutableList<Video.Data> = mutableListOf()
    private var currentPosition = 0
    private var isFragmentVisible = false
    private var lastPlayedPosition = 0

    constructor()
    constructor(activity: BaseActivity?) {
        this.activity = activity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (activity == null) {
            activity = getActivity() as BaseActivity?
        }
        binding = FragmentRecommendVideoBinding.inflate(inflater, container, false)
        return binding!!.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        loadVideos()
    }
    
    private fun setupRecyclerView() {
        // 创建适配器
        videoAdapter = VideoAdapter(videoList)
        
        // 设置RecyclerView
        binding?.videoRecyclerView?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = videoAdapter
            
            // 使用PagerSnapHelper实现类似ViewPager的效果
            val snapHelper = PagerSnapHelper()
            snapHelper.attachToRecyclerView(this)
            
            // 监听滚动事件，实现只播放当前视频
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        val layoutManager = recyclerView.layoutManager
                        val firstVisibleItemPosition = (layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
                        
                        if (firstVisibleItemPosition != RecyclerView.NO_POSITION && firstVisibleItemPosition != currentPosition) {
                            // 记录上一个播放位置
                            lastPlayedPosition = currentPosition
                            
                            // 播放新的视频
                            val viewHolder = recyclerView.findViewHolderForAdapterPosition(firstVisibleItemPosition) as? VideoAdapter.VideoViewHolder
                            viewHolder?.let {
                                videoAdapter?.playVideo(firstVisibleItemPosition, it)
                            }
                            
                            currentPosition = firstVisibleItemPosition
                        }
                    }
                }
            })
        }
    }
    
    private fun loadVideos() {
        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    ContentHttpUtils.getRecommendVideo()
                }

                result?.let { video ->
                    if (video.data.isNotEmpty()) {
                        videoAdapter?.setVideoList(video.data)

                        // 播放第一个视频
                        if (videoList.isNotEmpty() && isResumed && isFragmentVisible) {
                            binding?.videoRecyclerView?.post {
                                val viewHolder = binding?.videoRecyclerView?.findViewHolderForAdapterPosition(0) as? VideoAdapter.VideoViewHolder
                                viewHolder?.let {
                                    videoAdapter?.playVideo(0, it)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    // 滚动到指定位置
    private fun scrollToPosition(position: Int) {
        binding?.videoRecyclerView?.post {
            binding?.videoRecyclerView?.scrollToPosition(position)
            binding?.videoRecyclerView?.post {
                val viewHolder = binding?.videoRecyclerView?.findViewHolderForAdapterPosition(position) as? VideoAdapter.VideoViewHolder
                viewHolder?.let {
                    videoAdapter?.playVideo(position, it)
                }
                currentPosition = position
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        isFragmentVisible = true
        if (isResumed && videoList.isNotEmpty()) {
            // 恢复到上次播放的位置
            if (lastPlayedPosition >= 0 && lastPlayedPosition < videoList.size) {
                scrollToPosition(lastPlayedPosition)
            } else {
                videoAdapter?.resumeCurrentPlayer()
            }
        }
    }
    
    override fun onPause() {
        super.onPause()
        videoAdapter?.pauseCurrentPlayer()
    }
    
    override fun onStop() {
        super.onStop()
        isFragmentVisible = false
    }
    
    override fun onDestroy() {
        super.onDestroy()
        videoAdapter?.releaseAllPlayers()
        binding = null
    }
    
    // 处理Fragment可见性变化
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        isFragmentVisible = isVisibleToUser
        
        if (isVisibleToUser && isResumed) {
            // 恢复到上次播放的位置
            if (lastPlayedPosition >= 0 && lastPlayedPosition < videoList.size) {
                scrollToPosition(lastPlayedPosition)
            } else {
                videoAdapter?.resumeCurrentPlayer()
            }
        } else if (!isVisibleToUser && isResumed) {
            videoAdapter?.pauseCurrentPlayer()
        }
    }
}