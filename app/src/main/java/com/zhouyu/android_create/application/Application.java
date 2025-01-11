package com.zhouyu.android_create.application;

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
import com.zhouyu.android_create.tools.utils.NightModeUtils;
import com.zhouyu.android_create.tools.utils.PhoneMessage;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import androidx.documentfile.provider.DocumentFile;

import me.jessyan.autosize.AutoSize;
import me.jessyan.autosize.AutoSizeConfig;

@SuppressLint("StaticFieldLeak,SimpleDateFormat")
public class Application extends android.app.Application {
    public static Handler mainHandler; //主线程
    public boolean isStartActivity = false;
    public int appAlive = -1; //判断app是否被后台杀掉(初始为-1，进入app后为1)
    public int main = -1;
    public static Application application;
    public static Context context; //全局上下文
    // public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");  //用于时间格式化
    public static String appFilesPath;  //app文件目录
    public static String appCachePath;  //app缓冲目录
    public static AudioManager audioManager;
    public static DocumentFile saveFileDocumentFile;
    public static int NightMode = 2; //是否为夜间模式 0为关闭，1为开启,2为跟随系统
    public static boolean  isNightMode = false;
    public static boolean  isNightMode(){
        return NightModeUtils.isDarkTheme();
    }
    public static boolean noFirstOpenAPP = false;

    public static boolean closeOther = false;


    private static final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<>()); //线程池

    public static Runnable executeThread(Runnable runnable){  //提交全局处理任务
//        ConsoleUtils.logErr(threadPool.getPoolSize() + "");
        try {
            threadPool.execute(runnable);
        }catch (Exception ignored){ }
        return runnable;
    }

//    public static long startTime;
    @Override
    public void onCreate() {
//        startTime = System.currentTimeMillis();
        super.onCreate();


        //开启夜间模式
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        application = this;
        mainHandler = new Handler(Looper.getMainLooper());

        context = getApplicationContext();

        appFilesPath = getExternalFilesDir("").getAbsolutePath();
        appCachePath = getDiskCachePath(this);

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        CatchException catchException = CatchException.getInstance();
        catchException.init();

        isPad = false;
        //适配页面
//        ScreenAdaptUtil.setCustomDensity(null,application,false);
        //设置适配默认值，防止一些性能差的机型读取meta-data较慢导致适配初始化失败
//        isPad = isPad(context);
//        if(!isPad){
//
//        }else {
//            AutoSizeConfig.getInstance().setDesignWidthInDp(552).setDesignHeightInDp(552);
//        }
        AutoSizeConfig.getInstance().setDesignWidthInDp(392).setDesignHeightInDp(392);

        //防止头条适配法特殊情况下自启动失败
        if(!AutoSize.checkInit()){
            AutoSize.checkAndInit(this);
        }

        //数据储存库初始化
        Hawk.init(this).build();

        //PRDownloader下载模块初始化
//        PRDownloader.initialize(context);

        //加载图片缓存
//        loadImageCache();

        //设备信息初始化
        PhoneMessage.initMessage(context);

        //个推运营工具初始化
//        GsManager.getInstance().init(context);
    }


    public static Activity getCurrentActivity(){
//        if(app_activity == null){
//            return getCurrentActivity2();
//        }
//        return app_activity;
        return getCurrentActivity2();
    }

    public static Activity getCurrentActivity2 () {
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(
                    null);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);
            Map activities = (Map) activitiesField.get(activityThread);
            for (Object activityRecord : activities.values()) {
                Class activityRecordClass = activityRecord.getClass();
                Field pausedField = activityRecordClass.getDeclaredField("paused");
                pausedField.setAccessible(true);
                if (!pausedField.getBoolean(activityRecord)) {
                    Field activityField = activityRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);
                    Activity activity = (Activity) activityField.get(activityRecord);
                    return activity;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }


//    private void initUpdate() {
//        Beta.initDelay = 1000; //设置启动延时为0s（默认延时3s），APP启动1s后初始化SDK，避免影响APP启动速度;
//        Beta.autoCheckUpgrade = false;
////        Beta.upgradeDialogLayoutId = R.layout.my_dialog;
//        Beta.storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
////            Beta.canShowUpgradeActs.add(MainActivity.class);
////            Beta.canShowUpgradeActs.add(StartActivity.class);
//        Beta.upgradeListener = new UpgradeListener() {
//            @Override
//            public boolean onUpgrade(aa aa, int i, String s) {
//                return false;
//            }
//            @Override
//            public void onUpgrade(int ret, UpgradeInfo strategy, boolean isManual, boolean isSilence) {
//                ConsoleUtils.logErr(strategy == null);
//                if(strategy != null){
//                    ConsoleUtils.logErr(strategy.apkUrl);
//                    long appVersionCode = PhoneMessage.getAppVersionCode();
//                    int versionCode = strategy.versionCode;
//                    if(versionCode > appVersionCode){
//                        Intent i = new Intent();
//                        i.setClass(getApplicationContext(), UpgradeActivity.class);
//                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        Bundle bundle = new Bundle();
//                        bundle.putSerializable("isSilence", isSilence);
//                        bundle.putSerializable("isManual",isManual);
//                        i.putExtras(bundle);
//                        startActivity(i);
//                    }else {
//                        if(isSilence){
//                            Application.mainHandler.post(()-> MyToast.show("已是最新版本",true));
//                        }
//                    }
//                }else {
//                    if(isSilence){
//                        Application.mainHandler.post(()-> MyToast.show("已是最新版本",true));
//                    }
//                }
//            }
//        };
//        Bugly.init(getApplicationContext(), "f4fa80720c", false);
//    }

    public static boolean isPad;
    /**
     * 是否是平板
     *
     * @param context 上下文
     * @return 是平板则返回true，反之返回false
     */
    public static boolean isPad(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();

        display.getMetrics(dm);
        double x = Math.pow(dm.widthPixels / dm.xdpi, 2);
        double y = Math.pow(dm.heightPixels / dm.ydpi, 2);
        double screenInches = Math.sqrt(x + y); // 屏幕尺寸
        return screenInches >= 7.0 || ((context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE);
    }



    //获取APP缓存路径
    public static String getDiskCachePath(Context context) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            return context.getExternalCacheDir().getPath();
        } else {
            return context.getCacheDir().getPath();
        }
    }

    private void loadImageCache() {
        //自定义缓存目录
//        final String imageCacheDir = appCachePath +"/image";
//        OkHttpClient imageClient = new OkHttpClient.Builder()
//                .cache(new Cache(new File(imageCacheDir),50 * 1024 * 1024))
//                .addInterceptor(chain -> {
//                    Request request = chain.request();
//                    String url = request.url().toString();
//
//                    Request.Builder builder = request.newBuilder();
//
//                    //给Picasso添加请求头,用于加载云盘图片
//                    builder.addHeader("cookie", CloudDiskTool.cookie);
//
//                    //如果为云盘图片先获取url
//                    if(url.startsWith("https://downloadsource.quqi.com")){
//                        Map<String, String> urlMap = TextTool.urlParse(url);
//                        String se = urlMap.get("se");
//                        if(!TextTool.isEmpty(se)){
//                            Integer quQi_id = CloudDiskTool.getDirFileID(CloudDiskTool.songListImageDirID, se);
//                            if(quQi_id != null && quQi_id != -1){
//                                String imageUrl = CloudDiskTool.getImageUrl(quQi_id.toString());
//                                if(!TextTool.isEmpty(imageUrl)) {
//                                    builder.url(imageUrl);
//                                }
//                            }
//                        }
//                    }
//                    return chain.proceed(builder.build());
//                }).build();
//
//        Picasso picasso = new Picasso.Builder(this)
//                .downloader(new OkHttp3Downloader(imageClient))
//                .build();
//        Picasso.setSingletonInstance(picasso);
    }


    public static long MAX_MUSIC_CACHE_SIZE = 0;

    public static String getCurrMaxSize(){
        String currMaxCacheSize = "500M";
        if(Application.MAX_MUSIC_CACHE_SIZE == 0){
            currMaxCacheSize = "500M";
        }else if(Application.MAX_MUSIC_CACHE_SIZE == 100 * 1024 * 1024){
            currMaxCacheSize = "100M";
        }else if(Application.MAX_MUSIC_CACHE_SIZE == 500 * 1024 * 1024){
            currMaxCacheSize = "500M";
        }else if(Application.MAX_MUSIC_CACHE_SIZE == 1024L * 1024 * 1024){
            currMaxCacheSize = "1G";
        }else if(Application.MAX_MUSIC_CACHE_SIZE == 2048L * 1024 * 1024){
            currMaxCacheSize = "2G";
        }else if(Application.MAX_MUSIC_CACHE_SIZE == 4096L * 1024 * 1024){
            currMaxCacheSize = "4G";
        }else {
            currMaxCacheSize = (MAX_MUSIC_CACHE_SIZE / 1024 / 1024 / 1024) + "G";
        }
        return currMaxCacheSize;
    }



    /**
     * 作用是检测到手机内存不足，为了防止App被杀掉，可以通过该方法来释放一些资源降低内存
     *
     * TRIM_MEMORY_RUNNING_MODERATE：程序处于前台正常运行，不会被杀掉，但内存有点低，系统开始kill后台的其他进程。
     * TRIM_MEMORY_RUNNING_LOW：程序处于前台正常运行，但当前内存非常低，请释放不必要的资源，不然会影响App响应速度。
     * TRIM_MEMORY_RUNNING_CRITICAL：程序处于前台正常运行，大部分后台进程已经被杀死，请释放不必要的资源。
     * TRIM_MEMORY_UI_HIDDEN：应用从前台切换到后台，回收ui资源
     * TRIM_MEMORY_BACKGROUND：应用在后台运行，处于LRU缓存列表的最近位置，被回收的优先级比较低，可以释放一些资源，让应用在后台存活更长时间。
     * TRIM_MEMORY_MODERATE：应用在后台运行，处于LRU缓存列表的中间位置，如果资源得不到释放，有被系统回收的可能。
     * TRIM_MEMORY_COMPLETE：应用处于后台，处于LRU列表的边缘位置，系统内存严重不足，随时可能被回收，此时尽可能释放掉一切可释放的资源。
     *
     * @param level 对应上面的枚举值
     */
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        switch (level) {
            case TRIM_MEMORY_RUNNING_CRITICAL:
            case TRIM_MEMORY_RUNNING_LOW:
            case TRIM_MEMORY_RUNNING_MODERATE:
            case TRIM_MEMORY_BACKGROUND:
                break;
            case TRIM_MEMORY_UI_HIDDEN:
            case TRIM_MEMORY_MODERATE:
            case TRIM_MEMORY_COMPLETE:
                System.gc();
                break;
        }
    }
}
