package com.zhouyu.pet_science.pojo;


import com.zhouyu.pet_science.model.User;
import com.zhouyu.pet_science.tools.MessageArrayList;
import com.zhouyu.pet_science.tools.TimeUtils;

public class MessageListItem {
    private String userId;
    private String username;
    private String avatar;
    private String lastMessage;
    private String lastTime;
    private boolean online;
    private int unreadCount;

    private MessageArrayList<ChatMessage> chatMessageList;

    public MessageListItem() {
//        this.userId = userId;
//        this.username = username;
//        this.avatar = avatar;
//        this.lastMessage = lastMessage;
//        this.lastTime = lastTime;
//        this.online = online;
//        this.unreadCount = unreadCount;
    }

    public static MessageListItem userToMessageItem(User user, String myId){
        MessageListItem message = new MessageListItem();
        long followTime = user.getFollowTime() - 1000L;
        message.setUserId(String.valueOf(user.getUserId()));
        message.setAvatar(user.getAvatarUrl());
        message.setLastMessage("感谢您的关注！");
        message.setLastTime(TimeUtils.getMessageTime(followTime));
        message.setOnline(true);
        message.setUnreadCount(1);
        message.setUsername(user.getNickname());
        message.chatMessageList = new MessageArrayList<>();
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent("感谢您的关注！");
        chatMessage.setMessageType(1);
        chatMessage.setSenderAvatar(user.getAvatarUrl());
        chatMessage.setSenderId(String.valueOf(user.getUserId()));
        chatMessage.setSenderName(user.getNickname());
        chatMessage.setReceiverId(myId);
        chatMessage.setTimestamp(followTime);
        message.chatMessageList.add(chatMessage);
        return message;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public String getLastTime() { return lastTime; }
    public void setLastTime(String lastTime) { this.lastTime = lastTime; }

    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }

    public MessageArrayList<ChatMessage> getChatMessageList() {
        if(chatMessageList == null){
            chatMessageList = new MessageArrayList<>();
        }
        return chatMessageList;
    }

    public void setChatMessageList(MessageArrayList<ChatMessage> chatMessageList) {
        this.chatMessageList = chatMessageList;
    }
}
