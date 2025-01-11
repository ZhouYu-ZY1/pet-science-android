package com.zhouyu.android_create.tools.utils;

import android.util.Log;

/**
 * 控制台打印工具
 */
public class ConsoleUtils {
    private final static String myTAG = "MyTAG";

    public static void e(String tag, String msg) {  //信息太长,分段打印
        //因为String的length是字符数量不是字节数量所以为了防止中文字符过多，
        //  把4*1024的MAX字节打印长度改为2001字符数
        int max_str_length = 2001 - tag.length();
        //大于4000时
        while (msg.length() > max_str_length) {
            Log.e(tag, msg.substring(0, max_str_length));
            msg = msg.substring(max_str_length);
        }
        //剩余部分
        Log.e(tag, msg);
    }

    public static void log(String TAG,String message,String mode){
        switch (mode){
            case "ERROR":
                Log.e(TAG, "consoleErr: " + message);
                break;
            case "DEBUG":
                Log.d(TAG, "consoleDeBug: " + message);
                break;
            case "INFO":
                Log.i(TAG, "consoleInfo: " + message);
                break;
            case "WARN":
                Log.w(TAG, "consoleWarn: " + message);
        }
    }

    public static void logErr(String TAG,String message){
        log(TAG,message,"ERROR");
    }
    public static void logErr(String message){
        logErr(myTAG,message);
    }

    public static void logErr(int message) {
        logErr(myTAG,String.valueOf(message));
    }

    public static void logErr(long message) {
        logErr(myTAG,String.valueOf(message));
    }

    public static void logErr(float message) {
        logErr(myTAG,String.valueOf(message));
    }


    public static void logErr(boolean message) {
        logErr(myTAG,String.valueOf(message));
    }



    private static long startTime;
    public static void startTimer(){
        startTime = System.currentTimeMillis();
    }
    public static void endTimer(){
        long l = System.currentTimeMillis();
        logErr(l - startTime);
        startTime = 0;
    }
}
