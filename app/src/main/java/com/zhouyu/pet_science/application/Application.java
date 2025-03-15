package com.zhouyu.pet_science.application;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;


//import com.getui.gs.sdk.GsManager;
import com.orhanobut.hawk.Hawk;
import com.zhouyu.pet_science.tools.utils.NightModeUtils;
import com.zhouyu.pet_science.tools.utils.PhoneMessage;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import androidx.documentfile.provider.DocumentFile;

import me.jessyan.autosize.AutoSize;
import me.jessyan.autosize.AutoSizeConfig;

@SuppressLint("StaticFieldLeak,SimpleDateFormat")
public class Application extends android.app.Application {
    public static Handler mainHandler; //主线程
    public static Application application;
    public static Context context; //全局上下文
    // public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");  //用于时间格式化
    public static String appFilesPath;  //app文件目录
    public static String appCachePath;  //app缓冲目录
    public static AudioManager audioManager;
    public static int NightMode = 2; //是否为夜间模式 0为关闭，1为开启,2为跟随系统
    public static boolean  isNightMode = false;
    public static boolean  isNightMode(){
        return NightModeUtils.isDarkTheme();
    }


    private static final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<>()); //线程池

    public static Runnable executeThread(Runnable runnable){  //提交全局处理任务
        try {
            threadPool.execute(runnable);
        }catch (Exception ignored){ }
        return runnable;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        mainHandler = new Handler(Looper.getMainLooper());

        context = getApplicationContext();

        appFilesPath = Objects.requireNonNull(getExternalFilesDir("")).getAbsolutePath();
        appCachePath = getDiskCachePath(this);

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        CatchException catchException = CatchException.getInstance();
        catchException.init();
        //适配页面
//        ScreenAdaptUtil.setCustomDensity(null,application,false);
        //设置适配默认值，防止一些性能差的机型读取meta-data较慢导致适配初始化失败
        AutoSizeConfig.getInstance().setDesignWidthInDp(392).setDesignHeightInDp(392);

        //防止头条适配法特殊情况下自启动失败
        if(!AutoSize.checkInit()){
            AutoSize.checkAndInit(this);
        }

        //数据储存库初始化
        Hawk.init(this).build();

        //设备信息初始化
        PhoneMessage.initMessage(context);

        //个推运营工具初始化
//        GsManager.getInstance().init(context);
    }



    //获取APP缓存路径
    public static String getDiskCachePath(Context context) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            return Objects.requireNonNull(context.getExternalCacheDir()).getPath();
        } else {
            return context.getCacheDir().getPath();
        }
    }

}
