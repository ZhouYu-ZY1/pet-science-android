package com.zhouyu.pet_science.network

import com.orhanobut.hawk.Hawk
import com.zhouyu.pet_science.model.User
import com.zhouyu.pet_science.network.HttpUtils.BASE_URL
import com.zhouyu.pet_science.network.HttpUtils.client
import com.zhouyu.pet_science.utils.ConsoleUtils
import okhttp3.FormBody
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.util.Date
import java.util.Objects

object UserHttpUtils {
    /**
     * 发送验证码
     * @param email 邮箱地址
     * @return Pair<Boolean, String?> 第一个参数表示是否成功，第二个参数为错误信息
     */
    fun sendVerificationCode(email: String): Pair<Boolean, String?> {
        return try {
            val jsonString = """{"email": "$email"}""".trimIndent()

            val request = Request.Builder()
                .url("$BASE_URL/user/sendVerificationCode")
                .post(jsonString.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            if (response.code == 200 && responseBody != null) {
                Pair(true, null)
            } else {
                val jsonObject = JSONObject(responseBody!!)
                Pair(false, jsonObject.getString("message"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(false, "网络请求失败")
        }
    }

    /**
     * 验证验证码
     */
    fun verifyVerificationCode(email: String, code: String): Pair<Boolean, JSONObject> {
        return try {
            val jsonString = """{"email":"$email","code":"$code"}""".trimIndent()
            val request = Request.Builder()
                .url("$BASE_URL/user/loginByCode")
                .post(jsonString.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            if (response.code == 200 && responseBody != null) {
                Pair(true, JSONObject(responseBody).getJSONObject("data"))
            } else {
                Pair(false,JSONObject())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(false, JSONObject())
        }
    }

    /**
     * 更新用户信息
     */
    fun updateUserInfo(
        avatarUrl: String?,
        nickname: String,
        gender: String,
        birthday: String,
        location: String,
        bio: String,
    ): Pair<Boolean, String?> {
        return try {
            val jsonString = JSONObject().apply {
                put("nickname", nickname)
                put("gender", gender)
                put("birthday", birthday)
                put("location", location)
                put("bio", bio)

                // 如果有头像URI，也加入请求
                if(!avatarUrl.isNullOrEmpty()) {
                    put("avatarUrl", avatarUrl)
                }
            }.toString()

            val request = Request.Builder()
                .url("$BASE_URL/user/update")
                .put(jsonString.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (response.code == 200) {
                Pair(true, null)
            } else {
                Pair(false, "用户信息更新失败")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(false, "网络请求失败")
        }
    }

    /**
     * 上传用户头像
     * @param file 头像文件
     * @return Pair<Boolean, String?> 第一个参数表示是否成功，第二个参数为错误信息
     */
    fun uploadAvatar(file: File): Pair<Boolean, String?> {
        try {
            val requestFile = file.readBytes().toRequestBody("image/*".toMediaType())
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.name, requestFile)
                .build()

            val request = Request.Builder()
                .url("$BASE_URL/upload/userAvatar")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            val jsonObject = JSONObject(responseBody!!)
            return if (response.isSuccessful) {
                Pair(true, jsonObject.getJSONObject("data").getString("url"))
            } else {
                Pair(false, jsonObject.getString("message")?:"上传头像失败")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return Pair(false,"上传失败")
        }
    }


    /**
     * 搜索用户
     * @param keyword 搜索关键字，可以是用户名、邮箱或手机号
     * @return 返回匹配的用户列表
     */
    fun searchUser(keyword: String): List<User> {
        return try {
            if(keyword.isEmpty()) {
                return emptyList()
            }
            // 构建请求URL，传递搜索关键字
            val url = "$BASE_URL/user/list?pageNum=1&pageSize=10&keyword=$keyword"

            ConsoleUtils.logErr(url)
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.code == 200 && responseBody != null) {
                // 解析响应数据
                val jsonArray = JSONObject(responseBody).getJSONObject("data").getJSONArray("list")
                val userList = mutableListOf<User>()

                for (i in 0 until jsonArray.length()) {
                    val userObj = jsonArray.getJSONObject(i)
                    userList.add(formatUser(userObj))
                }
                userList
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }


    /**
     * 关注或取消关注用户
     */
    fun followUser(id: Int, isFollow: Boolean): Boolean {
        val builder = FormBody.Builder()
        builder.add("toUserId", id.toString())
        builder.add("isFollow", isFollow.toString())
        val request: Request = Request.Builder().url("$BASE_URL/user/followUser")
            .post(builder.build())
            .build()
        return try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            val jsonObject = JSONObject(responseBody!!)
            val code = jsonObject.getInt("code")
            code == 200
        } catch (e: java.lang.Exception) {
            false
        }
    }

    fun getFollowList(): List<User> {
        val request: Request = Request.Builder().url("$BASE_URL/user/getFollowList")
            .get()
            .build()
        val users = ArrayList<User>()
        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            val jsonObject = JSONObject(responseBody!!)
            if (response.code == 200) {
                val data = jsonObject.getJSONArray("data")
                for (i in 0 until data.length()) {
                    val userObj = data.getJSONObject(i)
                    users.add(formatUser(userObj))
                }
                return users
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return users
    }

    fun getFansList(): List<User> {
        val request: Request = Request.Builder().url("$BASE_URL/user/getFansList")
            .build()
        val users = ArrayList<User>()
        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            val jsonObject = JSONObject(responseBody!!)
            if (response.code == 200) {
                val data = jsonObject.getJSONArray("data")
                for (i in 0 until data.length()) {
                    val userJson = data.getJSONObject(i)
                    users.add(formatUser(userJson))
                }
                return users
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return users
    }

    private fun getUserInfo(id: Int?) : User? {
        val url: String = if(id == null){
            "$BASE_URL/user/getUserInfo?id=0"
        }else{
            "$BASE_URL/user/getUserInfo?id=$id"
        }

        val request: Request = Request.Builder().url(url)
            .build()
        try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            val jsonObject = JSONObject(responseBody!!)
            if (response.code == 200) {
                val data = jsonObject.getJSONObject("data")
                return formatUser(data)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 获取当前登录用户信息
     */
    fun getUserInfo(): User? {
        getUserInfo(null).let {
            if(it != null){
                // 获取用户的宠物列表
                val petList = PetHttpUtils.getUserPets(0)
                it.pets = petList
                return it
            }
        }
        return null
    }

    /**
     * 获取其他用户信息
     * @param userId 用户ID
     */
    fun getUserInfoById(userId: Int): User? {
        getUserInfo(userId).let {
            if(it != null){
                // 获取用户的宠物列表
                val petList = PetHttpUtils.getUserPets(userId)
                it.pets = petList
                return it
            }
        }
        return null
    }

    /**
     * 关注用户
     * @param userId 要关注的用户ID
     */
    fun followUser(userId: Int): Boolean {
        return followUser(userId, true)
    }

    /**
     * 取消关注用户
     * @param userId 要取消关注的用户ID
     */
    fun unfollowUser(userId: Int): Boolean {
        return followUser(userId, false)
    }

    private fun formatUser(userObj: JSONObject) : User{
        return User(
            userId = userObj.getInt("userId"),
            username = userObj.getString("username"),
            nickname = userObj.getString("nickname"),
            email = userObj.getString("email"),
            mobile = userObj.getString("mobile"),
            avatarUrl = userObj.getString("avatarUrl"),
            bio = if (!userObj.isNull("bio")) {
                userObj.getString("bio")
            } else {
                ""
            },
            gender = userObj.getInt("gender"),
            birthday = if (!userObj.isNull("birthday")) {
                Date(userObj.getLong("birthday"))
            } else {
                Date(0)
            },
            location = userObj.getString("location"),
            createdAt = Date(userObj.getLong("createdAt")),
            updatedAt = Date(userObj.getLong("updatedAt")),
            status = userObj.getInt("status"),
            isFollowed = userObj.getBoolean("isFollowed"),
            followCount = userObj.getInt("followCount"),
            fansCount = userObj.getInt("fansCount"),
            followTime = if (!userObj.isNull("followTime")) {
                userObj.getLong("followTime")
            } else {
                0L
            },
            mutualCount = userObj.getInt("mutualCount"),
            password = "",
            pets = emptyList()
        )
    }

    /**
     * 上传视频
     * @param path 视频路径
     */
    fun uploadVideo(path: String, desc: String): Boolean {
        val videoFile = File(path)
        if (!videoFile.exists()) { //视频不存在
            return false
        }
        val mediaType: MediaType? = "video/mp4".toMediaTypeOrNull()
        val requestBody: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", videoFile.name, RequestBody.create(mediaType, videoFile))
            .build()
        val request: Request = Request.Builder()
            .url("$BASE_URL/upload/video/userWorks?desc=$desc")
            .post(requestBody)
            .build()
        try {
            val result = client.newCall(request).execute().body?.string()
            val jsonObject = JSONObject(result!!)
            val code = jsonObject.getInt("code")
            return code == 200
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return false
    }
}