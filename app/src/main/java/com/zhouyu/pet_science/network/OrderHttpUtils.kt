package com.zhouyu.pet_science.network

import PageResult
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zhouyu.pet_science.model.CreateOrderRequest
import com.zhouyu.pet_science.model.Order
import com.zhouyu.pet_science.model.OrderResponse
import com.zhouyu.pet_science.model.Result
import com.zhouyu.pet_science.network.HttpUtils.BASE_URL
import com.zhouyu.pet_science.utils.ConsoleUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.HashMap

/**
 * 订单相关的网络请求工具类
 */
object OrderHttpUtils {

    /**
     * 获取订单列表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param params 筛选条件，可包含：orderNo, consignee, mobile, status, userId, startTime, endTime
     */
    suspend fun getOrderList(pageNum: Int, pageSize: Int, params: Map<String, Any>): Result<PageResult<Order>>? {
        return withContext(Dispatchers.IO) {
            try {
                val urlBuilder = StringBuilder("$BASE_URL/order/list?pageNum=$pageNum&pageSize=$pageSize")
                
                // 添加所有筛选条件
                params.forEach { (key, value) ->
                    if (key != "pageNum" && key != "pageSize" && value.toString().isNotEmpty()) {
                        urlBuilder.append("&$key=$value")
                    }
                }

                val request = Request.Builder()
                    .url(urlBuilder.toString())
                    .get()
                    .build()

                val response = HttpUtils.client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (responseBody != null) {
                    val gson = Gson()
                    val type = object : TypeToken<Result<PageResult<Order>>>() {}.type
                    gson.fromJson<Result<PageResult<Order>>>(responseBody, type)
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * 更新订单状态
     * @param orderId 订单ID
     * @param status 状态值
     */
    suspend fun updateOrderStatus(orderId: Int, status: String): Result<String>? {
        return withContext(Dispatchers.IO) {
            try {
                val jsonObject = JSONObject()
                jsonObject.put("orderId", orderId)
                jsonObject.put("status", status)

                val requestBody = jsonObject.toString()
                    .toRequestBody("application/json; charset=utf-8".toMediaType())

                val request = Request.Builder()
                    .url("$BASE_URL/order/status")
                    .put(requestBody)
                    .build()

                val response = HttpUtils.client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (responseBody != null) {
                    val gson = Gson()
                    val type = object : TypeToken<Result<String>>() {}.type
                    gson.fromJson<Result<String>>(responseBody, type)
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * 创建订单
     */
    suspend fun createOrder(orderRequest: CreateOrderRequest): Result<OrderResponse>? {
        return withContext(Dispatchers.IO) { // 使用协程在后台线程执行网络请求
            try {
                val gson = Gson()
                val json = gson.toJson(orderRequest)
                val requestBody = json.toRequestBody("application/json; charset=utf-8".toMediaType())

                val request = Request.Builder()
                    .url("$BASE_URL/order/create") // 确认后端接口路径
                    .post(requestBody)
                    .build()

                val response = HttpUtils.client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (responseBody != null) {
                    // 使用 Gson 解析嵌套的 Result<OrderResponse>
                    val type = object : TypeToken<Result<OrderResponse>>() {}.type
                    gson.fromJson<Result<OrderResponse>>(responseBody, type)
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * 获取订单详情
     * @param orderId 订单ID
     */
    suspend fun getOrderDetail(orderId: Int): Result<Order>? {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("$BASE_URL/order/getOrderDetail?orderId=$orderId")
                    .get()
                    .build()

                val response = HttpUtils.client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (responseBody != null) {
                    val gson = Gson()
                    val type = object : TypeToken<Result<Order>>() {}.type
                    gson.fromJson<Result<Order>>(responseBody, type)
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * 获取订单过期时间
     */
    fun getOrderExpiration(orderId: Int): Result<Long>? {
        try {
            val request = Request.Builder()
                .url("$BASE_URL/order/expiration/$orderId")
                .get()
                .build()

            val response = HttpUtils.client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && responseBody != null) {
                val gson = Gson()
                val type = object : TypeToken<Result<Long>>() {}.type
                return gson.fromJson(responseBody, type)
            }
            return null
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * 支付订单
     */
    fun payOrder(orderId: Int, paymentMethod: String): Result<String>? {
        try {
            val jsonObject = JSONObject()
            jsonObject.put("orderId", orderId)
            jsonObject.put("paymentMethod", paymentMethod)

            val requestBody = jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url("$BASE_URL/order/pay")
                .put(requestBody)
                .build()

            val response = HttpUtils.client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && responseBody != null) {
                val gson = Gson()
                val type = object : TypeToken<Result<String>>() {}.type
                return gson.fromJson(responseBody, type)
            }
            return null
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }



    /**
     * 完成订单
     * @param orderId 订单ID
     */
    suspend fun completeOrder(orderId: Int): Result<String>? {
        return withContext(Dispatchers.IO) {
            try {
                val jsonObject = JSONObject()
                jsonObject.put("orderId", orderId)

                val requestBody = jsonObject.toString()
                    .toRequestBody("application/json; charset=utf-8".toMediaType())

                val request = Request.Builder()
                    .url("$BASE_URL/order/complete")
                    .put(requestBody)
                    .build()

                val response = HttpUtils.client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (responseBody != null) {
                    val gson = Gson()
                    val type = object : TypeToken<Result<String>>() {}.type
                    gson.fromJson<Result<String>>(responseBody, type)
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
} 