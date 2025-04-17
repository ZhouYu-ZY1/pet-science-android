package com.zhouyu.pet_science.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.youth.banner.adapter.BannerAdapter
import com.zhouyu.pet_science.R

class BannerTextAdapter(
    data: List<BannerItem>
) : BannerAdapter<BannerTextAdapter.BannerItem, BannerTextAdapter.BannerTextHolder>(data) {

    data class BannerItem(
        val imageUrl: String,
        val title: String,
        val text: String,
    )

    class BannerTextHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.banner_image)
        val textView: TextView = view.findViewById(R.id.banner_text)
        val titleView: TextView = view.findViewById(R.id.banner_title)
    }

    override fun onCreateHolder(parent: ViewGroup, viewType: Int): BannerTextHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_banner_with_text, parent, false
        )
        return BannerTextHolder(view)
    }

    override fun onBindView(holder: BannerTextHolder, data: BannerItem, position: Int, size: Int) {
        // 加载图片
        Glide.with(holder.itemView)
            .load(data.imageUrl)
            .transition(DrawableTransitionOptions.withCrossFade())
            .centerCrop()
            .into(holder.imageView)
        
        // 设置文本
        holder.titleView.text = data.title
        holder.textView.text = data.text
    }
} 