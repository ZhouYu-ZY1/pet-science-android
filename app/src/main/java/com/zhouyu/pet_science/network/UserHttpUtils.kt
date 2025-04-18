package com.zhouyu.pet_science.network

import android.net.Uri
import com.zhouyu.pet_science.activities.UserInfoEditActivity
import com.zhouyu.pet_science.model.User
import com.zhouyu.pet_science.network.HttpUtils.BASE_URL
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date

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

                val response = HttpUtils.client.newCall(request).execute()
                val responseBody = response.body?.string()
                if (response.code == 200 && responseBody != null) {
                    Pair(true, null)
                } else {
                    Pair(false, "验证码发送失败")
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

                val response = HttpUtils.client.newCall(request).execute()
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

                val response = HttpUtils.client.newCall(request).execute()
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
         * 获取关注列表
         */
        fun getFollowList(): List<User> {
            val user = User(
                1,
                "test",
                "test",
                "test",
                "test",
                "/statics/images/defaultAvatar.jpg",
                "test",
                0,
                Date(),
                "",
                "",
                Date(),
                Date(),
                0,
                true,
                0,
                0,
                0
            )
            return listOf(user)
        }
    }
}