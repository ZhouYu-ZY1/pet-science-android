package com.zhouyu.pet_science.adapter

import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.youth.banner.adapter.BannerAdapter

/**
 * 图文轮播适配器
 */
class ImageBannerAdapter(private val imageUrls: List<String>) : 
    BannerAdapter<String, ImageBannerAdapter.ImageViewHolder>(imageUrls) {

    override fun onCreateHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val imageView = ImageView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        return ImageViewHolder(imageView)
    }

    override fun onBindView(holder: ImageViewHolder, data: String, position: Int, size: Int) {
        // 使用Glide加载图片
        Glide.with(holder.imageView.context)
            .load(data)
            .transform(CenterCrop())
            .into(holder.imageView)
    }

    class ImageViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)
}
