package com.zhouyu.pet_science.adapter;

import static com.zhouyu.pet_science.network.HttpUtils.BASE_URL;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.zhouyu.pet_science.R;
import com.zhouyu.pet_science.pojo.ChatMessage;
import com.zhouyu.pet_science.tools.TimeUtils;

import java.util.List;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<ChatMessage> messages;
    private String currentUserId;

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public ImageView avatarImage;
        public TextView nickname;
        public TextView message_content;
        public TextView sendTime;
        public LinearLayout message_content_layout;

        public MessageViewHolder(View itemView) {
            super(itemView);
            avatarImage = itemView.findViewById(R.id.avatar_image);
            nickname = itemView.findViewById(R.id.nickname);
            message_content = itemView.findViewById(R.id.message_content);
            sendTime = itemView.findViewById(R.id.sendTime);

            message_content_layout = itemView.findViewById(R.id.message_content_layout);
        }
    }


    public MessageAdapter(List<ChatMessage> messages,String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        ChatMessage chatMessage = messages.get(position);
        holder.message_content.setText(chatMessage.getContent());
        holder.nickname.setText(chatMessage.getSenderName());
        holder.sendTime.setText(TimeUtils.getMessageTime(chatMessage.getTimestamp()));

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) holder.avatarImage.getLayoutParams();
        if (currentUserId.equals(chatMessage.getSenderId())) {
            // 当前用户发送的消息，显示在右边
            layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_START);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            holder.message_content_layout.setGravity(Gravity.END);
            holder.message_content.setBackgroundResource(R.drawable.message_bg_blue_me);
            holder.message_content.setTextColor(holder.itemView.getContext().getColor(R.color.white));
//            String avatarUrl = InformationChangeActivity.loginInformation.getAvatarUrl();
//            Glide.with(holder.itemView.getContext()).load(avatarUrl).into(holder.avatarImage);
        } else {
            // 其他用户发送的消息，显示在左边
            layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_END);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            holder.message_content_layout.setGravity(Gravity.START);
            holder.message_content.setBackgroundResource(R.drawable.message_bg_white_other);
            holder.message_content.setTextColor(holder.itemView.getContext().getColor(R.color.black));
            Glide.with(holder.itemView.getContext())
                    .load(BASE_URL +chatMessage.getSenderAvatar())
                    .placeholder(R.drawable.default_user_icon)
                    .error(R.drawable.default_user_icon)
                    .into(holder.avatarImage);
        }
        holder.avatarImage.setLayoutParams(layoutParams);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
} 