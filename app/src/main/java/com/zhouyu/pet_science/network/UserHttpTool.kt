package com.zhouyu.pet_science.network

import com.zhouyu.pet_science.network.HttpTool.BASE_URL
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class UserHttpTool {
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

                val response = HttpTool.client.newCall(request).execute()
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

                val response = HttpTool.client.newCall(request).execute()
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
    }
}