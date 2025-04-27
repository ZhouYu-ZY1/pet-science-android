package com.zhouyu.pet_science.utils

import android.content.Context
import com.google.gson.Gson
import com.zhouyu.pet_science.pojo.CityJsonBean
import org.json.JSONArray
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

object GetJsonDataUtil {

    //文件读取JSON
    fun getJson(context: Context, fileName: String): String {
        val stringBuilder = StringBuilder()
        try {
            val assetManager = context.assets
            val bf = BufferedReader(
                InputStreamReader(
                    assetManager.open(fileName)
                )
            )
            var line: String?
            while (bf.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return stringBuilder.toString()
    }
    fun parseData(result: String?): java.util.ArrayList<CityJsonBean> { //Gson 解析
        val detail = java.util.ArrayList<CityJsonBean>()
        try {
            val data = JSONArray(result)
            val gson = Gson()
            for (i in 0 until data.length()) {
                val entity =
                    gson.fromJson(data.optJSONObject(i).toString(), CityJsonBean::class.java)
                detail.add(entity)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return detail
    }
}
