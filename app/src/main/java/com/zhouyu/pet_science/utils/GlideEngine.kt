package com.zhouyu.pet_science.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.huantansheng.easyphotos.engine.ImageEngine
import com.zhouyu.pet_science.application.Application

/**
 * Glide4.x的加载图片引擎实现,单例模式
 * Glide4.x的缓存机制更加智能，已经达到无需配置的境界。如果使用Glide3.x，需要考虑缓存机制。
 * Created by huan on 2018/1/15.
 */
class GlideEngine  //单例模式，私有构造方法
private constructor() : ImageEngine {
    /**
     * 加载图片到ImageView
     *
     * @param context   上下文
     * @param uri 图片路径Uri
     * @param imageView 加载到的ImageView
     */
    //安卓10推荐uri，并且path的方式不再可用
    override fun loadPhoto(context: Context, uri: Uri, imageView: ImageView) {
        Glide.with(context).load(uri).transition(DrawableTransitionOptions.withCrossFade())
            .into(imageView)
    }

    /**
     * 加载gif动图图片到ImageView，gif动图不动
     *
     * @param context   上下文
     * @param gifUri   gif动图路径Uri
     * @param imageView 加载到的ImageView
     *
     *
     * 备注：不支持动图显示的情况下可以不写
     */
    //安卓10推荐uri，并且path的方式不再可用
    override fun loadGifAsBitmap(context: Context, gifUri: Uri, imageView: ImageView) {
        Glide.with(context).asBitmap().load(gifUri).into(imageView)
    }

    /**
     * 加载gif动图到ImageView，gif动图动
     *
     * @param context   上下文
     * @param gifUri   gif动图路径Uri
     * @param imageView 加载动图的ImageView
     *
     *
     * 备注：不支持动图显示的情况下可以不写
     */
    //安卓10推荐uri，并且path的方式不再可用
    override fun loadGif(context: Context, gifUri: Uri, imageView: ImageView) {
        Glide.with(context).asGif().load(gifUri)
            .transition(DrawableTransitionOptions.withCrossFade()).into(imageView)
    }

    /**
     * 获取图片加载框架中的缓存Bitmap
     *
     * @param context 上下文
     * @param uri    图片路径
     * @param width   图片宽度
     * @param height  图片高度
     * @return Bitmap
     * @throws Exception 异常直接抛出，EasyPhotos内部处理
     */
    //安卓10推荐uri，并且path的方式不再可用
    @Throws(Exception::class)
    override fun getCacheBitmap(context: Context, uri: Uri, width: Int, height: Int): Bitmap {
        return Glide.with(context).asBitmap().load(uri).submit(width, height).get()
    }

    @Throws(Exception::class)
    fun getCacheBitmap(context: Context, url: String, width: Int, height: Int): Bitmap {
        return getCacheBitmap(context, Uri.parse(url), width, height)
    }

    /**
     * 使用 Glide 加载视频封面
     * @param videoPath 视频的路径或 URL
     */
    fun getVideoBitmap(videoPath: String?): Bitmap? {
        return try {
            Glide.with(Application.context)
                .asBitmap()
                .load(videoPath) //                    .skipMemoryCache(true) // 禁用内存缓存
                .diskCacheStrategy(DiskCacheStrategy.NONE) // 禁用磁盘缓存
                .submit().get()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    companion object {
        //单例
        private var inst: GlideEngine? = null
        val instance: GlideEngine
            //获取单例
            get() {
                if (null == inst) {
                    synchronized(GlideEngine::class.java) {
                        if (null == inst) {
                            inst = GlideEngine()
                        }
                    }
                }
                return inst!!
            }
    }
}
