package com.zhouyu.pet_science.tools.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.DisplayMetrics;

import com.zhouyu.pet_science.application.Application;

public class NightModeUtils {

    /**
     * 切换 夜间模式
     * -1为跟随系统，0为关闭，1为开启
     */
    public static void updateNightMode(int mode) {
        //setDefaultNightMode参数有以下几种模式：
        //浅色 - MODE_NIGHT_NO
        //深色 - MODE_NIGHT_YES
        //由省电模式设置 - MODE_NIGHT_AUTO_BATTERY
        //系统默认 - MODE_NIGHT_FOLLOW_SYSTEM
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        if(mode != 2){
            boolean on;
            if(mode == 1){
                on = true;
            }else if(mode == 0){
                on = false;
            }else {
                return;
            }
            DisplayMetrics dm = Application.context.getResources().getDisplayMetrics();
            Configuration config = Application.context.getResources().getConfiguration();
            config.uiMode &= ~Configuration.UI_MODE_NIGHT_MASK;
            config.uiMode |= on ? Configuration.UI_MODE_NIGHT_YES : Configuration.UI_MODE_NIGHT_NO;
            Application.context.getResources().updateConfiguration(config, dm);
        }
    }

    public static boolean isDarkTheme() {
        int flag = Application.context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return flag == Configuration.UI_MODE_NIGHT_YES;
    }


    /**
     * 解决夜间模式webView不能加载
     */
    public static void startWebActivity(Activity activity, Intent intent){
        if(Application.isNightMode()){
            updateNightMode(Application.NightMode);
        }
        activity.startActivity(intent);
    }
}
