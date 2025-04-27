package com.zhouyu.pet_science.tools.utils;

import android.os.Handler;
import android.os.Looper;

public class AndroidTimer {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable timerRunnable;
    private boolean isPaused = true;
    public long elapsedTime = 0;

    public AndroidTimer(){
        // 初始化Runnable
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isPaused) {
                    elapsedTime += 100;
                    handler.postDelayed(this, 100); // 每100毫秒更新一次
                }
            }
        };
    }

    // 启动计时器
    public void startTimer() {
        if (!isPaused) {
            // 如果没有暂停，则不需要重新开始
            return;
        }
        isPaused = false;
        handler.postDelayed(timerRunnable, 0); // 立即开始执行Runnable
    }

    // 暂停计时器
    public void pauseTimer() {
        isPaused = true;
        // 在这里不需要移除Runnable，因为当isPaused为true时，Runnable内部会检查这个状态并停止更新UI
    }

    public void clearTimer(){
        isPaused = true;
        elapsedTime = 0;
        handler.removeCallbacksAndMessages(null);
    }
}
