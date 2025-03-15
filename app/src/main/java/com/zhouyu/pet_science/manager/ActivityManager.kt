package com.zhouyu.pet_science.manager

import android.app.Activity
import android.os.Looper
import android.os.Process
import com.bumptech.glide.Glide
import com.zhouyu.pet_science.activities.MainActivity
import com.zhouyu.pet_science.application.Application

class ActivityManager {
    //添加Activity
    fun addActivity(activity: Activity?) {
        if (activity != null && !isFinishingAll) {
            if (activity.javaClass == MainActivity::class.java) {
                MainActivity.isRemove = false
            }
            activities!!.add(activity)
        }
    }

    val currActivity: Activity?
        /**
         * 获取当前Activity
         */
        get() = if (activities!!.size == 0) {
            null
        } else activities!![activities!!.size - 1]

    //移出Activity
    fun removeActivity(activity: Activity) {
        if (!isFinishingAll) { //防止结束所有Activity遍历时出现ConcurrentModificationException
            activities!!.remove(activity)
        }
    }

    private var isFinishingAll = false

    //清除所有activity
    fun finishAllActivity() {
        if (activities == null) {
            return
        }
        try {
            val currActivity = currActivity
            currActivity?.finishAffinity()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            isFinishingAll = true
            for (activity in activities!!) {
                if (!activity.isDestroyed && !activity.isFinishing) {
                    if (activity.javaClass == MainActivity::class.java) {
                        MainActivity.isRemove = true
                    }
                    activity.finish()
                }
            }
            isFinishingAll = false
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            activities!!.clear()
            try {
                if (Looper.myLooper() != Looper.getMainLooper()) { //只能在主线程执行
                    Application.mainHandler.post {
                        Glide.get(Application.context).clearMemory()
                    } //清理内存中的缓存
                } else {
                    Glide.get(Application.context).clearMemory()
                }
            } catch (ignored: Exception) {
            }
            System.gc()
        }
    }

    fun stopApplication() {
        finishAllActivity()
        isFinishApplication = true
        Application.mainHandler.postDelayed({
            Process.killProcess(Process.myPid())
            System.exit(0)

            //3秒之内程序还没结束则直接终止
            Application.mainHandler.postDelayed({
                Process.killProcess(Process.myPid())
                System.exit(0)
            }, 3000)
        }, 500)

        //结束别人程序的方法 需要权限：<uses-permission android:name="android.permission.KILL_BACKGROUND_PROCESSES" />
//        android.app.ActivityManager activityManager = (android.app.ActivityManager) Application.application.getSystemService(Context.ACTIVITY_SERVICE);
//        activityManager.killBackgroundProcesses(Application.application.getPackageName());
    }

    //结束程序
    fun finishApplication() {
        stopApplication()
    }

    /**
     * 获取指定activity
     * @param specifyActivity 指定activity
     */
    fun getSpecifyActivity(specifyActivity: Class<*>): Activity? {
        try {
            for (i in activities!!.indices) {
                val activity = activities!![i]
                if (activity.javaClass == specifyActivity) {
                    return activity
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 仅保留一个activity
     * @param thisActivity 要保留的activity
     */
    fun KeepOneActivity(thisActivity: Class<*>) {
        for (i in activities!!.indices) {
            val activity = activities!![i]
            if (activity.javaClass != thisActivity) {
                if (activity.javaClass == MainActivity::class.java) {
                    MainActivity.isRemove = true
                }
                activity.finish()
            }
        }
    }

    companion object {
        //存储Activity的List
        var activities: MutableList<Activity>? = ArrayList()

        @get:Synchronized
        val instance = ActivityManager()
        var isFinishApplication = false
    }
}
