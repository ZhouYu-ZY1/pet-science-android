package com.zhouyu.pet_science.utils

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import com.zhouyu.pet_science.application.Application

object NightModeUtils {
    /**
     * 切换 夜间模式
     * -1为跟随系统，0为关闭，1为开启
     */
    fun updateNightMode(mode: Int) {
        //setDefaultNightMode参数有以下几种模式：
        //浅色 - MODE_NIGHT_NO
        //深色 - MODE_NIGHT_YES
        //由省电模式设置 - MODE_NIGHT_AUTO_BATTERY
        //系统默认 - MODE_NIGHT_FOLLOW_SYSTEM
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        if (mode != 2) {
            val on: Boolean
            on = if (mode == 1) {
                true
            } else if (mode == 0) {
                false
            } else {
                return
            }
            val dm = Application.context.resources.displayMetrics
            val config = Application.context.resources.configuration
            config.uiMode = config.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()
            config.uiMode =
                config.uiMode or if (on) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
            Application.context.resources.updateConfiguration(config, dm)
        }
    }

    @JvmStatic
    val isDarkTheme: Boolean
        get() {
            val flag =
                Application.context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            return flag == Configuration.UI_MODE_NIGHT_YES
        }

    /**
     * 解决夜间模式webView不能加载
     */
    fun startWebActivity(activity: Activity, intent: Intent?) {
        if (Application.isNightMode()) {
            updateNightMode(Application.NightMode)
        }
        activity.startActivity(intent)
    }
}
