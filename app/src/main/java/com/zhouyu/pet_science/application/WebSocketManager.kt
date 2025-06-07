package com.zhouyu.pet_science.application

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.zhouyu.pet_science.fragments.MessageFragment
import com.zhouyu.pet_science.network.HttpUtils.BASE_URL
import com.zhouyu.pet_science.network.HttpUtils.URL
import com.zhouyu.pet_science.pojo.ChatMessage
import com.zhouyu.pet_science.pojo.MessageListItem
import com.zhouyu.pet_science.utils.MessageArrayList
import com.zhouyu.pet_science.utils.NotificationHelper
import com.zhouyu.pet_science.utils.StorageUtils
import com.zhouyu.pet_science.utils.TimeUtils
import com.zhouyu.pet_science.utils.ConsoleUtils
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class WebSocketManager private constructor() {
    private var webSocket: WebSocket? = null
    private val wsUrl = "wss://$URL/message" // WebSocket服务器地址
    private val messageCallbacks: MutableList<MessageCallback> = ArrayList()

    interface MessageCallback {
        fun onMessage(message: String?)
        fun onConnected()
        fun onDisconnected()
        fun onError(error: String?)
    }
    fun setMessageCallback(callback: MessageCallback) {
        messageCallbacks.add(callback)
    }
    fun removeMessageCallback(callback: MessageCallback) {
        messageCallbacks.remove(callback)
    }

    fun connect() {
        val client: OkHttpClient = OkHttpClient.Builder()
            .pingInterval(30, TimeUnit.SECONDS)
            .build()
        val token = StorageUtils.get<String>("token")
        val request: Request = Request.Builder()
            .url("$wsUrl?token=$token")
            .build()
        ConsoleUtils.logErr("WebSocket连接中...$wsUrl")
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                ConsoleUtils.logErr("WebSocket连接成功")
                isConnectWebSocket = true
                for (messageCallback in messageCallbacks) {
                    messageCallback.onConnected()
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
//                Log.d(TAG, "收到消息: " + text);
                handleMessage(text)
                for (messageCallback in messageCallbacks) {
                    messageCallback.onMessage(text)
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                isConnectWebSocket = false
                for (messageCallback in messageCallbacks) {
                    messageCallback.onDisconnected()
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket连接失败", t)
                isConnectWebSocket = false
                for (messageCallback in messageCallbacks) {
                    messageCallback.onError(t.message)
                }
            }
        })
    }

    fun sendMessage(message: String?) {
        if (webSocket != null) {
            webSocket!!.send(message!!)
        }
    }

    fun disconnect() {
        if (webSocket != null) {
            webSocket!!.close(1000, "正常关闭")
        }
    }

    private fun handleMessage(message: String) {
//        ConsoleUtils.logErr(message);
        // 解析消息并更新UI
//        ChatMessage chatMessage = parseMessage(message);
//        // 根据消息类型进行处理
//        switch (chatMessage.getMessageType()) {
//            case 1: // JOIN
//                // 处理用户加入聊天室
//                break;
//            case 2: // CHAT
//                // 更新UI以显示新消息
//                break;
//            case 3: // PRIVATE
////                if(chatMessage.getReceiverId().equals(userId)) {
////                    // 显示私聊消息
////                }
//                break;
//        }
    }

    fun parseMessage(jsonMessage: String?): ChatMessage {
        // 解析JSON字符串为ChatMessage对象
        val gson = Gson()
        return gson.fromJson(jsonMessage, ChatMessage::class.java)
    }

    companion object {
        private const val TAG = "WebSocketManager"
        @JvmStatic
        val instance: WebSocketManager by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            WebSocketManager()
        }
        var isConnectWebSocket = false

        private var messageCallback: MessageCallback? = null

        fun closeWebSocket() {
            ConsoleUtils.logErr("WebSocket关闭中...")
            if(messageCallback != null) {
                instance.removeMessageCallback(messageCallback!!)
            }
            instance.disconnect()
        }
        fun connectWebSocket(context: Context) {
            val token = StorageUtils.get<String>("token")
            messageCallback = object : MessageCallback {
                override fun onMessage(message: String?) {
                    try {
                        message?.let {
                            val jsonObject = JSONObject(message)
                            val senderId = jsonObject.getString("senderId")
                            val userInfo = jsonObject.getJSONObject("userInfo")
                            val nickname = userInfo.getString("nickname")
                            val content = jsonObject.getString("content")
                            val avatar = userInfo.getString("avatarUrl")
                            val messageList = MessageFragment.getMessageList()
                            var sendMessageListItem: MessageListItem? = null
                            var isExist = false
                            //遍历查找聊天记录，如已存在则直接在聊天记录添加新消息
                            for (messageListItem in messageList!!) {
                                val userId = messageListItem.userId
                                if (userId.trim { it <= ' ' } == senderId.trim { it <= ' ' }) {
                                    //如果存在，则更新消息
                                    sendMessageListItem = messageListItem
                                    messageListItem.lastMessage = content //设置未读信息
                                    messageListItem.lastTime =
                                        TimeUtils.getMessageTime(System.currentTimeMillis())
                                    messageListItem.unreadCount += 1 //未读加1
                                    //添加新消息到消息列表中
                                    val chatMessageList: MutableList<ChatMessage> =
                                        messageListItem.getChatMessageList()
                                    val chatMessage = ChatMessage(
                                        senderId, nickname,
                                        token, content, 1
                                    )
                                    chatMessage.senderAvatar = avatar
                                    chatMessageList.add(chatMessage) //头像
                                    isExist = true
                                    break
                                }
                            }

                            //如不存在聊天记录，则新建聊天记录，再添加到聊天中
                            if (!isExist) {
                                val currentTimeMillis = System.currentTimeMillis()
                                val id = userInfo.getInt("userId")
                                val messageListItem = MessageListItem()
                                messageListItem.userId = id.toString()
                                messageListItem.avatar = avatar
                                messageListItem.lastMessage = content
                                messageListItem.lastTime = TimeUtils.getMessageTime(currentTimeMillis)
                                messageListItem.isOnline = true
                                messageListItem.unreadCount = 1
                                messageListItem.username = nickname
                                val chatMessages: MessageArrayList<ChatMessage> = MessageArrayList()
                                val chatMessage = ChatMessage()
                                chatMessage.content = content
                                chatMessage.messageType = 1
                                chatMessage.senderAvatar = avatar
                                chatMessage.senderId = id.toString()
                                chatMessage.senderName = nickname
                                chatMessage.receiverId = token
                                chatMessage.timestamp = currentTimeMillis
                                chatMessages.add(chatMessage)
                                messageListItem.chatMessageList = chatMessages
                                (messageList as MutableList<MessageListItem>).add(messageListItem)
                                sendMessageListItem = messageListItem
                            }

                            //发送通知
                            val allowNotification: Boolean =
                                NotificationHelper.isOpenNotification(context)
                            if (allowNotification) {
                                ConsoleUtils.logErr("收到消息：$message")
                                val avatarBitmap: Bitmap = Glide.with(context).asBitmap()
                                    .load(BASE_URL + sendMessageListItem!!.avatar)
                                    .submit(500, 500).get()
                                val time = TimeUtils.getNotificationTime(System.currentTimeMillis())
                                NotificationHelper.createMessageNotification(
                                    context,
                                    nickname,
                                    content,
                                    time,
                                    avatarBitmap,
                                    sendMessageListItem
                                )
                            }
                            MessageFragment.refreshList = true
                        }
                        //                runOnUiThread(() -> MyToast.show("收到消息：" + message));
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onConnected() {}
                override fun onDisconnected() {}

                override fun onError(error: String?) {
                    ConsoleUtils.logErr("错误$error")
                    //重新连接服务器
                    if (!isConnectWebSocket) {
                        // 每隔1秒后重新连接WebSocket
                        Application.mainHandler.postDelayed({ instance.connect() }, 1000)
                    }
                }
            }
            instance.setMessageCallback(messageCallback as MessageCallback)
            instance.connect()
        }
    }
}
