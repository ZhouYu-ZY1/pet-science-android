package com.zhouyu.android_create.application;

import android.content.Intent;
import android.os.Looper;
import android.widget.Toast;

import com.zhouyu.android_create.activities.parent.ErrorActivity;
import com.zhouyu.android_create.manager.ActivityManager;
import com.zhouyu.android_create.tools.FileTool;
import com.zhouyu.android_create.tools.utils.ConsoleUtils;
import com.zhouyu.android_create.tools.utils.PhoneMessage;

import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;

/**
 * 全局异常捕获
 */
public class CatchException implements Thread.UncaughtExceptionHandler {
    //本类实例
    private static CatchException mInstance;
    private Thread.UncaughtExceptionHandler mDefaultException;

    //保证只有一个实例
    public CatchException() {
    }

    //单例模式
    public static CatchException getInstance() {
        if (mInstance == null) {
            mInstance = new CatchException();
        }
        return mInstance;
    }

    //获取系统默认的异常处理器,并且设置本类为系统默认处理器
    public void init() {
        mDefaultException = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }





    /**
     * 获取异常信息
     */
    public static String getStackTraceInfo(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        try {
            e.printStackTrace(pw);
            pw.flush();
            sw.flush();
            return sw.toString();
        } catch (Exception ex) {
            return "异常信息转换错误";
        } finally {
            try {
                pw.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            try {
                sw.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static Throwable errorThrowable;

    @Override
    public void uncaughtException(@NotNull Thread thread, @NotNull Throwable ex) {
        try {
            if((Thread.currentThread() == Looper.getMainLooper().getThread())){
                //UI线程的异常处理，启动新页面会导致无响应
                //直接复制错误信息
                if (!handlerException(ex) && mDefaultException != null) {
                    // 如果用户没有处理则让系统默认的异常处理器来处理
                    mDefaultException.uncaughtException(thread, ex);
                }else {
                    waitCollectMsg();
                }

            }else {
                //非UI线程，可直接打开异常页面
                errorThrowable = ex;
                Intent intent = new Intent(Application.context, ErrorActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Application.context.startActivity(intent);
            }
        }catch (Exception e){
            e.printStackTrace();

            // 如果启动Activity失败，可能是应用处于无法响应的状态
            // 这里可以进行最后的处理，如退出应用
            ActivityManager.getInstance().finishApplication();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }
    }


    //自定义错误处理器
    private boolean handlerException(Throwable ex) {
        if (ex == null) {  //如果已经处理过这个Exception,则让系统处理器进行后续关闭处理
            return false;
        }

        String message = ex.getMessage();
        if(message != null){
            if(message.contains("Context.startForegroundService() did not then call Service.startForeground()")
                    || message.contains("android.os.DeadSystemException")){
                return false;
            }
        }

        //获取错误原因
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null){
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String errMsg = writer.toString();

        Application.executeThread(() -> {
            try {
                Looper.prepare();
                Toast.makeText(Application.context, "发生未知错误,已复制错误信息，您可前往【微音乐助手】微信公众号反馈该问题", Toast.LENGTH_LONG).show();
                Looper.loop();
            }catch (Exception e){
                e.printStackTrace();
            }
        });

        Application.executeThread(() -> {
            try {
                long time = new Date().getTime();
                //保存本地
                String path = Application.appCachePath +"/error/"+ time +".err";
                String msg = errMsg +"\n"+ ErrorActivity.collectDeviceInfo(true);
                PhoneMessage.copy(msg);
                FileTool.commonStream.write(msg,path);
            }catch (Exception | Error ignored){
            } finally {
                isSucceed = true;
            }
        });
        return true;
    }
    private boolean isSucceed = false;

    private void waitCollectMsg(){
        try {
            Thread.sleep(3000);
        }catch (Exception ignored){}
        finally {
            if(isSucceed){
                ConsoleUtils.logErr("结束");
                ActivityManager.getInstance().finishApplication();
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
            }else {
                waitCollectMsg();
            }
        }
    }

}