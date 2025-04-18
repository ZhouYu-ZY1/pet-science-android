package com.zhouyu.pet_science.tools.image_cache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.zhouyu.pet_science.application.Application;
import com.zhouyu.pet_science.tools.encoding.EncodingTool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * 三级缓存之本地缓存
 */
public class LocalCacheUtils {

    private static final String CACHE_PATH = Application.appCachePath +"/music_image";

    /**
     * 从本地读取图片
     */
    public Bitmap getBitmapFromLocal(String url){
        try {
            String fileName = EncodingTool.Md5.md5Hex(url) + ".jpg";
            File file = new File(CACHE_PATH,fileName);
            if(file.exists()){
                return BitmapFactory.decodeStream(new FileInputStream(file));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 从网络获取图片后,保存至本地缓存
     */
    public void setBitmapToLocal(String url,Bitmap bitmap){
        try {
            String fileName = EncodingTool.Md5.md5Hex(url) + ".jpg";;
            File file = new File(CACHE_PATH,fileName);
            //通过得到文件的父文件,判断父文件是否存在
            File parentFile = file.getParentFile();
            if (!parentFile.exists()){
                parentFile.mkdirs();
            }

            //把图片保存至本地
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,new FileOutputStream(file));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
