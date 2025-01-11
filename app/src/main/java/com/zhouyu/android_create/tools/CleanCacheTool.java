package com.zhouyu.android_create.tools;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import com.zhouyu.android_create.application.Application;
import com.zhouyu.android_create.tools.CustomSys.MyToast;
import com.zhouyu.android_create.tools.imageCache.GlideCacheUtil;
import com.zhouyu.android_create.views.dialog.MySelectDialog;

import java.io.File;

/**
 * 缓存清理
 */
public class CleanCacheTool {

    public float imageSize = 0.00F;
    public float musicSize = 0.00F;
    public float lrcSize = 0.00F;
    public float videoSize = 0.00F;
    public float countSize = 0.00F;
    public boolean isCacheSizeLoadComplete;
    private final Context context = Application.context;

    @SuppressLint("StaticFieldLeak")
    private static CleanCacheTool cleanCacheTool;
    public static synchronized CleanCacheTool getInstance(){
        if(cleanCacheTool == null){
            cleanCacheTool = new CleanCacheTool();
        }
        return cleanCacheTool;
    }

    private final Handler cacheHandler = new Handler(Looper.getMainLooper());
    public void showDialog(Activity activity,TextView textView){
        cacheHandler.post(new Runnable() {
            @Override
            public void run() {
                if(cleanCacheTool.isCacheSizeLoadComplete){
                    //M为单位
                    @SuppressLint("DefaultLocale")
                    String[] items = {"清除图片缓存:"+cleanCacheTool.imageSize+"M","清除音乐缓存:"+cleanCacheTool.musicSize+"M",
                            "清除歌词缓存:"+cleanCacheTool.lrcSize+"M",
                            "清除视频缓存:"+cleanCacheTool.videoSize+"M","全部清除:"+ cleanCacheTool.countSize +"M"};
                    MySelectDialog mySelectDialog = new MySelectDialog(activity,items,-1);
                    mySelectDialog.setTitle("请选择要清除的缓存");
                    mySelectDialog.setItemOnClickListener((index2,item,dialog) -> Application.executeThread(() -> {
                        switch (index2){
                            case 0:
                                FileTool.clearImageCache();
                                GlideCacheUtil.getInstance().clearImageAllCache(context);
                                break;
                            case 1:
                                FileTool.clearMusicCache();
                                break;
                            case 2:
                                FileTool.clearLyricCache();
                                break;
                            case 3:
                                FileTool.clearVideoCache();
                                break;
                            case 4:
                                FileTool.clearImageCache();
                                GlideCacheUtil.getInstance().clearImageAllCache(context);
                                FileTool.clearMusicCache();
                                FileTool.clearLyricCache();
                                FileTool.clearVideoCache();
                                break;
                        }
                        Application.mainHandler.post(() -> {
                            MyToast.show("清除成功，日志缓存可使用系统缓存清理工具清理", Toast.LENGTH_LONG,true);
                            dialog.dismiss();
                            calculateCacheSize(textView);
                        });
                    }));
                    mySelectDialog.show();
                    mySelectDialog.setOnDismissListener(dialog -> {
                        cacheHandler.removeCallbacksAndMessages(null);
                    });
                    return;
                }
                cacheHandler.postDelayed(this,10);
            }
        });
    }

    /**
     * 计算缓存大小
     */
    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    public void calculateCacheSize(TextView textView){
        isCacheSizeLoadComplete = false;
        Application.executeThread(() -> {
            countSize = 0;
            File imageFile = new File(Application.appCachePath +"/image");
            float img_size;
            if(imageFile.exists()){
                img_size = FileTool.getDirectorySize(imageFile);
            }else {
                img_size = 0.00F;
                imageSize = 0.00F;
            }
            img_size += GlideCacheUtil.getInstance().getCacheSize(context);
            imageSize = Float.parseFloat(String.format("%.2f",img_size / 1024 /1024));

            File musicFile = new File(Application.appCachePath +"/music");
            if(musicFile.exists()){
                float size = FileTool.getDirectorySize(musicFile);
                musicSize = Float.parseFloat(String.format("%.2f",size / 1024 /1024));
            }else {
                musicSize = 0.00F;
            }
            File lyricFile = new File(Application.appCachePath +"/lyric");
            if(lyricFile.exists()){
                float size = FileTool.getDirectorySize(lyricFile);
                lrcSize = Float.parseFloat(String.format("%.2f",size / 1024 /1024));
            }else {
                lrcSize = 0.00F;
            }
            File videoFile = new File(Application.appCachePath +"/video");
            if(videoFile.exists()){
                float size = FileTool.getDirectorySize(videoFile);
                videoSize = Float.parseFloat(String.format("%.2f",size / 1024 /1024));
            }else {
                videoSize = 0.00F;
            }

            countSize = Float.parseFloat(String.format("%.2f",musicSize + imageSize + lrcSize + videoSize));
            Application.mainHandler.post(() -> {
                if(textView != null){
                    textView.setText((cleanCacheTool.countSize)+"M");
                }
                isCacheSizeLoadComplete = true;
            });
        });
    }
}
