package com.zhouyu.pet_science.network

import com.zhouyu.pet_science.model.Content
import com.zhouyu.pet_science.network.HttpUtils.BASE_URL
import com.zhouyu.pet_science.network.HttpUtils.client
import org.json.JSONObject
import okhttp3.Request

object ContentHttpUtils {
    /**
     * 获取当前用户的发布内容
     */
    fun getUserPosts(): List<Content> {
        return getUserPostsById(-1)
    }

    /**
     * 获取当前用户的点赞内容
     */
    fun getUserLikes(): List<Content> {
        return getUserLikesById(-1)
    }
    
    /**
     * 获取指定用户的发布内容
     * @param userId 用户ID，-1表示获取当前登录用户的内容
     */
    fun getUserPostsById(userId: Int): List<Content> {
        try {
            val url = if (userId == -1) {
                "$BASE_URL/content/user/posts"
            } else {
                "$BASE_URL/content/user/posts?userId=$userId"
            }
            
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
                
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            
            if (response.code == 200 && responseBody != null) {
                val jsonObject = JSONObject(responseBody)
                val dataArray = jsonObject.getJSONArray("data")
                val contentList = mutableListOf<Content>()
                
                for (i in 0 until dataArray.length()) {
                    val contentObj = dataArray.getJSONObject(i)
                    contentList.add(formatContent(contentObj))
                }
                return contentList
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }
    
    /**
     * 获取指定用户的点赞内容
     * @param userId 用户ID，-1表示获取当前登录用户的内容
     */
    fun getUserLikesById(userId: Int): List<Content> {
        try {
            val url = if (userId == -1) {
                "$BASE_URL/content/user/likes"
            } else {
                "$BASE_URL/content/user/likes?userId=$userId"
            }
            
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
                
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            
            if (response.code == 200 && responseBody != null) {
                val jsonObject = JSONObject(responseBody)
                val dataArray = jsonObject.getJSONArray("data")
                val contentList = mutableListOf<Content>()
                
                for (i in 0 until dataArray.length()) {
                    val contentObj = dataArray.getJSONObject(i)
                    contentList.add(formatContent(contentObj))
                }
                return contentList
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return emptyList()
    }
    
    /**
     * 格式化内容对象
     */
    private fun formatContent(contentObj: JSONObject): Content {
        return Content(1,"","",100,"",1,false)
    }
}
