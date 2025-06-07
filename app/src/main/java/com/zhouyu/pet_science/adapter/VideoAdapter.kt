package com.zhouyu.pet_science.adapter

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.youth.banner.Banner
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.fragments.ContentListFragment
import com.zhouyu.pet_science.fragments.VideoPlayFragment
import com.zhouyu.pet_science.network.ContentHttpUtils
import com.zhouyu.pet_science.pojo.Video
import com.zhouyu.pet_science.utils.ConsoleUtils
import com.zhouyu.pet_science.utils.EventUtils
import com.zhouyu.pet_science.utils.MyToast
import com.zhouyu.pet_science.views.LoveView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class VideoAdapter(private val context: Context,private val videoPlayFragment: VideoPlayFragment, private val videos: List<Video.Data>, private val videoCache: SimpleCache) :
    RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {
    
    // 播放器池，复用ExoPlayer实例
    private val playerPool = ConcurrentHashMap<Int, ExoPlayer>()
    // 音频播放器池，用于图文内容的背景音乐
    private val audioPlayerPool = ConcurrentHashMap<Int, ExoPlayer>()
    private val holderPool = ConcurrentHashMap<Int, VideoViewHolder>()
    // 预加载状态跟踪
    private val preloadedPositions = HashSet<Int>()
    // 图片预加载状态跟踪
    private val preloadedImages = HashSet<Int>()
    // 协程作用域
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    // 用于取消预加载任务
    private val preloadJobs = ConcurrentHashMap<Int, Job>()
    var currentPlayPosition = 0 // 当前播放视频的位置

    // 图文轮播相关
    private val audioPlayingStates = ConcurrentHashMap<Int, Boolean>() // 音频播放状态
    
    // 播放器池大小限制，防止内存泄漏
    private val maxPlayerPoolSize = 11
    // 记录播放进度
    val playbackPositions = ConcurrentHashMap<Int, Long>()
    // 20秒阈值
    val playbackThreshold = 20000L
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view)
    }

    // 数字格式化
    private fun Int.formatCount(): String {
        return when {
            this >= 10000 -> "%.1f万".format(this / 10000f)
            else -> toString()
        }
    }
    
    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = videos[position]
        holderPool[position] = holder // 缓存ViewHolder

        // 设置视频信息
        holder.authorNickname.text = video.nickname
        holder.videoTitle.text = video.desc
        // 设置点赞、评论、分享数量
        holder.tvLikeCount.text = video.diggCount?.formatCount() ?: "0"
        holder.tvCommentCount.text = video.commentCount?.formatCount() ?: "0"
        holder.tvShareCount.text = video.shareCount?.formatCount() ?: "0"

        // 加载作者头像
        Glide.with(context)
            .load(video.authorAvatar)
            .placeholder(R.drawable.default_avatar)
            .into(holder.authorAvatar)

        // 根据类型显示不同内容
        when (video.type) {
            "68" -> {
                // 图文内容
                setupImageContent(holder, video, position)
            }
            "0", "1" -> {
                // 视频内容
                setupVideoContent(holder, video, position)
            }
            else -> {
                // 默认按视频处理
                setupVideoContent(holder, video, position)
            }
        }

        // 设置双击点赞
        setupEvent(holder, video, position)
        
        // 设置点赞状态
        if (video.isLike) {
            holder.likeBtn.imageTintList = null
        } else {
            holder.likeBtn.imageTintList = holder.itemView.context.getColorStateList(R.color.white)
        }
        // 设置爱心点击事件
        holder.likeBtn.setOnClickListener {
            if (video.isLike) {
                holder.likeBtn.imageTintList = holder.itemView.context.getColorStateList(R.color.white)
                video.isLike = false
                Thread {
                    if (ContentHttpUtils.likeVideo(false, video)) {
                        video.isLike = false
                        ContentListFragment.refreshLikeList = true
                    } else {
                        video.isLike = true
                        holder.itemView.post {
                            holder.likeBtn.imageTintList = null
                            MyToast.show("取消点赞失败")
                        }
                    }
                }.start()
            } else {
                holder.likeBtn.imageTintList = null
                video.isLike = true
                Thread {
                    if (ContentHttpUtils.likeVideo(true, video)) {
                        video.isLike = true
                        ContentListFragment.refreshLikeList = true
                    } else {

                        video.isLike = false
                        holder.itemView.post {
                            holder.likeBtn.imageTintList = holder.itemView.context.getColorStateList(R.color.white)
                            MyToast.show("点赞失败")
                        }
                    }
                }.start()
            }
        }

        // 设置评论点击事件
        holder.commentBtn.setOnClickListener {
            // 处理评论逻辑
        }
    }

    // 设置图文内容
    private fun setupImageContent(holder: VideoViewHolder, video: Video.Data, position: Int) {
        // 隐藏视频相关组件
        holder.playerView.visibility = View.GONE
        holder.videoBackgroundImage.visibility = View.GONE
        holder.seekBarParent.visibility = View.GONE

        // 显示图文轮播和指示器
        holder.imageBanner.visibility = View.VISIBLE
        holder.imageIndicatorLayout.visibility = View.VISIBLE
        holder.bannerTouchOverlay.visibility = View.VISIBLE // 显示透明触摸覆盖层
        holder.videoPlayImage.visibility = View.VISIBLE // 显示播放按钮用于音乐控制

        // 解析图片URL列表（从coverSrc获取，用分号分割）
        val imageUrls = video.coverSrc?.split(";")?.filter { it.isNotEmpty() } ?: emptyList()

        if (imageUrls.isNotEmpty()) {
            // 设置轮播适配器
            val adapter = ImageBannerAdapter(imageUrls)
            holder.imageBanner.setAdapter(adapter)

            // 配置Banner（不使用内置指示器）
            holder.imageBanner.apply {
                // 启用自动轮播
                isAutoLoop(true)
                // 设置轮播间隔为3秒
                setLoopTime(3000)
                // 设置触摸滑动
                setUserInputEnabled(true)
                // 添加生命周期观察者
                addBannerLifecycleObserver(videoPlayFragment)
                // 设置Banner可点击
                isClickable = true
                isFocusable = true
            }

            // 创建自定义指示器
            setupCustomIndicator(holder, imageUrls.size, position)

            // 设置背景音乐
            setupBackgroundMusic(holder, video, position)
        }
    }

    // 设置自定义指示器
    private fun setupCustomIndicator(holder: VideoViewHolder, imageCount: Int, position: Int) {
        holder.imageIndicatorLayout.removeAllViews()

        val indicators = mutableListOf<View>()

        for (i in 0 until imageCount) {
            val indicator = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    (16 * context.resources.displayMetrics.density).toInt(),
                    (4 * context.resources.displayMetrics.density).toInt()
                ).apply {
                    if (i > 0) leftMargin = (8 * context.resources.displayMetrics.density).toInt()
                }
                setBackgroundResource(R.drawable.banner_indicator_normal)
            }
            indicators.add(indicator)
            holder.imageIndicatorLayout.addView(indicator)
        }

        // 设置第一个为选中状态
        if (indicators.isNotEmpty()) {
            indicators[0].setBackgroundResource(R.drawable.banner_indicator_selected)
        }

        // 监听Banner页面切换
        holder.imageBanner.addOnPageChangeListener(object : com.youth.banner.listener.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                // 更新指示器状态
                indicators.forEachIndexed { index, view ->
                    if (index == position) {
                        view.setBackgroundResource(R.drawable.banner_indicator_selected)
                    } else {
                        view.setBackgroundResource(R.drawable.banner_indicator_normal)
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    // 设置背景音乐
    private fun setupBackgroundMusic(holder: VideoViewHolder, video: Video.Data, position: Int) {
        if (!video.videoSrc.isNullOrEmpty()) {
            // 创建音频播放器
            val audioPlayer = getOrCreateAudioPlayer(position)

            // 准备音频媒体源
            coroutineScope.launch(Dispatchers.Main) {
                try {
                    val mediaItem = MediaItem.fromUri(video.videoSrc)
                    val mediaSource = withContext(Dispatchers.IO) {
                        buildMediaSource(mediaItem)
                    }
                    audioPlayer.setMediaSource(mediaSource)
                    audioPlayer.prepare()
                    audioPlayer.repeatMode = Player.REPEAT_MODE_ONE // 循环播放

                    // 设置播放状态监听
                    audioPlayer.addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(state: Int) {
                            when (state) {
                                Player.STATE_READY -> {
                                    // 音频准备就绪，如果是当前播放位置则自动播放
                                    if (position == currentPlayPosition && videoPlayFragment.isFragmentVisible()) {
                                        audioPlayer.playWhenReady = true
                                        audioPlayingStates[position] = true
                                        holder.videoPlayImage.visibility = View.GONE
                                    }
                                }
                            }

                            // 非当前播放不显示播放图标
                            if (position != currentPlayPosition) {
                                holder.videoPlayImage.visibility = View.GONE
                                return
                            }
                        }

                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            audioPlayingStates[position] = isPlaying
                            // 非当前播放不显示播放图标
                            if (position != currentPlayPosition) {
                                holder.videoPlayImage.visibility = View.GONE
                                return
                            }

                            // 同步轮播状态
                            holder.imageBanner.isAutoLoop(isPlaying)
                        }
                    })

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }



    // 创建或获取音频播放器
    private fun getOrCreateAudioPlayer(position: Int): ExoPlayer {
        return audioPlayerPool.getOrPut(position) {
            createAudioPlayerInstance()
        }
    }

    // 创建音频播放器实例
    private fun createAudioPlayerInstance(): ExoPlayer {
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(3000, 8000, 1000, 1000)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        return ExoPlayer.Builder(context)
            .setRenderersFactory(DefaultRenderersFactory(context).setEnableDecoderFallback(true))
            .setLoadControl(loadControl)
            .build().apply {
                repeatMode = Player.REPEAT_MODE_ONE
            }
    }

    // 设置视频内容
    private fun setupVideoContent(holder: VideoViewHolder, video: Video.Data, position: Int) {
        // 显示视频相关组件
        holder.playerView.visibility = View.VISIBLE
        holder.videoBackgroundImage.visibility = View.VISIBLE
        holder.seekBarParent.visibility = View.VISIBLE

        // 隐藏图文轮播和指示器
        holder.imageBanner.visibility = View.GONE
        holder.imageIndicatorLayout.visibility = View.GONE
        holder.bannerTouchOverlay.visibility = View.GONE // 隐藏透明触摸覆盖层

        // 设置播放器
        setupPlayer(holder, position)

        // 设置封面图
        if (video.coverSrc.isNotEmpty()) {
            Glide.with(context)
                .load(video.coverSrc)
                .into(holder.videoBackgroundImage)
        }

        // 设置进度条
        setupSeekBar(holder)
    }

    private fun setupPlayer(holder: VideoViewHolder, position: Int) {
        // 获取或创建播放器
        val player = getOrCreatePlayer(position)

        // 设置播放器视图
        holder.playerView.apply {
            this.player = player
            // 设置视频缩放模式
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            // 设置视频背景色为黑色
            setBackgroundColor(Color.BLACK)
            // 设置控制器可见性
            useController = false
        }

        // 设置播放状态监听
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_READY -> {
                        // 播放器准备就绪
                        if(position == currentPlayPosition && videoPlayFragment.isFragmentVisible()){
                            playVideo(currentPlayPosition)
                        }
                        // 当前视频准备就绪后，立即预加载后续视频
                        if (position == currentPlayPosition) {
                            triggerPreloadAfterReady(position)
                        }
                    }
                    Player.STATE_ENDED -> {
                        // 播放结束，重新开始
                        player.seekTo(0)
                        player.play()
                    }
                    Player.STATE_BUFFERING -> {
                        // 正在缓冲
                    }
                    Player.STATE_IDLE -> {
                        // 播放器空闲
                    }
                }
                // 非当前播放视频不显示播放图标
                if (position != currentPlayPosition) {
                    holder.videoPlayImage.visibility = View.GONE
                    return
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                // 非当前播放视频不显示播放图标
                if (position != currentPlayPosition) {
                    holder.videoPlayImage.visibility = View.GONE
                    return
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                // 处理播放错误
                error.printStackTrace()
                // 尝试重新准备播放器
                player.prepare()
            }
        })

        // 准备媒体源 - 只对视频内容设置
        val video = videos[position]
        if (!video.videoSrc.isNullOrEmpty() && video.type != "68") {
            coroutineScope.launch(Dispatchers.Main) {
                try {
                    val mediaItem = MediaItem.fromUri(video.videoSrc)
                    // 在IO线程构建媒体源
                    val mediaSource = withContext(Dispatchers.IO) {
                        buildMediaSource(mediaItem)
                    }
                    // 回到主线程设置媒体源
                    player.setMediaSource(mediaSource)
                    player.prepare()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // 当前视频准备就绪后触发预加载
    private fun triggerPreloadAfterReady(currentPosition: Int) {
        coroutineScope.launch {
            // 延迟一小段时间确保当前视频稳定播放
            delay(500)

            // 预加载后续2-3个视频
            for (i in 1..3) {
                val preloadPosition = currentPosition + i
                if (preloadPosition < videos.size) {
                    preloadVideo(preloadPosition)
                    // 每个预加载之间稍微间隔，避免同时大量网络请求
                    delay(200)
                }
            }
        }
    }

    private fun setupEvent(holder: VideoViewHolder, video: Video.Data, position: Int) {
        val player = holder.playerView.player
        val videoPlayImage = holder.videoPlayImage

        // 根据内容类型设置不同的事件处理
        if (video.type == "68") {
            // 图文内容的事件处理
            setupImageEvent(holder, video, position)
        } else {
            // 视频内容的事件处理
            setupVideoEvent(holder, video, position, player, videoPlayImage)
        }
    }

    // 图文内容事件处理
    @SuppressLint("ClickableViewAccessibility")
    private fun setupImageEvent(holder: VideoViewHolder, video: Video.Data, position: Int) {
        // 在透明覆盖层上设置触摸监听器
        holder.bannerTouchOverlay.setOnTouchListener(EventUtils.OnDoubleClickListener(object : EventUtils.OnDoubleClickListener.DoubleClickCallback {
            override fun onDoubleClick(event: MotionEvent?) {
                handleDoubleTap(event, holder, video)
            }
            override fun onClick(event: MotionEvent?) {
                // 图文内容单击事件：暂停/继续背景音乐和轮播
                toggleImageContentPlayback(holder, position)
            }

            override fun onLongPress(event: MotionEvent?) {
                // 图文内容长按事件（可以添加其他逻辑）
            }

            override fun onLongPressFinish(event: MotionEvent?) {
                // 图文内容长按结束事件
            }

            override fun onTouch(view: View?, event: MotionEvent?): Boolean {
                // 将触摸事件传递给Banner，让Banner处理滑动
                if (event != null) {
                    // 先让Banner处理触摸事件（用于滑动）
                    holder.imageBanner.dispatchTouchEvent(event)
                }
                return true
            }
        }, holder.itemView.context))
    }

    // 切换图文内容播放状态
    private fun toggleImageContentPlayback(holder: VideoViewHolder, position: Int) {
        val audioPlayer = audioPlayerPool[position]
        val isAudioPlaying = audioPlayingStates[position] ?: false

        if (audioPlayer != null) {
            if (isAudioPlaying) {
                // 暂停音乐和轮播
                audioPlayer.pause()
                holder.imageBanner.isAutoLoop(false)
                // 显示播放按钮
                holder.videoPlayImage.apply {
                    visibility = View.VISIBLE
                    alpha = 0.8f
                    scaleX = 1.5f
                    scaleY = 1.5f
                    animate().scaleX(1f).scaleY(1f).setDuration(100)
                }
            } else {
                // 继续音乐和轮播
                audioPlayer.play()
                holder.imageBanner.isAutoLoop(true)
                // 隐藏播放按钮
                holder.videoPlayImage.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction { holder.videoPlayImage.visibility = View.GONE }
            }
        }
    }

    // 视频内容事件处理
    private fun setupVideoEvent(holder: VideoViewHolder, video: Video.Data, position: Int, player: Player?, videoPlayImage: ImageView) {
        val itemView = holder.itemView
        itemView.setOnTouchListener(EventUtils.OnDoubleClickListener(object : EventUtils.OnDoubleClickListener.DoubleClickCallback {
            override fun onDoubleClick(event: MotionEvent?) {
                handleDoubleTap(event, holder, video)
            }
            override fun onClick(event: MotionEvent?) {
                // 单击事件逻辑，切换播放/暂停
                videoPlayImage.apply {
                    if (player?.isPlaying == true) {
                        pauseVideo(position)
                        // 缩小动画
                        visibility = View.VISIBLE
                        scaleX = 1.5f;scaleY = 1.5f;alpha = 0.6f
                        animate().scaleX(1f).scaleY(1f)
                            .setDuration(100).withEndAction(null)
                    } else {
                        playVideo(position)
                        // 淡出动画
                        animate().alpha(0f).setDuration(200)
                            .withEndAction { visibility = View.GONE }
                    }
                }
            }

            override fun onLongPress(event: MotionEvent?) {
                // 长按开始逻辑，例如加速播放
                holder.isLongPress = true
                setPlaySpeed(3.0f)
            }

            override fun onLongPressFinish(event: MotionEvent?) {
                // 长按结束逻辑，例如恢复正常播放速度
                holder.isLongPress = false
                setPlaySpeed(1.0f)
            }

            private fun setPlaySpeed(fl: Float) {
                if (player != null && player.playbackState == Player.STATE_READY) {
                    player.setPlaybackSpeed(fl)
                } else {
                    itemView.post { MyToast.show("视频未准备好") }
                }
            }
        }, itemView.context))
    }

    // 统一的双击点赞处理
    private fun handleDoubleTap(event: MotionEvent?, holder: VideoViewHolder, video: Video.Data) {
        if(event != null){
            holder.loveView.addLoveView(event)
        }
        if (!video.isLike) {
            video.isLike = true
            holder.likeBtn.imageTintList = null
            Thread {
                if (ContentHttpUtils.likeVideo(true, video)) {
                    video.isLike = true
                    ContentListFragment.refreshLikeList = true
                } else {
                    video.isLike = false
                    holder.itemView.post {
                        holder.likeBtn.imageTintList = holder.itemView.context.getColorStateList(R.color.white)
                        MyToast.show("点赞失败")
                    }
                }
            }.start()
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun setupSeekBar(holder: VideoViewHolder) {
        val player = holder.playerView.player as? ExoPlayer ?: return
        holder.seekBar.progress = 0 // 初始化进度条为0
        // 更新进度条
        coroutineScope.launch {
            while (true) {
                if (player.isPlaying && !holder.isSeekBarTouch) {
                    val duration = player.duration
                    val position = player.currentPosition
                    if (duration > 0) {
                        holder.seekBar.max = duration.toInt()
                        holder.seekBar.progress = position.toInt()
                    }
                }
                delay(500) // 每500ms更新一次
            }
        }
        
        // 设置拖动监听
        holder.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // 更新当前时间显示
                if (fromUser && holder.isSeekBarTouch) {
                    updateTimeText(holder, progress.toLong(), player.duration)
                }
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar) {
                holder.isSeekBarTouch = true
                // 使用动画放大 SeekBar
                animateSeekBarHeight(seekBar, 8.dpToPx(holder.itemView.context))
                
                // 显示时间文本
                holder.timeTextView.visibility = View.VISIBLE
                updateTimeText(holder, player.currentPosition, player.duration)
                
                // 隐藏标题和作者昵称
                holder.authorInfoLayout.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction { holder.authorInfoLayout.visibility = View.INVISIBLE }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val progress = seekBar.progress
                player.apply {
                    val duration: Long = duration
                    val newPosition: Long = duration / seekBar.max * progress
                    seekTo(newPosition) //修改播放器进度
                    if(!isPlaying){
                        playWhenReady = true //如果播放器未在播放，则开始播放
                    }
                }

                // 使用动画恢复 SeekBar 大小
                animateSeekBarHeight(seekBar, 2.dpToPx(holder.itemView.context))
                
                // 隐藏时间文本
                holder.timeTextView.visibility = View.GONE
                
                // 显示标题和作者昵称
                holder.authorInfoLayout.visibility = View.VISIBLE
                holder.authorInfoLayout.animate()
                    .alpha(1f)
                    .setDuration(200)
              
                holder.isSeekBarTouch = false
            }
        })

        //增加seekbar触摸区域
        holder.seekBarParent.setOnTouchListener(OnTouchListener { _: View?, event: MotionEvent ->
            val seekRect = Rect()
            holder.seekBar.getHitRect(seekRect)
            if (event.y >= seekRect.top - 500 && event.y <= seekRect.bottom + 500) {
                val y = (seekRect.top + (seekRect.height() shr 1)).toFloat()
                var x = event.x - seekRect.left
                if (x < 0) {
                    x = 0f
                } else if (x > seekRect.width()) {
                    x = seekRect.width().toFloat()
                }
                val me = MotionEvent.obtain(
                    event.downTime, event.eventTime,
                    event.action, x, y, event.metaState
                )
                return@OnTouchListener holder.seekBar.onTouchEvent(me)
            }
            false
        })
    }
    
    private fun Int.dpToPx(context: Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
    
    // 更新时间文本显示
    private fun updateTimeText(holder: VideoViewHolder, position: Long, duration: Long) {
        val positionStr = formatTime(position)
        val durationStr = formatTime(duration)
        holder.timeTextView.text = "$positionStr / $durationStr"
    }
    
    // 格式化时间为 mm:ss 格式
    private fun formatTime(timeMs: Long): String {
        val totalSeconds = timeMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun getOrCreatePlayer(position: Int): ExoPlayer {
//        // 检查播放器池大小，如果超过限制则清理远离当前位置的播放器
        if (playerPool.size >= maxPlayerPoolSize) {
            cleanupDistantPlayers(position)
        }

        // 更积极的初始化策略：提前初始化前5个播放器
        if (playerPool.isEmpty() && videos.size > 0) {
            val initCount = minOf(5, videos.size)
            (0 until initCount).forEach { i ->
                if (!playerPool.containsKey(i)) {
                    playerPool[i] = createPlayerInstance()
                }
            }
        }

        return playerPool.getOrPut(position) {
            createPlayerInstance().apply {
                // 设置初始参数
                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
                repeatMode = Player.REPEAT_MODE_ONE
            }
        }
    }
    private fun createPlayerInstance(): ExoPlayer {
        // 创建播放器实例
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                3000, // 减少最小缓冲时间
                8000, // 减少最大缓冲时间
                1000, // 播放缓冲
                1000 // 重新缓冲播放缓冲
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()

        return ExoPlayer.Builder(context)
            .setRenderersFactory(DefaultRenderersFactory(context).setEnableDecoderFallback(true))
            .setLoadControl(loadControl)
            .build().apply {
                // 设置视频缩放模式为适应宽度
                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
                // 循环播放
                repeatMode = Player.REPEAT_MODE_ONE
            }
    }
    
    // 清理远离当前播放位置的播放器
    private fun cleanupDistantPlayers(currentPos: Int) {
        val playersToRemove = mutableListOf<Int>()
        
        for ((position, player) in playerPool) {
            // 保留当前播放位置前后5个位置的播放器
            if (kotlin.math.abs(position - currentPos) > 5) {
                playersToRemove.add(position)
            }
        }
        
        // 移除远离的播放器
        playersToRemove.forEach { position ->
            playerPool[position]?.let { player ->
                try {
                    // 保存播放进度
                    playbackPositions[position] = player.currentPosition
                    player.stop()
                    player.clearMediaItems()
                    player.release()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            playerPool.remove(position)
        }
    }
    
    private fun buildMediaSource(mediaItem: MediaItem): MediaSource {
        // 创建HTTP数据源工厂
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15000) // 连接超时
            .setReadTimeoutMs(15000) // 读取超时
        
        // 创建默认数据源工厂
        val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)
        
        // 创建缓存数据源工厂
        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(videoCache)
            .setUpstreamDataSourceFactory(dataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        
        // 创建媒体源
        return ProgressiveMediaSource.Factory(cacheDataSourceFactory)
            .createMediaSource(mediaItem)
    }

    // 设置播放位置
    fun setPlayPosition(position: Int) {
        val player = playerPool[position] ?: return
        // 检查播放进度，如果小于20秒则从头开始播放
        val savedPosition = playbackPositions[position] ?: 0L
        if (savedPosition < playbackThreshold) {
            player.seekTo(0)
        } else {
            player.seekTo(savedPosition)
        }
    }
    
    fun playVideo(position: Int) {
        if (position < 0 || position >= videos.size) return

        val video = videos[position]
        currentPlayPosition = position

        // 根据内容类型处理
        when (video.type) {
            "68" -> {
                // 图文内容，播放背景音乐和启动轮播
                val audioPlayer = audioPlayerPool[position]
                val holder = holderPool[position]
                if (audioPlayer != null && holder != null) {
                    audioPlayer.playWhenReady = true
                    holder.imageBanner.isAutoLoop(true)
                }
                holder?.videoPlayImage?.visibility = View.GONE
            }
            "0", "1" -> {
                // 视频内容
                val player = playerPool[position] ?: return
                holderPool[position]?.videoPlayImage?.visibility = View.GONE
                try {
                    player.playWhenReady = true
                } catch (e: Exception) {
                    e.printStackTrace()
                    // 如果播放失败，尝试重新创建播放器
                    playerPool.remove(position)
                    val newPlayer = getOrCreatePlayer(position)
                    newPlayer.playWhenReady = true
                }
            }
            else -> {
                // 默认按视频处理
                val player = playerPool[position] ?: return
                holderPool[position]?.videoPlayImage?.visibility = View.GONE
                try {
                    player.playWhenReady = true
                } catch (e: Exception) {
                    e.printStackTrace()
                    playerPool.remove(position)
                    val newPlayer = getOrCreatePlayer(position)
                    newPlayer.playWhenReady = true
                }
            }
        }
    }
    
    fun pauseVideo(position: Int) {
        if (position < 0 || position >= videos.size) return

        val video = videos[position]

        // 根据内容类型处理
        when (video.type) {
            "68" -> {
                // 图文内容，暂停背景音乐和轮播
                val audioPlayer = audioPlayerPool[position]
                val holder = holderPool[position]
                if (audioPlayer != null && holder != null) {
                    audioPlayer.playWhenReady = false
                    holder.imageBanner.isAutoLoop(false)
                }
            }
            "0", "1" -> {
                // 视频内容
                val player = playerPool[position] ?: return
                try {
                    // 保存当前播放进度
                    playbackPositions[position] = player.currentPosition
                    player.playWhenReady = false
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            else -> {
                // 默认按视频处理
                val player = playerPool[position] ?: return
                try {
                    playbackPositions[position] = player.currentPosition
                    player.playWhenReady = false
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    fun pauseAllVideos() {
        // 暂停所有视频播放器
        for (player in playerPool.values) {
            try {
                player.playWhenReady = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 暂停所有音频播放器和轮播
        for ((position, audioPlayer) in audioPlayerPool) {
            try {
                audioPlayer.playWhenReady = false
                val holder = holderPool[position]
                holder?.imageBanner?.isAutoLoop(false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun releaseAllPlayers() {
        // 保存所有播放器的当前进度
        for ((position, player) in playerPool) {
            try {
                playbackPositions[position] = player.currentPosition
                player.stop()
                player.clearMediaItems()
                player.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        playerPool.clear()

        // 释放所有音频播放器
        for (audioPlayer in audioPlayerPool.values) {
            try {
                audioPlayer.stop()
                audioPlayer.clearMediaItems()
                audioPlayer.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        audioPlayerPool.clear()
        audioPlayingStates.clear()

        holderPool.clear()

        // 取消所有预加载任务
        for (job in preloadJobs.values) {
            job.cancel()
        }
        preloadJobs.clear()
        preloadedPositions.clear()
        preloadedImages.clear()
    }
    
    // 清理预加载状态，用于内存优化
    fun clearPreloadCache() {
        // 只保留当前播放位置附近的预加载状态
        val positionsToKeep = (currentPlayPosition - 2..currentPlayPosition + 5).toSet()
        preloadedPositions.retainAll(positionsToKeep)
        preloadedImages.retainAll(positionsToKeep)

        // 取消远离当前位置的预加载任务
        val jobsToCancel = preloadJobs.filterKeys { position ->
            kotlin.math.abs(position - currentPlayPosition) > 5
        }

        jobsToCancel.forEach { (position, job) ->
            job.cancel()
            preloadJobs.remove(position)
        }
    }

    // 预加载视频
    fun preloadVideo(position: Int) {
        if (position < 0 || position >= videos.size || preloadedPositions.contains(position)) return

        val video = videos[position]

        // 根据内容类型预加载
        when (video.type) {
            "68" -> {
                // 图文内容，只预加载图片
                preloadImagesOnly(video, position)
            }
            "0", "1" -> {
                // 视频内容，预加载视频和图片
                preloadVideoContent(video, position)
            }
            else -> {
                // 默认按视频处理
                preloadVideoContent(video, position)
            }
        }
    }

    // 只预加载图片（用于图文内容）
    private fun preloadImagesOnly(video: Video.Data, position: Int) {
        preloadJobs[position] = coroutineScope.launch(Dispatchers.Main) {
            try {
                if (!preloadedImages.contains(position)) {
                    preloadImages(video, position)
                }

                // 如果是图文内容，预加载图片列表中的图片（从coverSrc获取）
                if (video.type == "68" && !video.coverSrc.isNullOrEmpty()) {
                    val imageUrls = video.coverSrc.split(";").filter { it.isNotEmpty() }
                    imageUrls.forEach { url ->
                        Glide.with(context)
                            .load(url)
                            .preload()
                        delay(50) // 每个图片之间稍微延迟
                    }
                }

                // 预加载背景音乐
                if (video.type == "68" && !video.videoSrc.isNullOrEmpty()) {
                    try {
                        val audioPlayer = getOrCreateAudioPlayer(position)
                        val mediaItem = MediaItem.fromUri(video.videoSrc)
                        val mediaSource = withContext(Dispatchers.IO) {
                            buildMediaSource(mediaItem)
                        }
                        audioPlayer.setMediaSource(mediaSource)
                        audioPlayer.prepare()
                        audioPlayer.playWhenReady = false // 不自动播放
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                preloadedPositions.add(position)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 预加载视频内容
    private fun preloadVideoContent(video: Video.Data, position: Int) {
        // 检查播放器是否已经存在且准备好
        val existingPlayer = playerPool[position]
        if (existingPlayer != null && existingPlayer.playbackState == Player.STATE_READY) {
            preloadedPositions.add(position)
            return
        }

        // 取消之前的预加载任务
        preloadJobs[position]?.cancel()

        // 创建新的预加载任务
        preloadJobs[position] = coroutineScope.launch(Dispatchers.Main) {
            try {
                if (!video.videoSrc.isNullOrEmpty()) {
                    // 优先预加载封面图和头像 - 在主线程执行
                    if (!preloadedImages.contains(position)) {
                        preloadImages(video, position)
                    }

                    // 稍微延迟后预加载视频，避免阻塞图片加载
                    delay(100)

                    // 创建播放器并准备媒体源进行预加载
                    val player = getOrCreatePlayer(position)

                    // 检查播放器是否已经有媒体源
                    if (player.mediaItemCount == 0) {
                        try {
                            val mediaItem = MediaItem.fromUri(video.videoSrc)
                            // 在IO线程构建媒体源
                            val mediaSource = withContext(Dispatchers.IO) {
                                buildMediaSource(mediaItem)
                            }
                            // 回到主线程设置媒体源
                            player.setMediaSource(mediaSource)
                            player.prepare()
                            // 不自动播放，只是准备
                            player.playWhenReady = false
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    // 标记为已预加载
                    preloadedPositions.add(position)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 独立的图片预加载方法
    private fun preloadImages(video: Video.Data, position: Int) {
        // 预加载封面图
        if (!video.coverSrc.isNullOrEmpty()) {
            Glide.with(context)
                .load(video.coverSrc)
                .preload()
        }

        // 预加载作者头像
        if (!video.authorAvatar.isNullOrEmpty()) {
            Glide.with(context)
                .load(video.authorAvatar)
                .preload()
        }

        // 标记图片已预加载
        preloadedImages.add(position)
    }

    // 批量预加载图片（用于初始化时）
    fun preloadImagesForRange(startPosition: Int, count: Int) {
        coroutineScope.launch {
            for (i in 0 until count) {
                val position = startPosition + i
                if (position < videos.size && !preloadedImages.contains(position)) {
                    preloadImages(videos[position], position)
                    // 每个图片预加载之间稍微间隔
                    delay(50)
                }
            }
        }
    }
    
    override fun getItemCount(): Int = videos.size
    
    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var isLongPress = false
        var isSeekBarTouch = false
        val playerView: StyledPlayerView = itemView.findViewById(R.id.videoView)
        val videoBackgroundImage: ImageView = itemView.findViewById(R.id.video_background_image)
        val videoPlayImage: ImageView = itemView.findViewById(R.id.video_play_image)
        val loveView: LoveView = itemView.findViewById(R.id.loveView)
        val authorNickname: TextView = itemView.findViewById(R.id.author_nickname)
        val videoTitle: TextView = itemView.findViewById(R.id.video_title)
        val tvLikeCount: TextView = itemView.findViewById(R.id.tv_like_count)
        val tvCommentCount: TextView = itemView.findViewById(R.id.tv_comment_count)
        val tvShareCount: TextView = itemView.findViewById(R.id.tv_share_count)
        val authorAvatar: ImageView = itemView.findViewById(R.id.author_avatar)
        val likeBtn: ImageView = itemView.findViewById(R.id.like_btn)
        val commentBtn: ImageView = itemView.findViewById(R.id.comment_btn)
        val seekBar: SeekBar = itemView.findViewById(R.id.video_seekBar)
        val seekBarParent: LinearLayout = itemView.findViewById(R.id.seekBar_parent)
        val timeTextView: TextView = itemView.findViewById(R.id.time_text_view)
        val authorInfoLayout: LinearLayout = itemView.findViewById(R.id.author_info_layout)
        val imageBanner: Banner<String, ImageBannerAdapter> = itemView.findViewById(R.id.imageBanner)
        val imageIndicatorLayout: LinearLayout = itemView.findViewById(R.id.imageIndicatorLayout)
        val bannerTouchOverlay: View = itemView.findViewById(R.id.bannerTouchOverlay)
    }

    private fun animateSeekBarHeight(seekBar: SeekBar, targetHeight: Int) {
        val startHeight = seekBar.layoutParams.height
        val animator = ValueAnimator.ofInt(startHeight, targetHeight)
        animator.duration = 200 // 动画持续200毫秒
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            seekBar.layoutParams.height = value
            seekBar.requestLayout()
        }
        animator.start()
    }
}