package com.zhouyu.pet_science.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import com.zhouyu.pet_science.application.Application
import java.io.BufferedWriter
import java.io.EOFException
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets

object FileUtils {

    /**
     * 获取Uri对应的真实路径
     */
    fun getRealPathFromURI(uri: Uri, contextWrapper: ContextWrapper): File? {
        val returnCursor = contextWrapper.contentResolver.query(uri, null, null, null, null)
        returnCursor?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
//                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
//                val size = cursor.getLong(sizeIndex).toString()
                val name = cursor.getString(nameIndex)
                val file = File(contextWrapper.cacheDir, name)
                try {
                    val inputStream = contextWrapper.contentResolver.openInputStream(uri)
                    val outputStream = FileOutputStream(file)
                    inputStream?.use { input ->
                        outputStream.use { output ->
                            val buffer = ByteArray(4 * 1024) // 4k buffer
                            var read: Int
                            while (input.read(buffer).also { read = it } != -1) {
                                output.write(buffer, 0, read)
                            }
                            output.flush()
                        }
                    }
                    return file
                } catch (e: Exception) {
                    e.printStackTrace()
                    file.delete() // 如果复制失败，删除临时文件
                }
            }
        }
        return null
    }

    /**
     * bitmap保存图片
     * @param bitmap bitmap参数
     * @param quality 压缩率
     * @param path 保存路径
     */
    @JvmOverloads
    fun BitmapToImage(
        bitmap: Bitmap,
        quality: Int,
        path: String,
        isDocument: Boolean,
        type: String? = "image/jpeg"
    ): Boolean {
        return try {
            val bitmapFile = File(path)
            val file = File(path.substring(0, path.lastIndexOf("/")))
            if (!file.exists()) {
                val mkdirs = file.mkdirs()
                if (!mkdirs) {
                    return false
                }
            }
            val out: OutputStream?
            val name = bitmapFile.name
            out = if (isDocument) {
                val documentFile = DocumentFile.fromFile(bitmapFile)
                if (documentFile.exists()) {
                    documentFile.delete()
                }
                documentFile.createFile(type!!, name)
                Application.context.contentResolver.openOutputStream(documentFile.uri)
            } else {
                FileOutputStream(bitmapFile)
            }
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out!!)) {
                out.flush()
                out.close()
                if (isDocument) {
                    //保存图片后发送广播通知更新数据库
                    Application.context.sendBroadcast(
                        Intent(
                            Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                            Uri.fromFile(bitmapFile)
                        )
                    )
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 读取Assets目录下的图片文件
     * @param context 上下文
     * @param fileName 路径
     */
    fun readAssetsImageFile(context: Context, fileName: String?): Bitmap? {
        var bitmap: Bitmap? = null
        val assetManager = context.assets
        try {
            val inputStream = assetManager.open(fileName!!)
            bitmap = BitmapFactory.decodeStream(inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bitmap
    }

    /**
     * 清理音乐图片缓存
     */
    @JvmStatic
    fun clearImageCache() {
        val file = File(Application.appCachePath + "/image")
        if (file.exists() && file.isDirectory) {
            val files = file.listFiles()
            if (files != null) {
                for (f in files) {
                    f.delete()
                }
                file.delete()
            }
        }
        clearLocalMusicImageCache()
    }

    fun clearLocalMusicImageCache() {
        val file = File(Application.appCachePath + "/music_image")
        if (file.exists() && file.isDirectory) {
            val files = file.listFiles()
            for (f in files) {
                f.delete()
            }
            file.delete()
        }
    }

    /**
     * 清除音乐缓存
     */
    @JvmStatic
    fun clearMusicCache() {
        val file = File(Application.appCachePath + "/music")
        if (file.exists() && file.isDirectory) {
            val files = file.listFiles()
            for (f in files) {
                f.delete()
            }
            file.delete()
        }
        StorageUtils.delete("matchMusicMap")
    }

    fun removeDirectoryAllFile(file: File) {
        if (file.exists()) {
            if (file.isDirectory) {
                val files = file.listFiles()
                for (file_c in files) {
                    if (file_c.isDirectory) {
                        removeDirectoryAllFile(file_c)
                    } else {
                        file_c.delete()
                    }
                }
            }
            file.delete()
        }
    }

    /**
     * 清除视频缓存
     */
    @JvmStatic
    fun clearLyricCache() {
        val file = File(Application.appCachePath + "/lyric")
        if (file.exists() && file.isDirectory) {
            val files = file.listFiles()
            for (f in files) {
                f.delete()
            }
            file.delete()
        }
    }

    /**
     * 清除视频缓存
     */
    @JvmStatic
    fun clearVideoCache() {
        val file = File(Application.appCachePath + "/video")
        if (file.exists() && file.isDirectory) {
            val files = file.listFiles()
            for (f in files) {
                f.delete()
            }
            file.delete()
        }
    }

    /**
     * 获取目录大小(字节)
     */
    @JvmStatic
    fun getDirectorySize(file: File): Long {
        return if (file.exists()) {
            if (file.isDirectory) {
                countSize = 0
                sizeOfDirectory(file)
                countSize
            } else {
                file.length()
            }
        } else {
            0
        }
    }

    private var countSize: Long = 0
    private fun sizeOfDirectory(file: File) {
        val file_list = file.listFiles()
        if (file_list != null && file_list.size != 0) {
            for (list_file in file_list) {
                if (list_file.isDirectory) {
                    sizeOfDirectory(list_file)
                } else {
                    countSize += list_file.length()
                }
            }
        }
    }

    @JvmStatic
    @SuppressLint("DefaultLocale")
    fun longToMBStr(size: Long): String {
        return String.format("%.1f", size / 1024f / 1024) + "M"
    }

    @JvmStatic
    fun getFileNameWithoutSuffix(fileName: String): String {
        return try {
            fileName.substring(0, fileName.lastIndexOf("."))
        } catch (e: Exception) {
            e.printStackTrace()
            fileName
        }
    }

    //复制文件
    @Throws(IOException::class)
    fun copyFileUsingStream(source: File?, dest: File?) {
        var `is`: InputStream? = null
        var os: OutputStream? = null
        try {
            `is` = FileInputStream(source)
            os = FileOutputStream(dest)
            val buffer = ByteArray(1024)
            var length: Int
            while (`is`.read(buffer).also { length = it } > 0) {
                os.write(buffer, 0, length)
            }
        } finally {
            `is`?.close()
            os?.close()
        }
    }

    /**
     * 文件的复制操作方法
     * @param fromFile 准备复制的文件
     * @param toFile 复制路径
     */
    fun copyFile(fromFile: File, toFile: File) {
        if (fromFile.path == toFile.path) {
            return
        }
        if (!fromFile.exists()) {
            return
        }
        if (!fromFile.isFile) {
            return
        }
        if (!fromFile.canRead()) {
            return
        }
        if (!toFile.parentFile.exists()) {
            toFile.parentFile.mkdirs()
        }
        if (toFile.exists()) {
            toFile.delete()
        }
        try {
            val fosfrom = FileInputStream(fromFile)
            val fosto = FileOutputStream(toFile)
            val bt = ByteArray(1024)
            var c: Int
            while (fosfrom.read(bt).also { c = it } > 0) {
                fosto.write(bt, 0, c)
            }
            //关闭输入、输出流
            fosfrom.close()
            fosto.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //File转Uri
    fun getUriForFile(context: Context?, file: File?): Uri {
        if (context == null || file == null) {
            throw NullPointerException()
        }
        val uri: Uri
        uri = if (Build.VERSION.SDK_INT >= 24) {
            FileProvider.getUriForFile(
                context.applicationContext,
                "com.zhouyu.pet_science.fileprovider",
                file
            )
        } else {
            Uri.fromFile(file)
        }
        return uri
    }

    /**
     * 对象输入输出流
     */
    object ObjectStream {
        fun writeObjectToFile(obj: Any?, path: String) {
            val file = File(path)
            try {
                val directoryFile = File(path.substring(0, path.lastIndexOf("/")))
                if (!directoryFile.exists()) {
                    val mkdirs = directoryFile.mkdirs()
                    if (!mkdirs) {
                        return
                    }
                }
                val outputStream = FileOutputStream(file)
                val objOut = ObjectOutputStream(outputStream)
                objOut.writeObject(obj)
                objOut.flush()
                objOut.close()
            } catch (e: IOException) {
                Log.e("main", "存储失败")
                e.printStackTrace()
            }
        }

        fun <T> readObjectFromFile(path: String?): T? {
            var temp: T? = null
            val file = File(path)
            try {
                val inputStream = FileInputStream(file)
                val objIn = ObjectInputStream(inputStream)
                temp = objIn.readObject() as T
                objIn.close()
            } catch (e: EOFException) {
                val b = file.delete()
                if (b) {
                    return null
                }
            } catch (e: IOException) {
                Log.e("main", "读取失败")
                e.printStackTrace()
            } catch (e: ClassNotFoundException) {
                Log.e("main", "文件不存在")
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return temp
        }
    }

    /**
     * 普通文件读写
     */
    object commonStream {
        fun read(url: String?): String? {
            val encoding = "UTF-8"
            val file = File(url)
            val file_length = file.length().toInt()
            val file_content = ByteArray(file_length)
            try {
                val `in` = FileInputStream(file)
                `in`.read(file_content)
                `in`.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return try {
                String(file_content, charset(encoding))
            } catch (e: UnsupportedEncodingException) {
                System.err.println("The OS does not support $encoding")
                e.printStackTrace()
                null
            }
        }

        fun write(content: String?, url: String) {
            var outputStreamWriter: OutputStreamWriter? = null
            var writer: BufferedWriter? = null
            try {
                val file = File(url.substring(0, url.lastIndexOf("/")))
                if (!file.exists()) {
                    val mkdirs = file.mkdirs()
                    if (!mkdirs) {
                        MyToast.show("文件创建失败", false)
                        return
                    }
                }
                outputStreamWriter =
                    OutputStreamWriter(FileOutputStream(url), StandardCharsets.UTF_8)
                writer = BufferedWriter(outputStreamWriter)
                writer.write(content)
                writer.close()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    outputStreamWriter?.close()
                    writer?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}
