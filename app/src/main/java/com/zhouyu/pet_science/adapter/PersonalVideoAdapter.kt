package com.zhouyu.pet_science.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.activities.VideoPlayActivity
import com.zhouyu.pet_science.pojo.Video
import com.zhouyu.pet_science.utils.ConsoleUtils

class PersonalVideoAdapter(private val context: Context) :
    RecyclerView.Adapter<PersonalVideoAdapter.VideoViewHolder>() {
    private var videos: List<Video.Data> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_video_grid, parent, false)
        return VideoViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: VideoViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        val video = videos[position]
        //加载预览图片
        Glide.with(context).load(video.coverSrc).into(holder.imageView)
        //加载标题
        holder.textView.text = "@" + video.nickname
        holder.itemView.setOnClickListener {
            VideoPlayActivity.videoList = videos
            VideoPlayActivity.position = position
            val intent = Intent(context, VideoPlayActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return videos.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setVideos(videos: List<Video.Data>) {
        this.videos = videos
        notifyDataSetChanged()
    }

    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView
        var textView: TextView

        init {
            imageView = itemView.findViewById(R.id.video_thumbnail)
            textView = itemView.findViewById(R.id.video_title)
        }
    }
}
