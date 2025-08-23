package com.zhouyu.pet_science.application

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.job.JobScheduler
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import android.widget.Toast
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.exitProcess

class CatchException private constructor() : Thread.UncaughtExceptionHandler {

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: CatchException? = null

        @JvmStatic
        fun getInstance(): CatchException {
            return instance ?: synchronized(this) {
                instance ?: CatchException().also { instance = it }
            }
        }

        private fun getStackTraceString(ex: Throwable): String {
            StringWriter().use { sw ->
                PrintWriter(sw).use { pw ->
                    ex.printStackTrace(pw)
                    return sw.toString()
                }
            }
        }

        fun collectDeviceInfo(isLocal: Boolean): String {
            val c = if (isLocal) {
                "\n"
            } else {
                "</br>"
            }
            val stringBuilder = StringBuilder()
            stringBuilder.append("Android版本: ").append(Build.VERSION.RELEASE).append(c)
            val fields = Build::class.java.declaredFields
            for (field in fields) {
                try {
                    field.isAccessible = true
                    stringBuilder.append(field.name).append(": ").append(
                        Objects.requireNonNull(
                            field[null]
                        ).toString()
                    ).append(c)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return stringBuilder.toString()
        }

        private fun getVersionName(context: Context?): String {
            return try {
                context?.packageManager?.getPackageInfo(context.packageName, 0)?.versionName ?: "未知"
            } catch (e: Exception) {
                "未知"
            }
        }
    }

    private var defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
    private val isHandling = AtomicBoolean(false)
    private var lastCrashTime = 0L
    private var context: Context? = null

    fun init(context: Context) {
        this.context = context.applicationContext
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(thread: Thread, ex: Throwable) {
        if (isSystemCrash(ex)) {
            defaultHandler?.uncaughtException(thread, ex)
            return
        }

        if (isHandling.getAndSet(true)) {
            forceExit()
            return
        }

        // 防止频繁崩溃处理
        if (SystemClock.elapsedRealtime() - lastCrashTime < 3000) {
            forceExit()
            return
        }
        lastCrashTime = SystemClock.elapsedRealtime()

        try {
            handleCrash(ex)
        } catch (e: Exception) {
            Log.e("崩溃处理", "处理崩溃时发生错误", e)
        } finally {
            forceExit()
        }
    }

    private fun handleCrash(ex: Throwable) {
        saveCrashLog(ex)
        Thread {
            Looper.prepare()
            context?.let {
                Toast.makeText(it, "程序遇到错误，正在保存错误日志", Toast.LENGTH_LONG).show()
            }
            Looper.loop()
        }.start()

        try {
            // 延迟1秒，等待Toast显示
            Thread.sleep(1000)
        }catch (_:  Exception){}
    }

    private fun isSystemCrash(ex: Throwable): Boolean {
        return when {
            ex is DeadSystemException -> true
            ex.message?.contains("android.os.DeadSystem") == true -> true
            ex.message?.contains("ForegroundServiceStartNotAllowedException") == true -> true
            else -> false
        }
    }

    private fun saveCrashLog(ex: Throwable) {
        try {
            val logContent = buildString {
                append("=== 设备信息 ===\n")
                append(collectDeviceInfo(true))
                append("\n=== 堆栈跟踪 ===\n")
                append(getStackTraceString(ex))
                append("\n\n")
            }

            saveToFile(logContent)
        } catch (e: Exception) {
            Log.e("崩溃处理", "保存崩溃日志失败", e)
        }
    }

    private fun saveToFile(content: String) {
        val externalDir = context?.getExternalFilesDir(null) ?: return
        val logDir = File(externalDir, "error_log").apply {
            if (!exists()) mkdirs()
        }

        val logFile = File(logDir, "error_${System.currentTimeMillis()}.log")
        try {
            FileWriter(logFile).use { writer ->
                writer.write(content)
                Log.d("崩溃处理", "崩溃日志已保存至 ${logFile.absolutePath}")
            }
        } catch (e: Exception) {
            Log.e("崩溃处理", "写入崩溃日志文件失败", e)
        }
    }

    /**
     * 强制退出程序，避免崩溃保存日志后APP无法正常退出
     */
    @Synchronized
    private fun forceExit() {
        try {
            // 1. 停止所有Activity
            (context?.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)?.apply {
                appTasks?.forEach { it.finishAndRemoveTask() }
            }

            // 2. 停止所有服务
            stopAllServices()

            // 3. 取消所有定时任务
            cancelPendingWorks()

            // 4. 杀死进程
            Process.killProcess(Process.myPid())

            // 5. 退出JVM
            exitProcess(10)
        } catch (_: Exception) { }
        finally {
            // Android 10+ 额外措施，比System.exit()或android.os.Process.killProcess()更底层，因为它直接使用了Linux系统的进程信号机制
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Process.sendSignal(Process.myPid(), Process.SIGNAL_KILL)
            }
            // 最终尝试
            Runtime.getRuntime().exit(0)
        }
    }

    private fun stopAllServices() {
        try {
            val activityManager = context?.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            activityManager?.getRunningServices(Integer.MAX_VALUE)?.forEach { service ->
                if (service.service.packageName == context?.packageName) {
                    val intent = Intent().apply {
                        component = service.service
                    }
                    context?.stopService(intent)
                }
            }
        } catch (e: Exception) {
            Log.e("崩溃处理", "停止服务失败", e)
        }
    }

    private fun cancelPendingWorks() {
        try {
            // 取消AlarmManager定时任务
            val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.apply {
                getPendingIntent()?.let { cancel(it) }
            }

            // 取消JobScheduler任务
            (context?.getSystemService(Context.JOB_SCHEDULER_SERVICE) as? JobScheduler)?.cancelAll()

            // 取消WorkManager任务
//            context?.let { WorkManager.getInstance(it).cancelAllWork() }
        } catch (e: Exception) {
            Log.e("崩溃处理", "取消定时任务失败", e)
        }
    }

    private fun getPendingIntent(): PendingIntent? {
        return try {
            val intent = Intent(context, context?.javaClass)
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        } catch (e: Exception) {
            null
        }
    }
}