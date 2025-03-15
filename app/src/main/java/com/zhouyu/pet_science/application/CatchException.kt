package com.zhouyu.pet_science.application

import android.content.Intent
import android.os.Looper
import android.os.Process
import android.widget.Toast
import com.zhouyu.pet_science.activities.base.ErrorActivity
import com.zhouyu.pet_science.activities.base.ErrorActivity.Companion.collectDeviceInfo
import com.zhouyu.pet_science.manager.ActivityManager
import com.zhouyu.pet_science.tools.FileTool
import com.zhouyu.pet_science.tools.utils.ConsoleUtils
import com.zhouyu.pet_science.tools.utils.PhoneMessage
import java.io.PrintWriter
import java.io.StringWriter
import java.io.Writer
import java.util.Date
import kotlin.system.exitProcess

/**
 * 全局异常捕获
 */
class CatchException  //保证只有一个实例
    : Thread.UncaughtExceptionHandler {
    private var mDefaultException: Thread.UncaughtExceptionHandler? = null

    //获取系统默认的异常处理器,并且设置本类为系统默认处理器
    fun init() {
        mDefaultException = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(thread: Thread, ex: Throwable) {
        try {
            if (Thread.currentThread() === Looper.getMainLooper().thread) {
                //UI线程的异常处理，启动新页面会导致无响应
                //直接复制错误信息
                if (!handlerException(ex) && mDefaultException != null) {
                    // 如果用户没有处理则让系统默认的异常处理器来处理
                    mDefaultException!!.uncaughtException(thread, ex)
                } else {
                    waitCollectMsg()
                }
            } else {
                //非UI线程，可直接打开异常页面
                errorThrowable = ex
                val intent = Intent(Application.context, ErrorActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                Application.context.startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()

            // 如果启动Activity失败，可能是应用处于无法响应的状态
            // 这里可以进行最后的处理，如退出应用
            ActivityManager.instance.finishApplication()
            Process.killProcess(Process.myPid())
            exitProcess(0)
        }
    }

    //自定义错误处理器
    private fun handlerException(ex: Throwable?): Boolean {
        if (ex == null) {  //如果已经处理过这个Exception,则让系统处理器进行后续关闭处理
            return false
        }
        val message = ex.message
        if (message != null) {
            if (message.contains("Context.startForegroundService() did not then call Service.startForeground()")
                || message.contains("android.os.DeadSystemException")
            ) {
                return false
            }
        }

        //获取错误原因
        val writer: Writer = StringWriter()
        val printWriter = PrintWriter(writer)
        ex.printStackTrace(printWriter)
        var cause = ex.cause
        while (cause != null) {
            cause.printStackTrace(printWriter)
            cause = cause.cause
        }
        printWriter.close()
        val errMsg = writer.toString()
        Application.executeThread {
            try {
                Looper.prepare()
                Toast.makeText(Application.context, "发生未知错误", Toast.LENGTH_LONG).show()
                Looper.loop()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        Application.executeThread {
            try {
                val time = Date().time
                //保存本地
                val path = Application.appCachePath + "/error/" + time + ".err"
                val msg = """
                $errMsg
                ${collectDeviceInfo(true)}
                """.trimIndent()
                PhoneMessage.copy(msg)
                FileTool.commonStream.write(msg, path)
            } catch (ignored: Exception) {
            } catch (ignored: Error) {
            } finally {
                isSucceed = true
            }
        }
        return true
    }

    private var isSucceed = false
    private fun waitCollectMsg() {
        try {
            Thread.sleep(3000)
        } catch (ignored: Exception) {
        } finally {
            if (isSucceed) {
                ConsoleUtils.logErr("结束")
                ActivityManager.instance.finishApplication()
                Process.killProcess(Process.myPid())
                exitProcess(0)
            } else {
                waitCollectMsg()
            }
        }
    }

    companion object {
        //本类实例
        private var mInstance: CatchException? = null
        @JvmStatic
        val instance: CatchException?
            //单例模式
            get() {
                if (mInstance == null) {
                    mInstance = CatchException()
                }
                return mInstance
            }

        /**
         * 获取异常信息
         */
//        fun getStackTraceInfo(e: Exception): String {
//            val sw = StringWriter()
//            val pw = PrintWriter(sw)
//            return try {
//                e.printStackTrace(pw)
//                pw.flush()
//                sw.flush()
//                sw.toString()
//            } catch (ex: Exception) {
//                "异常信息转换错误"
//            } finally {
//                try {
//                    pw.close()
//                } catch (ex: Exception) {
//                    ex.printStackTrace()
//                }
//                try {
//                    sw.close()
//                } catch (ex: Exception) {
//                    ex.printStackTrace()
//                }
//            }
//        }

        var errorThrowable: Throwable? = null
    }
}