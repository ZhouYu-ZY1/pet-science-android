package com.zhouyu.android_create.tools.utils;

import android.app.AppOpsManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.zhouyu.android_create.application.Application;

public class NotificationsUtils {
    private static final String CHECK_OP_NO_THROW = "checkOpNoThrow";
    private static final String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";


    //调用该方法获取是否开启通知栏权限
    public static boolean isNotifyEnabled(Context mContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NotificationManager notificationManager = (NotificationManager) Application.context.getSystemService(Context.NOTIFICATION_SERVICE);
            return notificationManager.areNotificationsEnabled();
        } else {
            AppOpsManager appOps =
                    (AppOpsManager) mContext.getSystemService(Context.APP_OPS_SERVICE);
            ApplicationInfo appInfo = mContext.getApplicationInfo();
            String pkg = mContext.getApplicationContext().getPackageName();
            int uid = appInfo.uid;
            try {
                Class<?> appOpsClass = Class.forName(AppOpsManager.class.getName());
                Method checkOpNoThrowMethod = appOpsClass.getMethod(CHECK_OP_NO_THROW, Integer.TYPE,
                        Integer.TYPE, String.class);
                Field opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);
                int value = (int) opPostNotificationValue.get(Integer.class);
                return ((int) checkOpNoThrowMethod.invoke(appOps, value, uid, pkg)
                        == AppOpsManager.MODE_ALLOWED);
            } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException
                    | InvocationTargetException | IllegalAccessException | RuntimeException e) {
                return true;
            }
        }
    }

//    /**
//     * 8.0以下判断
//     * @param context api19  4.4及以上判断
//     */
//    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
//    private static boolean isEnabledV19(Context context) {
//
//        AppOpsManager mAppOps =
//                (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
//
//        ApplicationInfo appInfo = context.getApplicationInfo();
//        String pkg = context.getApplicationContext().getPackageName();
//        int uid = appInfo.uid;
//        Class appOpsClass;
//        try {
//            appOpsClass = Class.forName(AppOpsManager.class.getName());
//
//            Method checkOpNoThrowMethod =
//                    appOpsClass.getMethod(CHECK_OP_NO_THROW,
//                            Integer.TYPE, Integer.TYPE, String.class);
//
//            Field opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);
//            int value = (Integer) opPostNotificationValue.get(Integer.class);
//
//            return ((Integer) checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) ==
//                    AppOpsManager.MODE_ALLOWED);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return false;
//    }


//    /**
//     * 8.0及以上通知权限判断
//     */
//    private static boolean isEnableV26(Context context) {
//        ApplicationInfo appInfo = context.getApplicationInfo();
//        String pkg = context.getApplicationContext().getPackageName();
//        int uid = appInfo.uid;
//        try {
//            NotificationManager notificationManager = (NotificationManager)
//                    context.getSystemService(Context.NOTIFICATION_SERVICE);
//            @SuppressLint("DiscouragedPrivateApi") Method sServiceField = notificationManager.getClass().getDeclaredMethod("getService");
//            sServiceField.setAccessible(true);
//            Object sService = sServiceField.invoke(notificationManager);
//
//            Method method = sService.getClass().getDeclaredMethod("areNotificationsEnabledForPackage"
//                    , String.class, Integer.TYPE);
//            method.setAccessible(true);
//            return (boolean) method.invoke(sService, pkg, uid);
//        } catch (Exception e) {
//            return true;
//        }
//    }
}
