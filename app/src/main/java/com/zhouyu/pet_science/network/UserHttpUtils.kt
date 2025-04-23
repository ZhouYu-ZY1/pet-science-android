package com.zhouyu.pet_science.network

import android.net.Uri
import com.google.gson.Gson
import com.orhanobut.hawk.Hawk
import com.zhouyu.pet_science.activities.UserInfoEditActivity
import com.zhouyu.pet_science.model.User
import com.zhouyu.pet_science.network.HttpUtils.BASE_URL
import com.zhouyu.pet_science.network.HttpUtils.client
import com.zhouyu.pet_science.tools.utils.ConsoleUtils
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date
import java.util.Objects

class UserHttpUtils {
    companion object {
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
                ConsoleUtils.logErr(responseBody)
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
            avatarUri: Uri?,
            nickname: String,
            gender: String,
            birthday: String,
            location: String,
            bio: String,
            pets: MutableList<UserInfoEditActivity.PetInfo>
        ): Pair<Boolean, String?> {
            return try {
                // 将宠物信息转换为JSON数组
                val petsArray = JSONArray()
                pets.forEach { pet ->
                    val petObj = JSONObject().apply {
                        put("name", pet.name)
                        put("type", pet.type)
                        put("breed", pet.breed)
                        put("ageYear", pet.ageYear)
                        put("ageMonth", pet.ageMonth)
                    }
                    petsArray.put(petObj)
                }

                val jsonString = JSONObject().apply {
                    put("nickname", nickname)
                    put("gender", gender)
                    put("birthday", birthday)
                    put("location", location)
                    put("bio", bio)
                    put("pets", petsArray)
                    // 如果有头像URI，也加入请求
                    avatarUri?.let { put("avatar", it.toString()) }
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
                ConsoleUtils.logErr(responseBody)

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
                    Date()
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
                password = ""
            )
        }
    }
}