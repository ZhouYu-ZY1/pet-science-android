package com.zhouyu.pet_science.utils

import android.os.Handler
import android.os.Looper

class AndroidTimer {
    private val handler = Handler(Looper.getMainLooper())
    private val timerRunnable: Runnable
    private var isPaused = true
    var elapsedTime: Long = 0

    init {
        // 初始化Runnable
        timerRunnable = object : Runnable {
            override fun run() {
                if (!isPaused) {
                    elapsedTime += 100
                    handler.postDelayed(this, 100) // 每100毫秒更新一次
                }
            }
        }
    }

    // 启动计时器
    fun startTimer() {
        if (!isPaused) {
            // 如果没有暂停，则不需要重新开始
            return
        }
        isPaused = false
        handler.postDelayed(timerRunnable, 0) // 立即开始执行Runnable
    }

    // 暂停计时器
    fun pauseTimer() {
        isPaused = true
        // 在这里不需要移除Runnable，因为当isPaused为true时，Runnable内部会检查这个状态并停止更新UI
    }

    fun clearTimer() {
        isPaused = true
        elapsedTime = 0
        handler.removeCallbacksAndMessages(null)
    }
}
