package com.zhouyu.pet_science.tools.utils;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.zhouyu.pet_science.application.Application;

public class PhoneMessage {
    public static int statusBarHeight; //状态栏高度
//    public static float scale; //px和dp转换比例

    //复制
    public static void copy(String data) {
        // 获取系统剪贴板
        ClipboardManager clipboard = (ClipboardManager) Application.context.getSystemService(Context.CLIPBOARD_SERVICE);
        // 创建一个剪贴数据集，包含一个普通文本数据条目（需要复制的数据）,其他的还有
        // newHtmlText、
        // newIntent、
        // newUri、
        // newRawUri
        ClipData clipData = ClipData.newPlainText(null, data);

        // 把数据集设置（复制）到剪贴板
        clipboard.setPrimaryClip(clipData);
    }

    /**
     * （功能：如果没有连接耳机，当声音超过一定的限度时，提示是否继续播放歌曲）
     *
     * 判断当前是否连接耳机
     */
    public static Boolean  isConnectHeadset(){
        boolean isWired; //有线耳机是否连接
        boolean isBluetooth; //蓝牙耳机是否连接
        AudioManager  mAudioManager = (AudioManager) Application.context.getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        //获取当前使用的麦克风，设置媒体播放麦克风
        isWired = mAudioManager.isWiredHeadsetOn();
        
        if (BluetoothProfile.STATE_CONNECTED == BluetoothAdapter.getDefaultAdapter().getProfileConnectionState(BluetoothProfile.HEADSET)) {
            //蓝牙设备已连接，声音内放，从蓝牙设备输出
            isBluetooth = true;
        } else if (BluetoothProfile.STATE_DISCONNECTED == BluetoothAdapter.getDefaultAdapter().getProfileConnectionState(BluetoothProfile.HEADSET)) {
            //蓝牙设备未连接，声音外放，
            isBluetooth = false;
        } else {
            //蓝牙设备未连接，声音外放，
            isBluetooth = false;
        }
        return isWired || isBluetooth;
    }

    /**
     * 初始化
     * @param context 上下文
     */
    public static void initMessage(Context context){
//        scale  = context.getResources().getDisplayMetrics().density;
        statusBarHeight = getStatusBarHeight(context);
    }


    public static int getWidthPixels(){
        return Application.context.getResources().getDisplayMetrics().widthPixels;
    }
    public static int getHeightPixels(){
        return Application.context.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * 获取状态栏高度
     * @param context 上下文
     * @return 状态栏高度，px值
     */
    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }

    /**
     * dp转px
     * @param dpValue dp值
     * @return px值
     */
    public static int dpToPx(float dpValue) {
        return (int) (dpValue * Application.context.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static float dpToPxFloat(float dpValue) {
        return dpValue * Application.context.getResources().getDisplayMetrics().density;
    }

    /**
     * px转dp
     * @param pxValue px值
     * @return dp值
     */
    public static int pxToDp(float pxValue) {
        return (int) (pxValue / Application.context.getResources().getDisplayMetrics().density + 0.5f);
    }

    /**
     * 判断是否已经连接网络
     */
    public static boolean isConnectNetwork(Context context){
        ConnectivityManager mConnectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //检查网络连接
        NetworkInfo info = mConnectivity.getActiveNetworkInfo();
        return info != null;
    }

    /**
     * 判断网络类型(false为流量，true为wifi)
     */
    public static boolean getNetworkType(Context context){
        ConnectivityManager mConnectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //检查网络连接
        NetworkInfo info = mConnectivity.getActiveNetworkInfo();
        int netType = info.getType();
        int netSubtype = info.getSubtype();

        if (netType == ConnectivityManager.TYPE_WIFI) {  //WIFI
            return info.isConnected();
        } else if (netType == ConnectivityManager.TYPE_MOBILE && netSubtype == TelephonyManager.NETWORK_TYPE_UMTS) {   //MOBILE
            return info.isConnected();
        } else {
            return false;
        }
    }

//    /**
//     * 判断WiFi和移动流量是否已连接
//     */
//    public static boolean checkNetworkConnection(Context context) {
//        final ConnectivityManager connMgr = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
//
//        final NetworkInfo wifi =connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//        final NetworkInfo mobile =connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
//
//        //getState()方法是查询是否连接了数据网络
//        return wifi.isAvailable() || mobile.isAvailable();
//    }

    /**
     * 获取当前app version code
     */
    public static long getAppVersionCode() {
        long appVersionCode = 0;
        try {
            PackageInfo packageInfo = Application.context.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(Application.context.getPackageName(), 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                appVersionCode = packageInfo.getLongVersionCode();
            } else {
                appVersionCode = packageInfo.versionCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appVersionCode;
    }

    /**
     * 获取当前app version name
     */
    public static String getAppVersionName() {
        String appVersionName = "";
        try {
            PackageInfo packageInfo = Application.context.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(Application.context.getPackageName(), 0);
            appVersionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("", e.getMessage());
        }
        return appVersionName;
    }

    /**
     * 获取应用包名
     */
    public static synchronized String getPackageName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            return packageInfo.packageName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 检查是否安卓某个软件（包名）
     */
    public static boolean isAppInstalled(String packageName) {
        try {
            Application.context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * 获取软件签名
     */
    public static String getSign(){
        try {
            @SuppressLint("PackageManagerGetSignatures")
            PackageInfo packageInfo= Application.context.getPackageManager().getPackageInfo(Application.context.getPackageName(), PackageManager.GET_SIGNATURES);
            Signature[] signatures=packageInfo.signatures;
            StringBuilder builder=new StringBuilder();
            for (Signature signature:signatures){
                builder.append(signature.toCharsString());
            }
            return builder.toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return "";
    }
}
