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
import com.zhouyu.pet_science.activities.ChatActivity
import com.zhouyu.pet_science.network.HttpUtils.BASE_URL
import com.zhouyu.pet_science.pojo.MessageListItem

class MessageListAdapter(
    private val context: Context,
    private val messageList: List<MessageListItem>
) : RecyclerView.Adapter<MessageListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_message_list, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = messageList[position]
        holder.usernameText.text = item.username
        holder.lastMessageText.text = item.lastMessage
        holder.timeText.text = item.lastTime

        // 设置在线状态
        holder.onlineIndicator.visibility = if (item.isOnline) View.VISIBLE else View.GONE
        val unreadCount = item.unreadCount
        // 设置未读消息数
        if (unreadCount > 0) {
            holder.unreadCountText.visibility = View.VISIBLE
            if (unreadCount > 99) {
                holder.unreadCountText.text = "99+"
            } else {
                holder.unreadCountText.text = unreadCount.toString()
            }
        } else {
            holder.unreadCountText.visibility = View.INVISIBLE
        }
        holder.itemView.setOnClickListener {
            //跳转到聊天界面
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("userId", item.userId)
            intent.putExtra("username", item.username)
            intent.putExtra("avatar", item.avatar)
            ChatActivity.messageListItem = item
            context.startActivity(intent)
        }
        Glide.with(context).load(BASE_URL + item.avatar)
            .placeholder(R.drawable.default_user_icon)
            .error(R.drawable.default_user_icon)
            .into(holder.avatarImage)
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var avatarImage: ImageView
        var usernameText: TextView
        var lastMessageText: TextView
        var timeText: TextView
        var onlineIndicator: View
        var unreadCountText: TextView

        init {
            avatarImage = itemView.findViewById(R.id.avatarImage)
            usernameText = itemView.findViewById(R.id.usernameText)
            lastMessageText = itemView.findViewById(R.id.lastMessageText)
            timeText = itemView.findViewById(R.id.timeText)
            onlineIndicator = itemView.findViewById(R.id.onlineIndicator)
            unreadCountText = itemView.findViewById(R.id.unreadCountText)
        }
    }
}