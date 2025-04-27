package com.zhouyu.pet_science.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import com.zhouyu.pet_science.application.Application
import com.zhouyu.pet_science.utils.FileUtils.clearImageCache
import com.zhouyu.pet_science.utils.FileUtils.clearLyricCache
import com.zhouyu.pet_science.utils.FileUtils.clearMusicCache
import com.zhouyu.pet_science.utils.FileUtils.clearVideoCache
import com.zhouyu.pet_science.utils.FileUtils.getDirectorySize
import com.zhouyu.pet_science.utils.MyToast.Companion.show
import com.zhouyu.pet_science.views.dialog.MySelectDialog
import java.io.File

/**
 * 缓存清理
 */
class CleanCacheUtils {
    var imageSize = 0.00f
    var musicSize = 0.00f
    var lrcSize = 0.00f
    var videoSize = 0.00f
    var countSize = 0.00f
    var isCacheSizeLoadComplete = false
    private val context = Application.context
    private val cacheHandler = Handler(Looper.getMainLooper())
    fun showDialog(activity: Activity?, textView: TextView?) {
        cacheHandler.post(object : Runnable {
            override fun run() {
                if (cleanCacheTool!!.isCacheSizeLoadComplete) {
                    //M为单位
                    @SuppressLint("DefaultLocale") val items = arrayOf(
                        "清除图片缓存:" + cleanCacheTool!!.imageSize + "M",
                        "清除视频缓存:" + cleanCacheTool!!.videoSize + "M",
                        "全部清除:" + cleanCacheTool!!.countSize + "M"
                    )
                    val mySelectDialog = MySelectDialog(activity!!, items, -1)
                    mySelectDialog.setTitle("请选择要清除的缓存")
                    mySelectDialog.setItemOnClickListener { index2: Int, item: String?, dialog: MySelectDialog ->
                        Application.executeThread {
                            when (index2) {
                                0 -> {
                                    clearImageCache()
                                    GlideCacheUtil.instance.clearImageAllCache(context)
                                }

                                1 -> clearVideoCache()
                                2 -> {
                                    clearImageCache()
                                    GlideCacheUtil.instance.clearImageAllCache(context)
                                    clearMusicCache()
                                    clearLyricCache()
                                    clearVideoCache()
                                }
                            }
                            Application.mainHandler.post {
                                show("清除成功", Toast.LENGTH_LONG, true)
                                dialog.dismiss()
                                calculateCacheSize(textView)
                            }
                        }
                    }
                    mySelectDialog.show()
                    mySelectDialog.setOnDismissListener { dialog: DialogInterface? ->
                        cacheHandler.removeCallbacksAndMessages(
                            null
                        )
                    }
                    return
                }
                cacheHandler.postDelayed(this, 10)
            }
        })
    }

    /**
     * 计算缓存大小
     */
    @SuppressLint("SetTextI18n", "DefaultLocale")
    fun calculateCacheSize(textView: TextView?) {
        isCacheSizeLoadComplete = false
        Application.executeThread {
            countSize = 0f
            val imageFile = File(Application.appCachePath + "/image")
            var img_size: Float
            if (imageFile.exists()) {
                img_size = getDirectorySize(imageFile).toFloat()
            } else {
                img_size = 0.00f
                imageSize = 0.00f
            }
            img_size += GlideCacheUtil.instance.getCacheSize(context).toFloat()
            imageSize = String.format("%.2f", img_size / 1024 / 1024).toFloat()
            val musicFile = File(Application.appCachePath + "/music")
            musicSize = if (musicFile.exists()) {
                val size = getDirectorySize(musicFile).toFloat()
                String.format("%.2f", size / 1024 / 1024).toFloat()
            } else {
                0.00f
            }
            val lyricFile = File(Application.appCachePath + "/lyric")
            lrcSize = if (lyricFile.exists()) {
                val size = getDirectorySize(lyricFile).toFloat()
                String.format("%.2f", size / 1024 / 1024).toFloat()
            } else {
                0.00f
            }
            val videoFile = File(Application.appCachePath + "/video")
            videoSize = if (videoFile.exists()) {
                val size = getDirectorySize(videoFile).toFloat()
                String.format("%.2f", size / 1024 / 1024).toFloat()
            } else {
                0.00f
            }
            countSize = String.format("%.2f", musicSize + imageSize + lrcSize + videoSize).toFloat()
            Application.mainHandler.post {
                if (textView != null) {
                    textView.text = "（" + cleanCacheTool!!.countSize + "M" + "）"
                }
                isCacheSizeLoadComplete = true
            }
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var cleanCacheTool: CleanCacheUtils? = null

        @get:Synchronized
        val instance: CleanCacheUtils
            get() {
                if (cleanCacheTool == null) {
                    cleanCacheTool = CleanCacheUtils()
                }
                return cleanCacheTool!!
            }
    }
}
