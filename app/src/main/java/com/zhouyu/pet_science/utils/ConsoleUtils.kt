package com.zhouyu.pet_science.utils

import android.util.Log

/**
 * 控制台打印工具
 */
object ConsoleUtils {
    private const val MY_TAG = "MyTAG"
    fun e(tag: String, message: String) {  //信息太长,分段打印
        //因为String的length是字符数量不是字节数量所以为了防止中文字符过多，
        //  把4*1024的MAX字节打印长度改为2001字符数
        var msg = message
        val maxStrLength = 2001 - tag.length
        //大于4000时
        while (msg.length > maxStrLength) {
            Log.e(tag, msg.substring(0, maxStrLength))
            msg = msg.substring(maxStrLength)
        }
        //剩余部分
        Log.e(tag, msg)
    }

    fun log(tag: String?, message: String, mode: String?) {
        when (mode) {
            "ERROR" -> Log.e(tag, "consoleErr: $message")
            "DEBUG" -> Log.d(tag, "consoleDeBug: $message")
            "INFO" -> Log.i(tag, "consoleInfo: $message")
            "WARN" -> Log.w(tag, "consoleWarn: $message")
        }
    }

    fun logErr(tag: String?, message: String) {
        log(tag, message, "ERROR")
    }

    fun logErr(message: String) {
        logErr(MY_TAG, message)
    }

    @JvmStatic
    fun logErr(message: Int) {
        logErr(MY_TAG, message.toString())
    }

    fun logErr(message: Long) {
        logErr(MY_TAG, message.toString())
    }

    fun logErr(message: Float) {
        logErr(MY_TAG, message.toString())
    }

    fun logErr(message: Boolean) {
        logErr(MY_TAG, message.toString())
    }

    private var startTime: Long = 0
    fun startTimer() {
        startTime = System.currentTimeMillis()
    }

    fun endTimer() {
        val l = System.currentTimeMillis()
        logErr(l - startTime)
        startTime = 0
    }
}
