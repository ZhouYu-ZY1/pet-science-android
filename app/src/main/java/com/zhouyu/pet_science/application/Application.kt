package com.zhouyu.pet_science.application

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import com.amap.api.maps.MapsInitializer
import com.zhou.keepalive.ext.keepAliveService
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.activities.MainActivity
import com.zhouyu.pet_science.manager.IMClientManager
import com.zhouyu.pet_science.utils.PhoneMessage
import com.zhouyu.pet_science.utils.StorageUtils
import me.jessyan.autosize.AutoSize
import me.jessyan.autosize.AutoSizeConfig
import java.lang.ref.WeakReference

@SuppressLint("StaticFieldLeak,SimpleDateFormat")
class Application : android.app.Application() {

    companion object {
        val mainHandler: Handler by lazy { Handler(Looper.getMainLooper()) } //主线程
        // 使用弱引用避免内存泄漏
        var weakContext: WeakReference<Context>? = null
        var weakApplication: WeakReference<Application>? = null

        var appFilesPath: String? = null //app文件目录
        var appCachePath: String? = null //app缓冲目录

        @JvmStatic
        fun getContext(): Context {
            return weakContext?.get()!!
        }
        @JvmStatic
        fun getApplication(): Application {
            return weakApplication?.get()!!
        }

//        var isNightMode: Boolean = false
//        fun isNightMode(): Boolean {
//            return NightModeUtils.isDarkTheme
//        }


        private val threadPool = java.util.concurrent.ThreadPoolExecutor(
            0, Int.MAX_VALUE,
            60L, java.util.concurrent.TimeUnit.SECONDS,
            java.util.concurrent.SynchronousQueue()
        ) //线程池

        fun executeThread(runnable: Runnable): Runnable {  //提交全局处理任务
            try {
                threadPool.execute(runnable)
            } catch (ignored: java.lang.Exception) {
            }
            return runnable
        }

        //获取APP缓存路径
        fun getDiskCachePath(context: android.content.Context): String {
            return if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
                || !Environment.isExternalStorageRemovable()
            ) {
                java.util.Objects.requireNonNull<java.io.File?>(context.externalCacheDir).path
            } else {
                context.cacheDir.path
            }
        }
    }



    override fun onCreate() {
        super.onCreate()
        weakContext = WeakReference(applicationContext)
        weakApplication = WeakReference(this)

        appFilesPath = getExternalFilesDir("")?.absolutePath
        appCachePath = getDiskCachePath(this)


        CatchException.getInstance().init(this)

        //设置适配默认值，防止一些性能差的机型读取meta-data较慢导致适配初始化失败
        //适配页面
        AutoSizeConfig.getInstance().setDesignWidthInDp(393).setDesignHeightInDp(851)
        //防止特殊情况下自启动失败
        if (!AutoSize.checkInit()) {
            AutoSize.checkAndInit(this)
        }


        //数据储存库初始化
        StorageUtils.init(this)


        //设备信息初始化
        PhoneMessage.initMessage(this)

        // 高德地图隐私协议
        MapsInitializer.updatePrivacyShow(this, true, true)
        MapsInitializer.updatePrivacyAgree(this, true)

        // 初始化保活服务
        initKeepAliveService()
    }



    /**
     * 初始化保活服务
     */
    fun initKeepAliveService() {
        // 在启动前台服务前检查权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!checkNotificationPermission()) {
                // 获取通知权限
                mainHandler.postDelayed(object : Runnable {
                    override fun run() {
                        if (checkNotificationPermission()) {
                            // 初始化保活服务
                            initKeepAliveService()
                            return
                        }
                        mainHandler.postDelayed(this, 1000)
                    }
                },1000)
                return // 等待权限授予后再继续
            }
        }
        try {
//            LogUtils.writeLogToFile(this, "开始初始化保活服务")
            // 点击通知时让应用回到前台
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // 启动保活服务
            keepAliveService {
                setTitle(getString(R.string.app_name))
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    val channel =  NotificationChannel(
                        "1001",
                        "前台服务",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "APP前台服务,用于后台接收消息"
                        setShowBadge(false)
                        setSound(null, null)
                        enableVibration(false)
                    }
                    // 注册通知渠道
                    val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    manager.createNotificationChannel(channel)
                    setNotificationChannel(channel)
                }
                setContent("前台服务正在运行中")
                setPendingIntent(pendingIntent)
                setSmallIcon(R.mipmap.ic_launcher_round)
                setMusicId(com.zhou.keepalive.R.raw.novioce)
//                isDebug(true)
                addCallback({
                    //onStop回调，可以省略
//                    LogUtils.writeLogToFile(applicationContext, "保活服务停止")
//                    warnTimedTaskManager?.get()?.handler?.removeCallbacksAndMessages( null)
                }) {
                    // doWork回调
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 检查是否开启通知权限
    fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED)
        } else {
            true
        }
    }
}
