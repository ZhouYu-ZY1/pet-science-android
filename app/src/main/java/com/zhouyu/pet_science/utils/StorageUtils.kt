package com.zhouyu.pet_science.utils

import com.orhanobut.hawk.Hawk

object StorageUtils {
    /**
     * 添加
     */
    fun put(key: String?, value: Any): Boolean {
        return try {
            Hawk.put(key, value)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 删除
     */
    fun delete(key: String?): Boolean {
        return try {
            Hawk.delete(key)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 获取
     */
    operator fun <T> get(key: String?): T {
        return Hawk.get(key)
    }

    /**
     * 判断指定Key是否存在
     */
    operator fun contains(key: String?): Boolean {
        return try {
            Hawk.contains(key)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
