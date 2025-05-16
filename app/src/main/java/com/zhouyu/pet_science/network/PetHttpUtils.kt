package com.zhouyu.pet_science.network

import com.zhouyu.pet_science.model.Pet
import com.zhouyu.pet_science.network.HttpUtils.BASE_URL
import com.zhouyu.pet_science.network.HttpUtils.client
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.io.File
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody

object PetHttpUtils {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    /**
     * 获取用户的所有宠物
     */
    fun getUserPets(): List<Pet> {
        return try {
            val request = Request.Builder()
                .url("$BASE_URL/pet/list")
                .get()
                .build()
                
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            
            if (response.code == 200 && responseBody != null) {
                val jsonObject = JSONObject(responseBody)
                val dataArray = jsonObject.getJSONArray("data")
                val petList = mutableListOf<Pet>()
                
                for (i in 0 until dataArray.length()) {
                    val petObj = dataArray.getJSONObject(i)
                    petList.add(formatPet(petObj))
                }
                petList
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * 获取宠物详情
     */
    fun getPetDetail(id: Long): Pet? {
        return try {
            val request = Request.Builder()
                .url("$BASE_URL/pet/$id")
                .get()
                .build()
                
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            
            if (response.code == 200 && responseBody != null) {
                val jsonObject = JSONObject(responseBody)
                val petObj = jsonObject.getJSONObject("data")
                formatPet(petObj)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 添加宠物
     */
    fun addPet(pet: Pet): Pair<Boolean, Pet?> {
        return try {
            val jsonObject = JSONObject().apply {
                put("name", pet.name)
                put("type", pet.type)
                put("breed", pet.breed)
                put("birthday", dateFormat.format(pet.birthday))
                if (pet.avatarUrl.isNotEmpty()) {
                    put("avatarUrl", pet.avatarUrl)
                }
            }
            
            val request = Request.Builder()
                .url("$BASE_URL/pet/add")
                .post(jsonObject.toString().toRequestBody("application/json".toMediaType()))
                .build()
                
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            
            if (response.code == 200 && responseBody != null) {
                val resultObj = JSONObject(responseBody)
                val petObj = resultObj.getJSONObject("data")
                Pair(true, formatPet(petObj))
            } else {
                Pair(false, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(false, null)
        }
    }
    
    /**
     * 更新宠物信息
     */
    fun updatePet(pet: Pet): Pair<Boolean, String?> {
        return try {
            val jsonObject = JSONObject().apply {
                put("id", pet.id)
                put("name", pet.name)
                put("type", pet.type)
                put("breed", pet.breed)
                put("birthday", dateFormat.format(pet.birthday))
                if (pet.avatarUrl.isNotEmpty()) {
                    put("avatarUrl", pet.avatarUrl)
                }
            }
            
            val request = Request.Builder()
                .url("$BASE_URL/pet/update")
                .put(jsonObject.toString().toRequestBody("application/json".toMediaType()))
                .build()
                
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            
            if (response.code == 200 && responseBody != null) {
                Pair(true, null)
            } else {
                val jsonObject = JSONObject(responseBody!!)
                Pair(false, jsonObject.optString("message", "更新宠物信息失败"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(false, "网络请求失败")
        }
    }
    
    /**
     * 删除宠物
     */
    fun deletePet(id: Long): Pair<Boolean, String?> {
        return try {
            val request = Request.Builder()
                .url("$BASE_URL/pet/$id")
                .delete()
                .build()
                
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            
            if (response.code == 200 && responseBody != null) {
                Pair(true, null)
            } else {
                val jsonObject = JSONObject(responseBody!!)
                Pair(false, jsonObject.optString("message", "删除宠物失败"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(false, "网络请求失败")
        }
    }
    
    /**
     * 上传宠物头像
     */
    fun uploadPetAvatar(file: File, petId: Long): Pair<Boolean, String?> {
        return try {
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file", file.name,
                    file.asRequestBody("image/*".toMediaTypeOrNull())
                )
                .addFormDataPart("petId", petId.toString())
                .build()

            val request = Request.Builder()
                .url("$BASE_URL/upload/petAvatar")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.code == 200 && responseBody != null) {
                val jsonObject = JSONObject(responseBody)
                val dataObj = jsonObject.getJSONObject("data")
                val url = dataObj.getString("url")
                Pair(true, url)
            } else {
                val jsonObject = JSONObject(responseBody!!)
                Pair(false, jsonObject.optString("message", "上传宠物头像失败"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(false, "网络请求失败")
        }
    }
    
    /**
     * 格式化宠物对象
     */
    private fun formatPet(petObj: JSONObject): Pet {
        val birthdayStr = petObj.getString("birthday")
        val birthday = try {
            dateFormat.parse(birthdayStr) ?: Date()
        } catch (e: Exception) {
            Date(petObj.optLong("birthday", 0))
        }
        
        return Pet(
            id = petObj.getLong("id"),
            name = petObj.getString("name"),
            type = petObj.getString("type"),
            breed = petObj.getString("breed"),
            birthday = birthday,
            avatarUrl = petObj.optString("avatarUrl", "")
        )
    }
}