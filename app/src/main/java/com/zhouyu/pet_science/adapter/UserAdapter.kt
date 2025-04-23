package com.zhouyu.pet_science.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.databinding.ItemUserBinding
import com.zhouyu.pet_science.fragments.MessageFragment
import com.zhouyu.pet_science.model.User
import com.zhouyu.pet_science.network.HttpUtils.BASE_URL
import com.zhouyu.pet_science.network.UserHttpUtils.Companion.followUser

class UserAdapter(private val context: Context, private var users: List<User>) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    
    class UserViewHolder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(context), parent, false)
        return UserViewHolder(binding)
    }

    @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        val binding = holder.binding
        
        binding.nickname.text = user.nickname
        val intro: String = user.bio
        binding.desc.text = intro.ifEmpty { "暂无简介" }
        binding.petUsername.text = "萌宠号：${user.username}"

        // 优化图片加载
        Glide.with(context)
            .load(BASE_URL + user.avatarUrl)
            .apply(RequestOptions())
            .transform(CircleCrop())
            .into(binding.avatarImage)

        // 根据关注状态设置按钮文本和背景颜色
        updateFollowButton(binding, user.isFollowed)
        
        binding.buttonFollow.setOnClickListener {
            val followed = user.isFollowed
            // 立即更新UI，提高响应速度
            updateFollowButton(binding, !followed)
            user.isFollowed = !followed
            Thread {
                val succeed = followUser(user.userId, !followed)
                if (!succeed) {
                    holder.itemView.post {
                        // 如果失败，恢复原状态
                        user.isFollowed = followed
                        updateFollowButton(binding, followed)
                        Toast.makeText(
                            holder.itemView.context, 
                            if (followed) "取关失败" else "关注失败", 
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    MessageFragment.refreshFollowList = true
//                    PersonalFragment.refreshInfo = true
                }
            }.start()
        }
    }

    // 抽取更新按钮状态的方法，减少代码重复
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun updateFollowButton(binding: ItemUserBinding, isFollowed: Boolean) {
        if (isFollowed) {
            binding.buttonFollow.text = "取消关注"
            binding.buttonFollow.background = context.getDrawable(R.drawable.button_unfollow)
        } else {
            binding.buttonFollow.text = "关注"
            binding.buttonFollow.background = context.getDrawable(R.drawable.button_follow)
        }
    }

    override fun getItemCount(): Int = users.size

    // 添加更新数据的方法，避免每次都创建新适配器
    fun updateData(newUsers: List<User>) {
        this.users = newUsers
    }
}