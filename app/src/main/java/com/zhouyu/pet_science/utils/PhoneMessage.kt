package com.zhouyu.pet_science.utils

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothProfile
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import com.zhouyu.pet_science.application.Application

object PhoneMessage {
    @JvmField
    var statusBarHeight = 0 //状态栏高度

    //    public static float scale; //px和dp转换比例
    //复制
    fun copy(data: String?) {
        // 获取系统剪贴板
        val clipboard =
            Application.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        // 创建一个剪贴数据集，包含一个普通文本数据条目（需要复制的数据）,其他的还有
        // newHtmlText、
        // newIntent、
        // newUri、
        // newRawUri
        val clipData = ClipData.newPlainText(null, data)

        // 把数据集设置（复制）到剪贴板
        clipboard.setPrimaryClip(clipData)
    }

    /**
     * 初始化
     * @param context 上下文
     */
    @JvmStatic
    fun initMessage(context: Context) {
//        scale  = context.getResources().getDisplayMetrics().density;
        statusBarHeight = getStatusBarHeight(context)
    }

    @JvmStatic
    val widthPixels: Int
        get() = Application.context.resources.displayMetrics.widthPixels
    @JvmStatic
    val heightPixels: Int
        get() = Application.context.resources.displayMetrics.heightPixels

    /**
     * 获取状态栏高度
     * @param context 上下文
     * @return 状态栏高度，px值
     */
    fun getStatusBarHeight(context: Context): Int {
        val resources = context.resources
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return resources.getDimensionPixelSize(resourceId)
    }

    /**
     * dp转px
     * @param dpValue dp值
     * @return px值
     */
    @JvmStatic
    fun dpToPx(dpValue: Float): Int {
        return (dpValue * Application.context.resources.displayMetrics.density + 0.5f).toInt()
    }

    @JvmStatic
    fun dpToPxFloat(dpValue: Float): Float {
        return dpValue * Application.context.resources.displayMetrics.density
    }

    /**
     * px转dp
     * @param pxValue px值
     * @return dp值
     */
    fun pxToDp(pxValue: Float): Int {
        return (pxValue / Application.context.resources.displayMetrics.density + 0.5f).toInt()
    }

    /**
     * 判断是否已经连接网络
     */
    fun isConnectNetwork(context: Context): Boolean {
        val mConnectivity =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        //检查网络连接
        val info = mConnectivity.activeNetworkInfo
        return info != null
    }

    /**
     * 判断网络类型(false为流量，true为wifi)
     */
    fun getNetworkType(context: Context): Boolean {
        val mConnectivity =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        //检查网络连接
        val info = mConnectivity.activeNetworkInfo
        val netType = info!!.type
        val netSubtype = info.subtype
        return if (netType == ConnectivityManager.TYPE_WIFI) {  //WIFI
            info.isConnected
        } else if (netType == ConnectivityManager.TYPE_MOBILE && netSubtype == TelephonyManager.NETWORK_TYPE_UMTS) {   //MOBILE
            info.isConnected
        } else {
            false
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
    val appVersionCode: Long
        /**
         * 获取当前app version code
         */
        get() {
            var appVersionCode: Long = 0
            try {
                val packageInfo = Application.context.applicationContext
                    .packageManager
                    .getPackageInfo(Application.context.packageName, 0)
                appVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode
                } else {
                    packageInfo.versionCode.toLong()
                }
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            return appVersionCode
        }
    val appVersionName: String?
        /**
         * 获取当前app version name
         */
        get() {
            var appVersionName = ""
            try {
                val packageInfo = Application.context.applicationContext
                    .packageManager
                    .getPackageInfo(Application.context.packageName, 0)
                appVersionName = packageInfo.versionName!!
            } catch (e: PackageManager.NameNotFoundException) {
                Log.e("", e.message!!)
            }
            return appVersionName
        }

    /**
     * 获取应用包名
     */
    @Synchronized
    fun getPackageName(context: Context): String? {
        try {
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(
                context.packageName, 0
            )
            return packageInfo.packageName
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 检查是否安卓某个软件（包名）
     */
    fun isAppInstalled(packageName: String?): Boolean {
        return try {
            Application.context.packageManager.getPackageInfo(
                packageName!!, PackageManager.GET_ACTIVITIES
            )
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    val sign: String
        /**
         * 获取软件签名
         */
        get() {
            try {
                @SuppressLint("PackageManagerGetSignatures") val packageInfo =
                    Application.context.packageManager.getPackageInfo(
                        Application.context.packageName, PackageManager.GET_SIGNATURES
                    )
                val signatures = packageInfo.signatures
                val builder = StringBuilder()
                for (signature in signatures!!) {
                    builder.append(signature.toCharsString())
                }
                return builder.toString()
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            return ""
        }
}
