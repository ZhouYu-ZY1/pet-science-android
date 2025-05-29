package com.zhouyu.pet_science.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.pojo.Video
import com.zhouyu.pet_science.utils.ConsoleUtils

class VideoAdapter(private val videoList: List<Video.Data>) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {
    
    private var currentPlayingPosition = -1
    private var currentPlayer: ExoPlayer? = null
    private val preloadedPlayers = HashMap<Int, ExoPlayer>()
    private var isPaused = false
    
    // 记录视频暂停状态
    private val pausedPositions = HashSet<Int>()
    
    // 保存每个视频的播放位置
    private val playbackPositions = HashMap<Int, Long>()

    @SuppressLint("NotifyDataSetChanged")
    fun setVideoList(newVideoList: List<Video.Data>) {
        (videoList as MutableList).clear()
        videoList.addAll(newVideoList)
        notifyDataSetChanged()
    }
    
    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playerView: StyledPlayerView = itemView.findViewById(R.id.videoView)
        val authorNickname: TextView = itemView.findViewById(R.id.author_nickname)
        val videoTitle: TextView = itemView.findViewById(R.id.video_title)
        val authorAvatar: ImageView = itemView.findViewById(R.id.author_avatar)
        val likeBtn: ImageView = itemView.findViewById(R.id.like_btn)
        val playIcon: ImageView = itemView.findViewById(R.id.video_play_image)
        
        var player: ExoPlayer? = null
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = videoList[position]
        ConsoleUtils.logErr("Binding video at position $position: ${video.videoSrc}")
        
        holder.authorNickname.text = "@${video.nickname}"
        holder.videoTitle.text = video.desc
        
        // 加载作者头像
        Glide.with(holder.itemView.context)
            .load(video.authorAvatar)
            .placeholder(R.drawable.default_avatar)
            .into(holder.authorAvatar)
            
        // 设置点赞按钮状态
        holder.likeBtn.isSelected = video.isLike
        
        // 设置双击点赞
        holder.itemView.setOnClickListener { view ->
            if (holder.player?.isPlaying == true) {
                holder.player?.pause()
                holder.playIcon.visibility = View.VISIBLE
                pausedPositions.add(position)
            } else {
                playVideo(position, holder)
                holder.playIcon.visibility = View.GONE
                pausedPositions.remove(position)
            }
        }
        
        // 设置点赞按钮点击事件
        holder.likeBtn.setOnClickListener {
            video.isLike = !video.isLike
            holder.likeBtn.isSelected = video.isLike
        }
        
        // 显示暂停图标状态
        if (pausedPositions.contains(position)) {
            holder.playIcon.visibility = View.VISIBLE
        } else {
            holder.playIcon.visibility = View.GONE
        }
        
        // 预加载视频
        preloadVideo(position, holder)
    }
    
    override fun getItemCount(): Int {
        return videoList.size
    }
    
    fun playVideo(position: Int, holder: VideoViewHolder) {
        // 如果当前正在播放的视频不是要播放的视频，保存当前播放位置并暂停
        if (currentPlayingPosition != -1 && currentPlayingPosition != position && currentPlayer != null) {
            // 保存当前播放位置
            playbackPositions[currentPlayingPosition] = currentPlayer?.currentPosition ?: 0
            
            // 暂停当前播放器但不释放资源
            currentPlayer?.pause()
            
            // 将当前播放器添加到预加载列表中而不是释放它
            if (currentPlayer != null) {
                preloadedPlayers[currentPlayingPosition] = currentPlayer!!
            }
        }
        
        // 如果是同一个位置，且播放器已经初始化，则直接播放
        if (currentPlayingPosition == position && holder.player != null) {
            holder.player?.play()
            return
        }
        
        // 检查是否有预加载的播放器
        val preloadedPlayer = preloadedPlayers[position]
        if (preloadedPlayer != null) {
            // 使用预加载的播放器
            holder.playerView.player = preloadedPlayer
            
            // 恢复之前的播放位置
            val savedPosition = playbackPositions[position] ?: 0
            if (savedPosition > 0) {
                preloadedPlayer.seekTo(savedPosition)
            }
            
            preloadedPlayer.play()
            
            // 更新当前播放位置和播放器
            currentPlayingPosition = position
            currentPlayer = preloadedPlayer
            holder.player = preloadedPlayer
            
            // 从预加载列表中移除，但不释放资源
            preloadedPlayers.remove(position)
        } else {
            // 创建新的播放器
            val context = holder.itemView.context
            val player = ExoPlayer.Builder(context).build()
            
            // 设置播放器参数
            player.repeatMode = Player.REPEAT_MODE_ONE
            player.playWhenReady = true
            
            // 设置播放源
            val mediaItem = MediaItem.fromUri(videoList[position].videoSrc)
            player.setMediaItem(mediaItem)
            
            // 准备播放器
            player.prepare()
            
            // 恢复之前的播放位置
            val savedPosition = playbackPositions[position] ?: 0
            if (savedPosition > 0) {
                player.seekTo(savedPosition)
            }
            
            // 设置播放器到PlayerView
            holder.playerView.player = player
            
            // 更新当前播放位置和播放器
            currentPlayingPosition = position
            currentPlayer = player
            holder.player = player
        }
    }
    
    private fun preloadVideo(currentPosition: Int, holder: VideoViewHolder) {
        // 预加载下一个视频
        val nextPosition = currentPosition + 1
        if (nextPosition < videoList.size && !preloadedPlayers.containsKey(nextPosition)) {
            val context = holder.itemView.context
            
            val player = ExoPlayer.Builder(context).build()
            player.repeatMode = Player.REPEAT_MODE_ONE
            player.playWhenReady = false
            
            val mediaItem = MediaItem.fromUri(videoList[nextPosition].videoSrc)
            player.setMediaItem(mediaItem)
            player.prepare()
            
            preloadedPlayers[nextPosition] = player
        }
    }
    
    fun releaseAllPlayers() {
        // 保存所有播放器的播放位置
        if (currentPlayer != null && currentPlayingPosition != -1) {
            playbackPositions[currentPlayingPosition] = currentPlayer?.currentPosition ?: 0
        }
        
        // 暂停当前播放器，但不释放资源（除非是在Fragment销毁时）
        currentPlayer?.pause()
        
        // 暂停所有预加载的播放器，但不释放资源
        for (player in preloadedPlayers.values) {
            player.pause()
        }
    }
    
    // 添加一个新方法，用于真正释放所有资源（仅在Fragment完全销毁时调用）
    fun destroyAllPlayers() {
        // 释放当前播放器
        currentPlayer?.release()
        currentPlayer = null
        
        // 释放所有预加载的播放器
        for (player in preloadedPlayers.values) {
            player.release()
        }
        preloadedPlayers.clear()
        
        currentPlayingPosition = -1
    }
    
    fun pauseCurrentPlayer() {
        if (currentPlayer != null && currentPlayer?.isPlaying == true) {
            // 保存当前播放位置
            if (currentPlayingPosition != -1) {
                playbackPositions[currentPlayingPosition] = currentPlayer?.currentPosition ?: 0
            }
            
            currentPlayer?.pause()
            isPaused = true
            if (currentPlayingPosition != -1) {
                pausedPositions.add(currentPlayingPosition)
            }
        }
    }
    
    fun resumeCurrentPlayer() {
        if (currentPlayer != null && isPaused) {
            if (!pausedPositions.contains(currentPlayingPosition)) {
                currentPlayer?.play()
            }
            isPaused = false
        }
    }
    
    // 获取保存的播放位置
    fun getSavedPlaybackPosition(position: Int): Long {
        return playbackPositions[position] ?: 0
    }
}