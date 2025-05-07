
package com.zhouyu.pet_science.network

import PageResult
import Product
import com.zhouyu.pet_science.model.Category
import com.zhouyu.pet_science.network.HttpUtils.BASE_URL
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zhouyu.pet_science.model.CreateOrderRequest
import com.zhouyu.pet_science.model.Order
import com.zhouyu.pet_science.model.OrderResponse
import com.zhouyu.pet_science.model.UserAddress
import com.zhouyu.pet_science.model.Result // 确保导入了 Result 类
import com.zhouyu.pet_science.utils.ConsoleUtils
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONObject
object ProductHttpUtils {
    /**
     * 获取分类列表
     */
    fun getCategoryList(): List<Category>? {
        try {
            val request = Request.Builder()
                .url("$BASE_URL/category/list")
                .get()
                .build()

            val response = HttpUtils.client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.code == 200 && responseBody != null) {
                val jsonObject = JSONObject(responseBody)
                val dataObject = jsonObject.getJSONObject("data")
                val categoryArray = dataObject.getJSONArray("list")

                val categories = ArrayList<Category>()
                for (i in 0 until categoryArray.length()) {
                    val item = categoryArray.getJSONObject(i)
                    categories.add(
                        Category(
                            item.getInt("categoryId"),
                            item.getString("categoryCode"),
                            item.getString("categoryName")
                        )
                    )
                }
                return categories
            } else {
                return null
            }
        }catch (e: Exception){
            e.printStackTrace()
            return null
        }
    }

    /**
     * 获取商品列表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param productName 商品名称（可选）
     * @param categoryCode 分类ID（可选）
     */
    fun getProductList(pageNum: Int, pageSize: Int, productName: String? = null, categoryCode: String? = null): PageResult<Product>? {
        try {
            val urlBuilder = StringBuilder("$BASE_URL/product/list?pageNum=$pageNum&pageSize=$pageSize")
            if (!productName.isNullOrEmpty()) {
                urlBuilder.append("&productName=$productName")
            }
            if (categoryCode != null && categoryCode != "all") {
                urlBuilder.append("&category=$categoryCode")
            }

            val request = Request.Builder()
                .url(urlBuilder.toString())
                .get()
                .build()

            val response = HttpUtils.client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.code == 200 && responseBody != null) {
                val jsonObject = JSONObject(responseBody)
                val dataObject = jsonObject.getJSONObject("data")
                val productArray = dataObject.getJSONArray("list")

                val products = ArrayList<Product>()
                for (i in 0 until productArray.length()) {
                    val item = productArray.getJSONObject(i)
                    products.add(formatProduct(item))
                }

                return PageResult(
                    pageNum = dataObject.getInt("pageNum"),
                    pageSize = dataObject.getInt("pageSize"),
                    total = dataObject.getInt("total"),
                    list = products
                )
            } else {
                return null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * 搜索商品
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param keyword 搜索关键字
     */
    fun searchProduct(pageNum: Int, pageSize: Int,keyword: String): ArrayList<Product> {
        try {
            if(keyword.isEmpty()) return ArrayList()
            val urlBuilder = StringBuilder("$BASE_URL/product/search?pageNum=$pageNum&pageSize=$pageSize&keyword=$keyword")

            val request = Request.Builder()
                .url(urlBuilder.toString())
                .get()
                .build()
            val response = HttpUtils.client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.code == 200 && responseBody != null) {
                val jsonObject = JSONObject(responseBody)
                val dataObject = jsonObject.getJSONObject("data")
                val productArray = dataObject.getJSONArray("list")

                val products = ArrayList<Product>()
                for (i in 0 until productArray.length()) {
                    val item = productArray.getJSONObject(i)
                    products.add(formatProduct(item))
                }
                return products
            } else {
                return ArrayList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return ArrayList()
        }
    }

    private fun formatProduct(item: JSONObject): Product {
        val images = item.getString("mainImage")
        return Product(
            productId = item.getInt("productId"),
            productName = item.getString("productName"),
            category = item.getString("category"),
            price = item.getDouble("price"),
            stock = item.getInt("stock"),
            description = item.getString("description"),
            images = images,
            imageUrl =  getFirstImage(images),
            sales = "2.3万+",
            status = item.getInt("status"),
            createdAt = item.getString("createdAt"),
            updatedAt = item.getString("updatedAt")
        )
    }
    /**
     * 获取第一张图片
     */
    fun getFirstImage(mainImage: String): String {
        if (mainImage.isEmpty()) return ""
        val image = mainImage.split(";")[0]
        return "$BASE_URL$image"
    }

    /**
     * 获取图片列表
     */
    fun getImageList(mainImage: String): List<String> {
        if (mainImage.isEmpty()) return emptyList()
        return mainImage.split(";")
            .filter { it.isNotEmpty() }
            .map { "$BASE_URL$it" }
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
     * 获取用户的所有收货地址
     */
    fun getUserAddresses(): Result<List<UserAddress>>? {
        val url = "$BASE_URL/address/list"
        return try {
            val response = HttpUtils.get(url)
            val type = object : TypeToken<Result<List<UserAddress>>>() {}.type
            Gson().fromJson(response, type)
        } catch (e: Exception) {
            ConsoleUtils.logErr("获取用户地址列表失败: ${e.message}")
            null
        }
    }

    /**
     * 获取收货地址详情
     */
    fun getAddressDetail(id: Int): Result<UserAddress>? {
        val url = "$BASE_URL/address/$id"
        return try {
            val response = HttpUtils.get(url)
            val type = object : TypeToken<Result<UserAddress>>() {}.type
            Gson().fromJson(response, type)
        } catch (e: Exception) {
            ConsoleUtils.logErr("获取地址详情失败: ${e.message}")
            null
        }
    }

    /**
     * 获取默认收货地址
     */
    fun getDefaultAddress(): Result<UserAddress>? {
        val url = "$BASE_URL/address/default"
        return try {
            val response = HttpUtils.get(url)
            val type = object : TypeToken<Result<UserAddress>>() {}.type
            Gson().fromJson(response, type)
        } catch (e: Exception) {
            ConsoleUtils.logErr("获取默认地址失败: ${e.message}")
            null
        }
    }

    /**
     * 添加收货地址
     */
    fun addAddress(address: UserAddress): Result<UserAddress>? {
        val url = "$BASE_URL/address/add"
        return try {
            val jsonBody = Gson().toJson(address)
            val response = HttpUtils.post(url, jsonBody)
            val type = object : TypeToken<Result<UserAddress>>() {}.type
            Gson().fromJson(response, type)
        } catch (e: Exception) {
            ConsoleUtils.logErr("添加地址失败: ${e.message}")
            null
        }
    }

    /**
     * 更新收货地址
     */
    fun updateAddress(address: UserAddress): Result<String>? {
        val url = "$BASE_URL/address/update"
        return try {
            val jsonBody = Gson().toJson(address)
            val response = HttpUtils.put(url, jsonBody)
            val type = object : TypeToken<Result<String>>() {}.type
            Gson().fromJson(response, type)
        } catch (e: Exception) {
            ConsoleUtils.logErr("更新地址失败: ${e.message}")
            null
        }
    }

    /**
     * 删除收货地址
     */
    fun deleteAddress(id: Int): Result<String>? {
        val url = "$BASE_URL/address/$id"
        return try {
            val response = HttpUtils.delete(url)
            val type = object : TypeToken<Result<String>>() {}.type
            Gson().fromJson(response, type)
        } catch (e: Exception) {
            ConsoleUtils.logErr("删除地址失败: ${e.message}")
            null
        }
    }

    /**
     * 设置默认地址
     */
    fun setDefaultAddress(id: Int): Result<String>? {
        val url = "$BASE_URL/address/default/$id"
        return try {
            val response = HttpUtils.put(url, "") // PUT请求体可以为空字符串
            val type = object : TypeToken<Result<String>>() {}.type
            Gson().fromJson(response, type)
        } catch (e: Exception) {
            ConsoleUtils.logErr("设置默认地址失败: ${e.message}")
            null
        }
    }
}