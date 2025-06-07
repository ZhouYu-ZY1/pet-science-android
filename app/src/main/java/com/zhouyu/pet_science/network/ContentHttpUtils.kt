package com.zhouyu.pet_science.network

import com.google.gson.Gson
import com.orhanobut.hawk.Hawk
import com.zhouyu.pet_science.model.Content
import com.zhouyu.pet_science.network.HttpUtils.BASE_URL
import com.zhouyu.pet_science.network.HttpUtils.client
import com.zhouyu.pet_science.pojo.Like
import com.zhouyu.pet_science.pojo.Video
import okhttp3.FormBody
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.util.Objects
import java.util.function.Predicate

object ContentHttpUtils {
    /**
     * 获取推荐视频
     */
    fun getRecommendVideo(): Video? {
        val request: Request = Request.Builder().url("$BASE_URL/content/getRecommendList")
            .build()
        try {
            val result = client.newCall(request).execute().body!!.string()
            val video: Video = Gson().fromJson(result, Video::class.java)
            // 假设 Video.Data 是一个类，video.getData() 返回一个 List<Video.Data>
            val dataList: MutableList<Video.Data> = video.data
            //移除空对象
            dataList.removeIf { obj: Video.Data? -> Objects.isNull(obj) }
            return video
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 喜欢/不喜欢视频
     */
    fun likeVideo(isLike: Boolean, video: Video.Data?): Boolean {
        val like = Like(video, isLike)
        val json = Gson().toJson(like)
        var url: String = BASE_URL
        url += if (isLike) {
            "/content/like"
        } else {
            "/content/dislike"
        }
        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), json)
        val request: Request = Request.Builder().url(url)
            .post(requestBody)
            .build()
        try {
            val result = client.newCall(request).execute().body!!.string()
            val jsonObject = JSONObject(result)
            val code = jsonObject.getInt("code")
            return code == 200
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return false
    }

    //获取用户视频列表
    fun getUserVideoList(userId: Int): Video? {
        val url = if (userId == -1) {
            "$BASE_URL/content/getUserVideoList?userId=0"
        } else {
            "$BASE_URL/content/getUserVideoList?userId=$userId"
        }
        val request: Request = Request.Builder().url(url)
            .get()
            .build()
        try {
            val result = client.newCall(request).execute().body!!.string()
            val video: Video = Gson().fromJson(result, Video::class.java)
            val dataList: MutableList<Video.Data> = video.data
            //移除空对象
            dataList.removeIf { obj: Video.Data? -> Objects.isNull(obj) }
            for (data in dataList) {
                data.videoSrc = BASE_URL + data.videoSrc
                data.coverSrc = BASE_URL + data.coverSrc
                data.authorAvatar = BASE_URL + data.authorAvatar
            }
            return video
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return null
    }

    //获取点赞视频列表
    fun getLikeList(userId: Int): Video? {
        val url = if (userId == -1) {
            "$BASE_URL/content/getLikeList?userId=0"
        } else {
            "$BASE_URL/content/getLikeList?userId=$userId"
        }
        val request: Request = Request.Builder().url(url)
            .get()
            .build()
        try {
            val result = client.newCall(request).execute().body!!.string()
            val video: Video = Gson().fromJson(result, Video::class.java)
            // 假设 Video.Data 是一个类，video.getData() 返回一个 List<Video.Data>
            val dataList: MutableList<Video.Data> = video.data
            //移除空对象
            dataList.removeIf { obj: Video.Data? -> Objects.isNull(obj) }
            for (data in dataList) {
                if (data.type.equals("1")) {
                    data.videoSrc = BASE_URL + data.videoSrc
                    data.coverSrc = BASE_URL + data.coverSrc
                }
            }
            return video
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return null
    }



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
