package com.zhouyu.pet_science.tools

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zhouyu.pet_science.application.Application
import com.zhouyu.pet_science.fragments.MessageFragment.Companion.getMessageList
import com.zhouyu.pet_science.pojo.MessageListItem

class MessageArrayList<T> : ArrayList<T>() {
    companion object {
        private const val PREFS_NAME = "message_prefs"
        private const val KEY_MESSAGES = "messages"

        // 从本地读取保存的List
        fun loadList(context: Context): List<MessageListItem>? {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val json = prefs.getString(KEY_MESSAGES, null)
            return if (json != null) {
                val type = object : TypeToken<List<MessageListItem>>() {}.type
                Gson().fromJson(json, type)
            } else {
                null
            }
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
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val json = Gson().toJson(getMessageList())
        editor.putString(KEY_MESSAGES, json)
        editor.apply()
    }
}
