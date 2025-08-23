package com.zhouyu.pet_science.utils

import android.app.Application
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.tencent.mmkv.MMKV
import java.lang.reflect.Type

/**
 * MMKV 存储封装
 * 支持多进程数据共享
 */
object StorageUtils {
    const val TAG = "StorageUtils"

    /**
     * 存储类型枚举
     */
    enum class StorageType {
        DEFAULT,    // 默认存储
        USER,       // 用户数据存储
        CONFIG,     // 应用配置存储
        CACHE,      // 缓存数据存储
        ENCRYPTED   // 加密存储
    }

    // 默认的多进程MMKV实例
    private val defaultKV: MMKV by lazy {
        MMKV.mmkvWithID("default_storage", MMKV.MULTI_PROCESS_MODE)
    }

    // 用户相关的多进程存储
    private val userKV: MMKV by lazy {
        MMKV.mmkvWithID("user_storage", MMKV.MULTI_PROCESS_MODE)
    }

    // 应用配置的多进程存储
    private val configKV: MMKV by lazy {
        MMKV.mmkvWithID("config_storage", MMKV.MULTI_PROCESS_MODE)
    }

    // 缓存数据的多进程存储
    private val cacheKV: MMKV by lazy {
        MMKV.mmkvWithID("cache_storage", MMKV.MULTI_PROCESS_MODE)
    }

    // 加密存储
    private val encryptedKV: MMKV by lazy {
        val cryptKey = "app!@123"
        MMKV.mmkvWithID("encrypted_storage", MMKV.MULTI_PROCESS_MODE, cryptKey)
    }

    private fun getKV(storageType: StorageType): MMKV {
        return when (storageType) {
            StorageType.DEFAULT -> defaultKV
            StorageType.USER -> userKV
            StorageType.CONFIG -> configKV
            StorageType.CACHE -> cacheKV
            StorageType.ENCRYPTED -> encryptedKV
        }
    }

    // 安全的Gson配置
    private val gson: Gson by lazy {
        GsonBuilder()
            .disableHtmlEscaping() // 禁用Html转义
            .create()
    }

    /**
     * 初始化MMKV
     */
    fun init(context: Application) {
        MMKV.initialize(context)
    }

    /**
     * 存储数据到指定存储实例
     */
    fun put(key: String, value: Any, storageType: StorageType = StorageType.DEFAULT): Boolean {
        return try {
            val kv = getKV(storageType)
            when (value) {
                is String -> kv.encode(key, value)
                is Int -> kv.encode(key, value)
                is Long -> kv.encode(key, value)
                is Float -> kv.encode(key, value)
                is Double -> kv.encode(key, value)
                is Boolean -> kv.encode(key, value)
                else -> kv.encode(key, gson.toJson(value))
            }
        } catch (e: Exception) {
            Log.e(TAG, "put data error: key=$key, value=$value, storageType=$storageType", e)
            false
        }
    }

    /**
     * 从指定存储实例获取数据
     */
    inline fun <reified T> get(key: String, defaultValue: T, storageType: StorageType = StorageType.DEFAULT): T {
        return try {
            val type = object : TypeToken<T>() {}.type
            get(key, defaultValue, type, storageType)
        } catch (e: Exception) {
            Log.e(TAG, "get data error: key=$key, defaultValue=$defaultValue, storageType=$storageType", e)
            defaultValue
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String, defaultValue: T, type: Type, storageType: StorageType): T {
        return try {
            val kv = getKV(storageType)
            when (defaultValue) {
                is String -> kv.decodeString(key, defaultValue) as T
                is Int -> kv.decodeInt(key, defaultValue) as T
                is Long -> kv.decodeLong(key, defaultValue) as T
                is Float -> kv.decodeFloat(key, defaultValue) as T
                is Boolean -> kv.decodeBool(key, defaultValue) as T
                else -> {
                    val json = kv.decodeString(key, "")
                    return if (!json.isNullOrEmpty()) {
                        gson.fromJson(json, type)
                    } else {
                        defaultValue
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "get data error: key=$key, defaultValue=$defaultValue, type=$type, storageType=$storageType", e)
            defaultValue
        }
    }

    /**
     * 从指定存储实例删除数据
     */
    fun delete(key: String, storageType: StorageType = StorageType.DEFAULT): Boolean {
        return try {
            getKV(storageType).removeValueForKey(key)
            true
        } catch (e: Exception) {
            Log.e(TAG, "delete data error: key=$key, storageType=$storageType", e)
            false
        }
    }

    /**
     * 判断指定Key是否存在
     */
    fun contains(key: String, storageType: StorageType = StorageType.DEFAULT): Boolean {
        return try {
            getKV(storageType).containsKey(key)
        } catch (e: Exception) {
            Log.e(TAG, "contains key error: key=$key, storageType=$storageType", e)
            false
        }
    }

    /**
     * 清空指定存储实例的所有数据
     */
    fun clearAll(storageType: StorageType = StorageType.DEFAULT) {
        try {
            getKV(storageType).clearAll()
        } catch (e: Exception) {
            Log.e(TAG, "clear all data error: storageType=$storageType", e)
        }
    }

    /**
     * 获取指定存储实例的所有键
     */
    fun getAllKeys(storageType: StorageType = StorageType.DEFAULT): Array<String> {
        return try {
            getKV(storageType).allKeys() ?: emptyArray()
        } catch (e: Exception) {
            Log.e(TAG, "get all keys error: storageType=$storageType", e)
            emptyArray<String>()
        }
    }
}