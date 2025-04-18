package com.zhouyu.pet_science.tools.image_cache;

import android.graphics.Bitmap;
import android.util.LruCache;

import java.lang.ref.SoftReference;

/**
 * 三级缓存之内存缓存
 */
@SuppressWarnings("JavaDoc")
public class MemoryCacheUtils {

    // private HashMap<String,Bitmap> mMemoryCache=new HashMap<>();//1.因为强引用,容易造成内存溢出，所以考虑使用下面弱引用的方法
    // private HashMap<String, SoftReference<Bitmap>> mMemoryCache = new HashMap<>();//2.因为在Android2.3+后,系统会优先考虑回收弱引用对象,官方提出使用LruCache
    private final LruCache<String,Bitmap> mMemoryCache;

    private static MemoryCacheUtils memoryCacheUtils;

    public static MemoryCacheUtils getMemoryCacheUtils(){
        if(memoryCacheUtils == null){
            memoryCacheUtils = new MemoryCacheUtils();
        }
        return memoryCacheUtils;
    }

    private MemoryCacheUtils(){
        long maxMemory = Runtime.getRuntime().maxMemory()/8;//得到手机最大允许内存的1/8,即超过指定内存,则开始回收
        //需要传入允许的内存最大值,虚拟机默认内存16M,真机不一定相同
        mMemoryCache=new LruCache<String,Bitmap>((int) maxMemory){
            //用于计算每个条目的大小
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount();
            }
        };

    }

    /**
     * 从内存中读图片
     * @param url
     */
    @SuppressWarnings("JavaDoc")
    public Bitmap getBitmapFromMemory(String url) {
        try {
            SoftReference<Bitmap> bitmapSoftReference = new SoftReference<>(mMemoryCache.get(url));
            return bitmapSoftReference.get();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        //Bitmap bitmap = mMemoryCache.get(url);//1.
            //弱引用方法

        // 强引用方法
 /*       Bitmap bitmap = null;
        try {
            bitmap = mMemoryCache.get(url);
        }catch (Exception e){
            e.printStackTrace();
        }*/

    }

    /**
     * 往内存中写图片
     * @param url
     * @param bitmap
     */
    public void setBitmapToMemory(String url, Bitmap bitmap) {
        //mMemoryCache.put(url, bitmap);//1.强引用方法
            /*2.弱引用方法
            mMemoryCache.put(url, new SoftReference<>(bitmap));
            */
        try {
            mMemoryCache.put(url,bitmap);
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }
}
