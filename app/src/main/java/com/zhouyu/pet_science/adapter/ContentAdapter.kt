package com.zhouyu.pet_science.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zhouyu.pet_science.databinding.ItemContentGridBinding
import com.zhouyu.pet_science.model.Content
import com.zhouyu.pet_science.network.HttpUtils.BASE_URL

class ContentAdapter(private val context: Context, private val contents: List<Content>) :
    RecyclerView.Adapter<ContentAdapter.ContentViewHolder>() {

    class ContentViewHolder(val binding: ItemContentGridBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentViewHolder {
        val binding = ItemContentGridBinding.inflate(LayoutInflater.from(context), parent, false)
        return ContentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContentViewHolder, position: Int) {
        val content = contents[position]
        val binding = holder.binding

        // 设置内容图片
        Glide.with(context)
            .load(BASE_URL + content.coverUrl)
            .into(binding.ivContent)

        // 设置点赞和评论数
        binding.tvLikeCount.text = content.likeCount.toString()
        binding.tvCommentCount.text = content.commentCount.toString()

        // 设置点击事件
        holder.itemView.setOnClickListener {
            Toast.makeText(context, "查看详细内容", Toast.LENGTH_SHORT).show()
            // 在实际应用中，这里应该跳转到内容详情页
        }
    }

    override fun getItemCount(): Int = contents.size
}