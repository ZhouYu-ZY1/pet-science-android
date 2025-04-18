
package com.zhouyu.pet_science.network

import PageResult
import Product
import com.zhouyu.pet_science.model.Category
import com.zhouyu.pet_science.network.HttpUtils.BASE_URL
import okhttp3.Request
import org.json.JSONObject
class ProductHttpUtils {
    companion object {
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
         * @param categoryId 分类ID（可选）
         */
        fun getProductList(pageNum: Int, pageSize: Int, productName: String? = null, categoryCode: String? = null): PageResult<Product>? {
            try {
                val urlBuilder = StringBuilder("$BASE_URL/product/list?pageNum=$pageNum&pageSize=$pageSize")
                if (!productName.isNullOrEmpty()) {
                    urlBuilder.append("&productName=$productName")
                }
                if (categoryCode != null) {
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
                        products.add(
                            Product(
                                productId = item.getInt("productId"),
                                productName = item.getString("productName"),
                                category = item.getString("category"),
                                price = item.getDouble("price"),
                                stock = item.getInt("stock"),
                                description = item.getString("description"),
                                mainImage = item.getString("mainImage"),
                                status = item.getInt("status"),
                                createdAt = item.getString("createdAt"),
                                updatedAt = item.getString("updatedAt")
                            )
                        )
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
    }
}