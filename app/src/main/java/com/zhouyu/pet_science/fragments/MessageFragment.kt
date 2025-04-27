package com.zhouyu.pet_science.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.activities.AIChatActivity
import com.zhouyu.pet_science.activities.SearchActivity
import com.zhouyu.pet_science.adapter.message.MessageListAdapter
import com.zhouyu.pet_science.databinding.FragmentMessageBinding
import com.zhouyu.pet_science.model.User
import com.zhouyu.pet_science.network.UserHttpUtils
import com.zhouyu.pet_science.pojo.MessageListItem
import com.zhouyu.pet_science.utils.StorageUtils
import com.zhouyu.pet_science.utils.TimeUtils

class MessageFragment : BaseFragment() {
    private var binding: FragmentMessageBinding? = null
    private var messageListAdapter: MessageListAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessageBinding.inflate(inflater, container, false)
        setTopBarView(binding!!.messageFragment, true)
        initViews()
        loadMessages()
        refreshList()
        return binding!!.root
    }

    @SuppressLint("SetTextI18n")
    private fun initViews() {
        binding?.apply {
            loadAIMessage()
            messageListRecyclerView.layoutManager = LinearLayoutManager(context)
            messageListAdapter = MessageListAdapter(requireContext(), getMessageList()!!)
            messageListRecyclerView.adapter = messageListAdapter

            userSearchBtn.setOnClickListener {
                val intent = Intent(context, SearchActivity::class.java)
                intent.putExtra("type", "user")
                startActivity(intent)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    @SuppressLint("SetTextI18n")
    private fun loadAIMessage(){
        executeThread{
            var aiChat: MessageListItem?
            var messageTime = ""
            try {
                aiChat = StorageUtils.get<MessageListItem>("ai_last_message")
                messageTime = TimeUtils.getMessageTime(aiChat.lastTime.toLong())
            }catch (e: Exception){
                aiChat = null
            }
            runUiThread{
                binding?.aiItem!!.apply{
                    avatarImage.setImageResource(R.drawable.ai_icon)
                    usernameText.text = "萌宠AI助手"
                    lastMessageText.text = "您的专属宠物AI助手"
                    onlineIndicator.visibility = View.GONE
                    unreadCountText.visibility = View.INVISIBLE
                    timeText.visibility = View.INVISIBLE
                    root.setOnClickListener{
                        Intent(context, AIChatActivity::class.java).apply {
                            startActivity(this)
                        }
                    }

                    if (aiChat != null) {
                        lastMessageText.text = aiChat.lastMessage
                        timeText.text = messageTime
                        timeText.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private var isLoad = false
    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    private fun loadMessages() {
        if (isLoad) {
            return
        }
        isLoad = true
        executeThread {
            val token = StorageUtils.get<String>("token")
            val followList: List<User> = UserHttpUtils.getFollowList()
            for (user in followList) {
                var isExist = false
                val id: String = java.lang.String.valueOf(user.userId).trim { it <= ' ' }
                for (messageListItem in getMessageList()!!) {
                    if (messageListItem.userId.trim() == id) {
                        isExist = true
                        break
                    }
                }
                if (!isExist) {
                    val messageListItem: MessageListItem =
                        MessageListItem.userToMessageItem(user, token)
                    (getMessageList() as MutableList).add(messageListItem)
                }
            }
            requireActivity().runOnUiThread { messageListAdapter?.notifyDataSetChanged() }
            isLoad = false
        }
    }

    private fun refreshList() {
        binding?.root?.post(object : Runnable {
            @SuppressLint("NotifyDataSetChanged")
            override fun run() {
                if (refreshList) {
                    refreshList = false
                    loadAIMessage()
                    messageListAdapter?.notifyDataSetChanged()
                }
                if (refreshFollowList) {
                    refreshFollowList = false
                    loadMessages()
                }
                binding?.root?.postDelayed(this, 500)
            }
        })
    }

    override fun onStart() {
        super.onStart()
        refreshList = true
    }

    companion object {
        var refreshList = false
        var refreshFollowList = false
        private var messageList: MutableList<MessageListItem>? = null
        fun getMessageList(): List<MessageListItem>? {
            if (messageList == null) {
                messageList = ArrayList()
            }
            return messageList
        }

        fun setMessageList(messageList: MutableList<MessageListItem>?) {
            Companion.messageList = messageList
        }
    }
}