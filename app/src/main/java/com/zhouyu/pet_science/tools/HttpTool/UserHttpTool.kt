package com.zhouyu.pet_science.tools.HttpTool

import com.zhouyu.pet_science.tools.utils.ConsoleUtils
import okhttp3.Request
import okhttp3.RequestBody

class UserHttpTool {
    companion object {
        private const val IP = "172.18.238.246"
        private const val BASE_URL = "http://$IP:8888"

        /**
         * 发送验证码
         * @param email 邮箱地址
         * @return Pair<Boolean, String?> 第一个参数表示是否成功，第二个参数为错误信息
         */
        fun sendVerificationCode(email: String): Pair<Boolean, String?> {
            return try {
                val request = Request.Builder()
                    .url("$BASE_URL/user/sendVerificationCode?email=$email")
                    .post(RequestBody.create(null, ""))
                    .build()

                val response = HttpTool.client.newCall(request).execute()
                val responseBody = response.body?.string()

                ConsoleUtils.logErr(responseBody)
                if (response.isSuccessful && responseBody != null) {
                    Pair(true, null)
                } else {
                    Pair(false, "验证码发送失败")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Pair(false, "网络请求失败")
            }
        }
    }
}