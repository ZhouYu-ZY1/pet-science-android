package com.zhouyu.pet_science.pojo;

public class ChatMessage {
    private String messageId;
    private String senderId;
    private String receiverId;
    private String content;
    private long timestamp;
    private int messageType; // 0: 系统消息, 1: 文本消息, 2: 图片消息, 3: 语音消息
    private String senderName; // 发送者名称
    private String senderAvatar; // 发送者头像
    private boolean isRead; // 消息是否已读

    public ChatMessage() {
        this.timestamp = System.currentTimeMillis();
        this.messageType = 1; // 默认为文本消息
        this.isRead = false;
    }

    public ChatMessage(String senderId, String receiverId, String content) {
        this();
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
    }

    public ChatMessage(String senderId, String senderName, String receiverId, String content, int messageType) {
        this(senderId, receiverId, content);
        this.senderName = senderName;
        this.messageType = messageType;
    }

    // 新增的getter和setter方法


    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderAvatar() {
        return senderAvatar;
    }

    public void setSenderAvatar(String senderAvatar) {
        this.senderAvatar = senderAvatar;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "messageId='" + messageId + '\'' +
                ", senderId='" + senderId + '\'' +
                ", senderName='" + senderName + '\'' +
                ", receiverId='" + receiverId + '\'' +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                ", messageType=" + messageType +
                ", isRead=" + isRead +
                '}';
    }
}