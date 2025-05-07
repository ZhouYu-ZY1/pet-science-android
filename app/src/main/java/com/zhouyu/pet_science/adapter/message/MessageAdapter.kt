package com.zhouyu.pet_science.adapter.message

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.adapter.message.MessageAdapter.MessageViewHolder
import com.zhouyu.pet_science.fragments.PersonalCenterFragment
import com.zhouyu.pet_science.network.HttpUtils.BASE_URL
import com.zhouyu.pet_science.pojo.ChatMessage
import com.zhouyu.pet_science.utils.TimeUtils
import kotlin.math.acos

class MessageAdapter(private val messages: List<ChatMessage>, private val currentUserId: String) :
    RecyclerView.Adapter<MessageViewHolder>() {
    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var avatarImage: ImageView
        var nickname: TextView
        var messageContent: TextView
        var sendTime: TextView
        var messageContentLayout: LinearLayout

        init {
            avatarImage = itemView.findViewById(R.id.avatar_image)
            nickname = itemView.findViewById(R.id.nickname)
            messageContent = itemView.findViewById(R.id.message_content)
            sendTime = itemView.findViewById(R.id.sendTime)
            messageContentLayout = itemView.findViewById(R.id.message_content_layout)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(v)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val chatMessage = messages[position]
        holder.messageContent.text = chatMessage.content
        holder.nickname.text = chatMessage.senderName
        holder.sendTime.text = TimeUtils.getMessageTime(chatMessage.timestamp)
        val layoutParams = holder.avatarImage.layoutParams as RelativeLayout.LayoutParams
        if (currentUserId == chatMessage.senderId) {
            // 当前用户发送的消息，显示在右边
            layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_START)
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END)
            holder.messageContentLayout.gravity = Gravity.END
            holder.messageContent.setBackgroundResource(R.drawable.message_bg_blue_me)
            holder.messageContent.setTextColor(holder.itemView.context.getColor(R.color.white))
            val avatarUrl = PersonalCenterFragment.userInfo?.avatarUrl
            if(!avatarUrl.isNullOrEmpty()){
                Glide.with(holder.itemView.context).load(BASE_URL+ avatarUrl).into(holder.avatarImage)
            }
        } else {
            // 其他用户发送的消息，显示在左边
            layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_END)
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START)
            holder.messageContentLayout.gravity = Gravity.START
            holder.messageContent.setBackgroundResource(R.drawable.message_bg_white_other)
            holder.messageContent.setTextColor(holder.itemView.context.getColor(R.color.black))

            // 头像加载最后一条对方发送的消息
            val lastOtherMessage = messages.reversed().find { it.senderId != currentUserId }
            val otherAvatar = if(lastOtherMessage != null){ lastOtherMessage.senderAvatar }else{ "" }
            Glide.with(holder.itemView.context)
                .load(BASE_URL + otherAvatar)
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .into(holder.avatarImage)
        }
        holder.avatarImage.layoutParams = layoutParams
    }

    override fun getItemCount(): Int {
        return messages.size
    }
}