package com.zhouyu.pet_science.utils

import android.content.Context
import android.os.Looper
import android.text.TextUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.cache.ExternalPreferredCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.zhouyu.pet_science.application.Application
import java.io.File

/**
 * Glide缓存工具类
 */
class GlideCacheUtil {
    /**
     * 清除图片磁盘缓存
     */
    fun clearImageDiskCache(context: Context?) {
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                Application.executeThread {
                    Glide.get(
                        context!!
                    ).clearDiskCache()
                }
            } else {
                Glide.get(context!!).clearDiskCache()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 清除图片内存缓存
     */
    fun clearImageMemoryCache(context: Context?) {
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) { //只能在主线程执行
                Glide.get(context!!).clearMemory()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 清除图片所有缓存
     */
    fun clearImageAllCache(context: Context) {
        clearImageDiskCache(context)
        clearImageMemoryCache(context)
        val ImageExternalCatchDir =
            context.externalCacheDir.toString() + ExternalPreferredCacheDiskCacheFactory.DEFAULT_DISK_CACHE_DIR
        deleteFolderFile(ImageExternalCatchDir, true)
    }

    /**
     * 获取Glide造成的缓存大小
     * @return CacheSize
     */
    fun getCacheSize(context: Context): Long {
        return getFolderSize(File(context.cacheDir.toString() + "/" + InternalCacheDiskCacheFactory.DEFAULT_DISK_CACHE_DIR))
    }

    /**
     * 获取指定文件夹内所有文件大小的和
     *
     * @param file file
     * @return size
     */
    private fun getFolderSize(file: File): Long {
        var size: Long = 0
        try {
            val fileList = file.listFiles()
            if (fileList != null && fileList.size > 0) {
                for (aFileList in fileList) {
                    size = if (aFileList.isDirectory) {
                        size + getFolderSize(aFileList)
                    } else {
                        size + aFileList.length()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return size
    }

    /**
     * 删除指定目录下的文件，这里用于缓存的删除
     *
     * @param filePath filePath
     * @param deleteThisPath deleteThisPath
     */
    private fun deleteFolderFile(filePath: String, deleteThisPath: Boolean) {
        if (!TextUtils.isEmpty(filePath)) {
            try {
                val file = File(filePath)
                if (file.isDirectory) {
                    val files = file.listFiles()
                    for (file1 in files) {
                        deleteFolderFile(file1.absolutePath, true)
                    }
                }
                if (deleteThisPath) {
                    if (!file.isDirectory) {
                        file.delete()
                    } else {
                        if (file.listFiles().size == 0) {
                            file.delete()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    } //    /**

    //     * 格式化单位
    //     *
    //     * @param size size
    //     * @return size
    //     */
    //    private static String getFormatSize(double size) {
    //
    //        double kiloByte = size / 1024;
    //        if (kiloByte < 1) {
    //            return size + "Byte";
    //        }
    //        double megaByte = kiloByte / 1024;
    //        if (megaByte < 1) {
    //            BigDecimal result1 = new BigDecimal(Double.toString(kiloByte));
    //            return result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "KB";
    //        }
    //        double gigaByte = megaByte / 1024;
    //        if (gigaByte < 1) {
    //            BigDecimal result2 = new BigDecimal(Double.toString(megaByte));
    //            return result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "MB";
    //        }
    //        double teraBytes = gigaByte / 1024;
    //        if (teraBytes < 1) {
    //            BigDecimal result3 = new BigDecimal(Double.toString(gigaByte));
    //            return result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "GB";
    //        }
    //        BigDecimal result4 = new BigDecimal(teraBytes);
    //        return result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString() + "TB";
    //    }
    companion object {
        private var inst: GlideCacheUtil? = null
        val instance: GlideCacheUtil
            get() {
                if (inst == null) {
                    inst = GlideCacheUtil()
                }
                return inst!!
            }
    }
}
