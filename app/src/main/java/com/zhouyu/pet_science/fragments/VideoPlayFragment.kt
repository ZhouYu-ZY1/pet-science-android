package com.zhouyu.pet_science.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.zhouyu.pet_science.activities.VideoPlayActivity
import com.zhouyu.pet_science.activities.base.BaseActivity
import com.zhouyu.pet_science.adapter.VideoAdapter
import com.zhouyu.pet_science.databinding.FragmentRecommendVideoBinding
import com.zhouyu.pet_science.layoutmanager.VideoLayoutManager
import com.zhouyu.pet_science.network.ContentHttpUtils
import com.zhouyu.pet_science.pojo.Video
import com.zhouyu.pet_science.utils.VideoUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class VideoPlayFragment : BaseFragment {
    private var activity: BaseActivity? = null
    private var videoRecyclerView: RecyclerView? = null
    private var binding: FragmentRecommendVideoBinding? = null
    private var videoAdapter: VideoAdapter? = null
    private var videoLayoutManager: VideoLayoutManager? = null
    private var currentPosition = 0
    private var videoCache: SimpleCache? = null
    private var isFirstLoad = true
    private var videoList = mutableListOf<Video.Data>()
    private var isLoading = false
    private var listType = "recommend"

    // 预加载配置
    private var preloadCount = 2 // 默认预加载数量

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

        listType = arguments?.getString("listType") ?: "recommend"
        if(listType != "user"){
            setTopBarView(binding!!.container,true)
        }
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        videoCache =  VideoUtils.getVideoCache(requireContext())
        initRecyclerView()
        loadVideos()
    }

    private fun initRecyclerView() {
        videoRecyclerView = binding?.videoRecyclerView

        // 初始化自定义LayoutManager
        videoLayoutManager = VideoLayoutManager(requireContext()).apply {
            initialPrefetchItemCount = 3 // 设置预加载数量
        }

        videoRecyclerView?.let {

            it.layoutManager = videoLayoutManager

            // 使用PagerSnapHelper实现视频滑动效果
            val snapHelper = PagerSnapHelper()
            snapHelper.attachToRecyclerView(it)

            // 优化缓存配置
            it.setItemViewCacheSize(3) // 增加ViewHolder缓存
            it.setHasFixedSize(true) // 优化性能

            // 设置RecycledViewPool大小
            it.recycledViewPool.setMaxRecycledViews(0, 5)

            // 初始化适配器
            videoAdapter = VideoAdapter(requireContext(),this, videoList, videoCache!!)
            it.adapter = videoAdapter


            // 监听滑动事件
            it.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        val position = videoLayoutManager?.findFirstCompletelyVisibleItemPosition() ?: 0
                        if (position != RecyclerView.NO_POSITION && position != currentPosition) {
                            // 停止当前播放的视频
                            videoAdapter?.pauseVideo(currentPosition)
                            currentPosition = position
                            // 播放新位置的视频
                            videoAdapter?.playVideo(currentPosition)

                            // 检查是否需要加载更多视频
                            if (position >= videoList.size - 3 && !isLoading) {
                                loadMoreVideos()
                            }

                            // 预加载后面的视频
                            preloadVideos(position)

                            // 定期清理预加载缓存，优化内存使用
                            if (position % 5 == 0) {
                                videoAdapter?.clearPreloadCache()
                            }
                        }
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    // 在滑动过程中就开始预加载
                    if (dy > 0) { // 向下滑动
                        val position = videoLayoutManager?.findFirstCompletelyVisibleItemPosition() ?: 0
                        if (position != RecyclerView.NO_POSITION) {
                            // 提前预加载下一个视频的图片
                            val nextPosition = position + 1
                            if (nextPosition < videoList.size) {
                                videoAdapter?.preloadImagesForRange(nextPosition, 1)
                            }
                        }
                    }
                }
            })
        }


    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadVideos() {
        isLoading = true
        lifecycleScope.launch {
            if(listType == "user"){
                // 如果是用户视频列表，直接使用传入的列表
                if(VideoPlayActivity.videoList == null) {
                    return@launch
                }
                videoList.clear()
                videoList.addAll(VideoPlayActivity.videoList!!)
                videoAdapter?.currentPlayPosition = VideoPlayActivity.position
                videoAdapter?.notifyDataSetChanged()
                videoRecyclerView?.scrollToPosition(VideoPlayActivity.position)
                currentPosition = VideoPlayActivity.position

                // 立即预加载图片
                videoAdapter?.preloadImagesForRange(currentPosition, 5)

                // 预加载后面的视频
                preloadVideos(currentPosition)
            }else{
                val videos = withContext(Dispatchers.IO) {
                    ContentHttpUtils.getRecommendVideo()
                }
                videos?.data?.let {
                    videoList.clear()
                    videoList.addAll(it)
                    videoAdapter?.notifyDataSetChanged()

//                    // 首次加载完成后播放第一个视频
                    if (isFirstLoad && videoList.isNotEmpty()) {
                        isFirstLoad = false

                        // 立即预加载前几个视频的图片
                        videoAdapter?.preloadImagesForRange(0, 5)

                        // 稍微延迟后开始播放第一个视频
                        lifecycleScope.launch {
                            if(isFragmentVisible()){
                                videoAdapter?.playVideo(0)
                            }
                            // 预加载后面的视频
                            preloadVideos(0)
                        }
                    }
                }
            }
            isLoading = false
        }
    }

    private fun loadMoreVideos() {
        if(listType == "user" || isLoading) {
            return
        }
        isLoading = true
        lifecycleScope.launch {
            val videos = withContext(Dispatchers.IO) {
                ContentHttpUtils.getRecommendVideo()
            }
            videos?.data?.let {
                val oldSize = videoList.size
                videoList.addAll(it)
                videoAdapter?.notifyItemRangeInserted(oldSize, it.size)
            }
            isLoading = false
        }
    }

    private fun preloadVideos(currentPosition: Int) {
        // 根据网络环境动态调整预加载数量
        adjustPreloadCount()

        // 预加载后面的视频
        for (i in 1..preloadCount) {
            val preloadPosition = currentPosition + i
            if (preloadPosition < videoList.size) {
                videoAdapter?.preloadVideo(preloadPosition)
            }
        }
        
        // 预加载前面的视频（用户可能向上滑动）
        val prevPosition = currentPosition - 1
        if (prevPosition >= 0) {
            videoAdapter?.preloadVideo(prevPosition)
        }
    }

    private fun adjustPreloadCount() {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        // 根据网络类型调整预加载数量
        preloadCount = when {
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> 3
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> {
                // 根据网络信号强度进一步调整
                if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_CONGESTED)) 2 else 1
            }
            else -> 1 // 其他网络类型或无网络
        }
    }

    override fun onPause() {
        super.onPause()
        // 暂停所有视频播放
        videoAdapter?.pauseAllVideos()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            // Fragment 被隐藏时暂停播放
            videoAdapter?.pauseAllVideos()
        } else {
            // Fragment 显示时恢复播放
            if (videoList.isNotEmpty()) {
                videoAdapter?.playVideo(currentPosition)
            }
        }
    }
    // 处理Fragment可见性变化
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)

        if (isVisibleToUser && isResumed) {
            // 恢复到上次播放的位置
//            if (lastPlayedPosition >= 0 && lastPlayedPosition < videoList.size) {
//                scrollToPosition(lastPlayedPosition)
//            } else {
//
//            }
            videoAdapter?.playVideo(currentPosition)
        } else if (!isVisibleToUser && isResumed) {
            videoAdapter?.pauseVideo(currentPosition)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 释放所有播放器资源
        videoAdapter?.releaseAllPlayers()
        lifecycleScope.launch(Dispatchers.IO) {
            videoCache?.release()
        }
    }
}
