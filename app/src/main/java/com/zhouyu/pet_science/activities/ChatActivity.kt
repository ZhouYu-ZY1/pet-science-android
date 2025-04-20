package com.zhouyu.pet_science.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.activities.base.BaseActivity
import com.zhouyu.pet_science.adapter.MessageAdapter
import com.zhouyu.pet_science.application.WebSocketManager
import com.zhouyu.pet_science.fragments.MessageFragment
import com.zhouyu.pet_science.pojo.ChatMessage
import com.zhouyu.pet_science.pojo.MessageListItem
import com.zhouyu.pet_science.tools.MessageArrayList
import com.zhouyu.pet_science.tools.MyToast
import com.zhouyu.pet_science.tools.StorageTool
import com.zhouyu.pet_science.tools.TimeUtils
import kotlin.math.max

class ChatActivity : BaseActivity(), WebSocketManager.MessageCallback {
    private var recyclerViewChat: RecyclerView? = null
    private var editTextMessage: EditText? = null
    private var buttonSend: Button? = null
    private var messageAdapter: MessageAdapter? = null
    private var chatMessages: MessageArrayList<ChatMessage>? = null
    private var currentUserId: String? = null
    private var currentUserName: String? = null
    private var targetUserId: String? = null
    private var targetUserName: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // 获取传递的用户信息
        targetUserId = intent.getStringExtra("userId")
        targetUserName = intent.getStringExtra("username")
        currentUserId = StorageTool.get("token")
        if(currentUserId == null){
            MyToast.show("登录异常，请重新登录")
            finish()
            return
        }
        currentUserName = "我" // 可以从用户配置或登录信息中获取
        chatMessages = messageListItem!!.getChatMessageList()
        messageListItem!!.unreadCount = 0 //重置未读数量

        // 设置标题
        if (supportActionBar != null) {
            supportActionBar!!.title = targetUserName
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        initViews()
        setupWebSocket()
        loadHistoryMessages()
    }

    private fun initViews() {
        val main = findViewById<View>(R.id.main)
        setTopBarView(findViewById(R.id.toolbar),true)

        // 自动适应软键盘，软键盘不遮挡输入框
        ViewCompat.setOnApplyWindowInsetsListener(main) { v, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.updatePadding(bottom = imeInsets.bottom)
            scrollToBottom(false)
            insets
        }

        recyclerViewChat = findViewById(R.id.recyclerViewChat)
        editTextMessage = findViewById(R.id.editTextMessage)
        buttonSend = findViewById(R.id.buttonSend)
        buttonSend?.setOnClickListener { sendMessage() }

        findViewById<TextView>(R.id.textTitle).text = targetUserName
        findViewById<View>(R.id.buttonBack).setOnClickListener{
            finish()
        }
    }

    private fun setupWebSocket() {
        WebSocketManager.instance.setMessageCallback(this)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadHistoryMessages() {
        messageAdapter = MessageAdapter(chatMessages, currentUserId)
        val layoutManager = LinearLayoutManager(this)
        recyclerViewChat?.setLayoutManager(layoutManager)
        recyclerViewChat?.setAdapter(messageAdapter)
        messageAdapter?.notifyDataSetChanged()
        scrollToBottom(false)
    }

    private fun sendMessage() {
        //重新连接服务器
        if (!WebSocketManager.isConnectWebSocket) {
            WebSocketManager.instance.connect()
        }
        val token = StorageTool.get<String>("token")
        val messageText = editTextMessage!!.text.toString().trim { it <= ' ' }
        if (messageText.isNotEmpty()) {
            // 创建消息对象
            val chatMessage = ChatMessage(
                token, currentUserName,
                targetUserId, messageText, 1
            )

            // 发送到WebSocket服务器
            val jsonMessage = Gson().toJson(chatMessage)
            WebSocketManager.instance.sendMessage(jsonMessage)

            // 添加到本地消息列表
            chatMessages!!.add(chatMessage)
            messageAdapter!!.notifyItemInserted(chatMessages!!.size - 1)
            scrollToBottom(true)
            messageListItem!!.lastMessage = messageText
            messageListItem!!.lastTime =
                TimeUtils.getMessageTime(System.currentTimeMillis())

            // 清空输入框
            editTextMessage!!.setText("")
        }
    }

    private fun scrollToBottom(anim: Boolean) {
        if(anim){
            recyclerViewChat!!.smoothScrollToPosition(
                max(
                    0,
                    chatMessages!!.size - 1
                )
            )
        }else{
            recyclerViewChat!!.scrollToPosition(
                max(
                    0,
                    chatMessages!!.size - 1
                )
            )
        }
    }

    override fun onMessage(message: String?) {
        if (!isFinishing) {
            runOnUiThread {
                //收到消息更新
                messageListItem!!.unreadCount = 0
                messageAdapter!!.notifyItemInserted(chatMessages!!.size - 1)
                scrollToBottom(true)
            }
        }
    }

    override fun onConnected() {}
    override fun onDisconnected() {}
    override fun onError(error: String?) {}
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun handleMessage(message: String) {
//        ChatMessage chatMessage = WebSocketManager.getInstance("ws://localhost:8181/chat").parseMessage(message);
//        chatMessages.add(chatMessage);
//        messageAdapter.notifyItemInserted(chatMessages.size() - 1);
//        recyclerViewChat.smoothScrollToPosition(chatMessages.size() - 1);
    }

    override fun onDestroy() {
        chatMessages = MessageArrayList()
        super.onDestroy()
    }

    override fun finish() {
        WebSocketManager.instance.removeMessageCallback(this)
        MessageFragment.refreshList = true
        super.finish()
    }

    companion object {
        var messageListItem: MessageListItem? = null
    }
}