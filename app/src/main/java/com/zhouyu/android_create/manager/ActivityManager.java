package com.zhouyu.android_create.manager;

import android.app.Activity;
import android.os.Looper;

import com.bumptech.glide.Glide;
import com.zhouyu.android_create.activities.MainActivity;
import com.zhouyu.android_create.application.Application;

import java.util.ArrayList;
import java.util.List;

public class ActivityManager {
    //存储Activity的List
    public static List<Activity> activities = new ArrayList<>();
    private static final ActivityManager instance = new ActivityManager();
    public static synchronized ActivityManager getInstance() {
        return instance;
    }

    //添加Activity
    public void addActivity(Activity activity) {
        if(activity != null && !isFinishingAll){
            if(activity.getClass().equals(MainActivity.class)){
                MainActivity.isRemove = false;
            }
            activities.add(activity);
        }
    }


    /**
     * 获取当前Activity
     */
    public Activity getCurrActivity(){
        if(activities.size() == 0){
            return null;
        }
        return activities.get(activities.size()-1);
    }

    //移出Activity
    public void removeActivity(Activity activity) {
        if(!isFinishingAll){ //防止结束所有Activity遍历时出现ConcurrentModificationException
            activities.remove(activity);
        }
    }

    private boolean isFinishingAll = false;
    //清除所有activity
    public void finishAllActivity() {
        if(activities == null){
            return;
        }
        try {
            Activity currActivity = getCurrActivity();
            if(currActivity != null){
                currActivity.finishAffinity(); //结束当前Activity之前所有Activity
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            isFinishingAll = true;
            for (Activity activity : activities) {
                if(!activity.isDestroyed() && !activity.isFinishing()){
                    if(activity.getClass().equals(MainActivity.class)){
                        MainActivity.isRemove = true;
                    }
                    activity.finish();
                }
            }
            isFinishingAll = false;
        } catch (Exception e){
            e.printStackTrace();
        }finally {
            activities.clear();
            Application.application.appAlive = -1;
            try {
                if (Looper.myLooper() != Looper.getMainLooper()) { //只能在主线程执行
                    Application.mainHandler.post(() -> Glide.get(Application.context).clearMemory()); //清理内存中的缓存
                }else {
                    Glide.get(Application.context).clearMemory();
                }
            }catch (Exception ignored){}
            System.gc();
        }
    }

    public void stopApplication(){
        finishAllActivity();

        isFinishApplication = true;
        Application.mainHandler.postDelayed(() -> {
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);

            //3秒之内程序还没结束则直接终止
            Application.mainHandler.postDelayed(() -> {
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
            },3000);
        },500);

        //结束别人程序的方法 需要权限：<uses-permission android:name="android.permission.KILL_BACKGROUND_PROCESSES" />
//        android.app.ActivityManager activityManager = (android.app.ActivityManager) Application.application.getSystemService(Context.ACTIVITY_SERVICE);
//        activityManager.killBackgroundProcesses(Application.application.getPackageName());
    }

    public static boolean isFinishApplication;
    //结束程序
    public void finishApplication() {
        stopApplication();
    }


    /**
     * 获取指定activity
     * @param specifyActivity 指定activity
     */
    public Activity getSpecifyActivity(Class<?> specifyActivity){
        try {
            for (int i = 0; i < activities.size(); i++) {
                Activity activity = activities.get(i);
                if(activity.getClass().equals(specifyActivity)){
                    return activity;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 仅保留一个activity
     * @param thisActivity 要保留的activity
     */
    public void KeepOneActivity(Class<?>  thisActivity){
        for (int i = 0; i < activities.size(); i++) {
            Activity activity = activities.get(i);
            if(activity.getClass() != thisActivity){
                if(activity.getClass().equals(MainActivity.class)){
                    MainActivity.isRemove = true;
                }
                activity.finish();
            }
        }
    }

}
