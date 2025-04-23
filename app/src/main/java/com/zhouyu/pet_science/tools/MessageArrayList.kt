package com.zhouyu.pet_science.tools

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zhouyu.pet_science.application.Application
import com.zhouyu.pet_science.fragments.MessageFragment.Companion.getMessageList
import com.zhouyu.pet_science.pojo.MessageListItem

class MessageArrayList<T> : ArrayList<T>() {
    companion object {
        // 从本地读取保存的List
        fun loadList(context: Context): List<MessageListItem>? {
            return emptyList()
        }
    }

    override fun add(element: T): Boolean {
        Application.executeThread {
            // 将消息List保存到本地
            saveList(Application.context)
        }
        return super.add(element)
    }

    // 保存List到本地
    private fun saveList(context: Context) {

    }
}
