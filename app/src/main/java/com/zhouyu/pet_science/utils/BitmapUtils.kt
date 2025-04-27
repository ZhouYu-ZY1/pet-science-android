package com.zhouyu.pet_science.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Build
import android.renderscript.Allocation
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Base64
import androidx.palette.graphics.Palette
import androidx.palette.graphics.Palette.Swatch
import com.zhouyu.pet_science.application.Application
import com.zhouyu.pet_science.network.HttpUtils
import okhttp3.Request
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import kotlin.math.min

object BitmapUtils {
    /**
     * * 生成圆角Bitmap
     * @param bitmap 原bitmap
     * @param radius 圆角大小
     * @return 圆角后的bitmap
     */
    fun getRoundedCornerBitmap(bitmap: Bitmap?, radius: Int): Bitmap? {
        return if (bitmap == null) {
            null
        } else try {
            val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)
            val color = -0xbdbdbe
            val paint = Paint()
            val rect = Rect(0, 0, bitmap.width, bitmap.height)
            val rectF = RectF(rect)
            paint.isAntiAlias = true
            canvas.drawARGB(0, 0, 0, 0)
            paint.color = color
            canvas.drawRoundRect(rectF, radius.toFloat(), radius.toFloat(), paint)
            paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))
            canvas.drawBitmap(bitmap, rect, rect, paint)
            output
        } catch (e: Exception) {
//            e.printStackTrace();
            bitmap
        }
    }

    /**
     * url获取bitmap
     */
    fun urlToBitmap(url: String?): Bitmap? {
        try {
            return GlideEngine.instance.getCacheBitmap(Application.context, url!!, 1000, 1000)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 得到Bitmap的大小
     */
    @SuppressLint("ObsoleteSdkInt")
    fun getBitmapSize(bitmap: Bitmap?): Long {
        if (bitmap == null) {
            return 0
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {    //API 19
            return bitmap.allocationByteCount.toLong()
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) { //API 12
            bitmap.byteCount.toLong()
        } else (bitmap.rowBytes * bitmap.height).toLong()

        // 在低版本中用一行的字节x高度
        //earlier version
    }

    //获取bitmap颜色
    fun getBitmapColor(bitmap: Bitmap?, type: Int): Swatch? {
        // Palette的部分
        val palette = Palette.from(bitmap!!).generate()
        var swatch: Swatch? = null
        when (type) {
            1 ->                     //获取充满活力的亮
                swatch = palette.lightVibrantSwatch

            2 ->                     //获取充满活力的黑
                swatch = palette.darkVibrantSwatch

            3 ->                     //获取柔和的色调
                swatch = palette.mutedSwatch

            4 ->                     //获取柔和的亮
                swatch = palette.lightMutedSwatch

            5 ->                     //获取柔和的黑
                swatch = palette.darkMutedSwatch

            6 ->                     //获取充满活力的色调
                swatch = palette.vibrantSwatch
        }
        if (swatch == null) {
            swatch = palette.vibrantSwatch
        }
        return swatch
    }

    /**
     * * 将bitmap裁剪为正方形
     */
    fun cropToSquare(source: Bitmap): Bitmap {
        val size = min(source.width, source.height)
        val x = (source.width - size) / 2
        val y = (source.height - size) / 2
        return Bitmap.createBitmap(source, x, y, size, size)
    }

    /**
     * 将字符串转换成Bitmap
     * @param string Bitmap字符串
     * @return Bitmap位图
     */
    fun stringToBitmap(string: String?): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val bitmapArray: ByteArray = Base64.decode(string, Base64.DEFAULT)
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.size)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmap
    }

    /**
     * 将Bitmap转为String
     * @param bitmap Bitmap位图
     * @return Bitmap字符串
     */
    fun bitmapToString(bitmap: Bitmap): String {
        val string: String
        val bStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bStream)
        val bytes = bStream.toByteArray()
        string = Base64.encodeToString(bytes, Base64.DEFAULT)
        return string
    }

    /**
     * 文件转bitmap
     */
    fun bitmapFromFile(filePath: String): Bitmap? {
        val file = File(filePath)
        return if (file.exists()) {
            // 使用BitmapFactory的decodeFile方法从文件路径创建Bitmap
            BitmapFactory.decodeFile(filePath)
        } else null
    }

    /**
     * bitmap转文件
     */
    fun bitmapToFile(bitmap: Bitmap, fileName: String): File? {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos) //质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        var options = 100
        while (baos.toByteArray().size / 1024 > 2048) {  //循环判断如果压缩后图片是否大于2M,大于继续压缩
            baos.reset() //重置baos即清空baos
            options -= 5 //每次都减少5
            bitmap.compress(
                Bitmap.CompressFormat.JPEG,
                options,
                baos
            ) //这里压缩options%，把压缩后的数据存放到baos中
        }
        val dirFile = File(Application.appFilesPath + "/bitmapFile")
        if (!dirFile.exists()) {
            val mkdirs = dirFile.mkdirs()
            if (!mkdirs) {
                return null
            }
        }
        val file = File(dirFile, fileName)
        try {
            val fos = FileOutputStream(file)
            try {
                fos.write(baos.toByteArray())
                fos.flush()
                fos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return file
    }

    /**
     * 根据传入url获取到图片的Bitmap对象
     */
    fun getBitmap(imgUrl: String): Bitmap? {
        var bm: Bitmap?
        val request: Request = Request.Builder().url(imgUrl).build()
        try {
            HttpUtils.client.newCall(request).execute().use { response ->
                //发送请求
                val bis = response.body?.byteStream()
                bm = BitmapFactory.decodeStream(bis)
            }
        } catch (e: Exception) {
            return null
        }
        val bitmapSize = getBitmapSize(bm) //图片过大，先压缩大小
        if (bitmapSize > 32000000) {
            bm = Bitmap.createScaledBitmap(bm!!, 500, 500, true)
        }
        return bm
    }

    // Drawable转换成Bitmap
    fun drawableToBitmap(drawable: Drawable): Bitmap {
        val bitmap = Bitmap
            .createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                if (drawable.opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
            )
        val canvas = Canvas(bitmap)
        drawable.setBounds(
            0, 0, drawable.intrinsicWidth,
            drawable.intrinsicHeight
        )
        drawable.draw(canvas)
        return bitmap
    }

    // 将Bitmap转换成InputStream
    fun bitmapToInputStream(bm: Bitmap): InputStream {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        return ByteArrayInputStream(baos.toByteArray())
    }

    /**
     * 图片模糊处理
     */
    object BlurBitmap {
        /**
         * 模糊图片
         * @param radius 模糊半径
         * @param simple 缩小比例
         */
        fun blur(context: Context?, bm: Bitmap, radius: Int, simple: Int): Bitmap {
            var bitmap = bm
            if (simple > 1) {
                bitmap = compressBitmap(bitmap, simple)
            }
            return blurByRenderScript(context, bitmap, radius)
        }

        /**
         * 模糊
         */
        private fun blurByRenderScript(context: Context?, bitmap: Bitmap, radius: Int): Bitmap {
            try {
                val rs = RenderScript.create(context)
                val allocation = Allocation.createFromBitmap(rs, bitmap)
                val blur = ScriptIntrinsicBlur.create(rs, allocation.element)
                blur.setInput(allocation)
                blur.setRadius(radius.toFloat())
                blur.forEach(allocation)
                allocation.copyTo(bitmap)
                rs.destroy()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return bitmap
        }

        /**
         * 缩放
         */
        private fun compressBitmap(bm: Bitmap, simple: Int): Bitmap {
            var bitmap = bm
            if (simple > 1) {
                if (bitmap.width < 200) {
                    return bitmap
                }
                bitmap = Bitmap.createScaledBitmap(
                    bitmap, bitmap.width / simple,
                    bitmap.height / simple, true
                )
            }
            return bitmap
        }
    }
}
