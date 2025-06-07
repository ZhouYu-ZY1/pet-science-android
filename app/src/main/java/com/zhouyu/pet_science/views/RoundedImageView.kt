package com.zhouyu.pet_science.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.widget.ImageView
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.utils.PhoneMessage.dpToPxFloat
import androidx.core.graphics.createBitmap

/**
 * 圆角图片（可设置圆角度）
 */
@SuppressLint("AppCompatCustomView")
class RoundedImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) :
    ImageView(context, attrs, defStyle) {
    private var radius: Float //圆角度数

    fun setRadius(radius: Int) {
        this.radius = dpToPxFloat(radius.toFloat())
    }

    /**
     * 绘制圆角矩形图片
     * @author caizhiming
     */
    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        try {
            val drawable = drawable
            if (drawable is BitmapDrawable) {
                val bitmap = drawable.bitmap
                val b = getRoundBitmapByShader(
                    bitmap,
                    width, height, radius, 0
                )

                if (b == null) {
                    super.onDraw(canvas)
                    return
                }

                if (rect == null) {
                    rect = Rect()
                }

                rect!![0, 0, b.width] = b.height
                val rectSrc = rect
                rect!![0, 0, width] = height
                val rectDest = rect

                paint!!.reset()
                canvas.drawBitmap(b, rectSrc, rectDest!!, paint)
            } else {
                super.onDraw(canvas)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            super.onDraw(canvas)
        }
    }

    init {
        if (paint == null) {
            paint = Paint()
        }

        @SuppressLint("Recycle") val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.RoundedImageView)
        radius = typedArray.getDimension(R.styleable.RoundedImageView_radius, dpToPxFloat(50f))
    }

    companion object {
        private var paint: Paint? = null
        var rect: Rect? = null
        var matrix: Matrix? = null
        var canvas: Canvas? = null
        var bitmapPaint: Paint? = null
        var rectf: RectF? = null
        var boarderPaint: Paint? = null
        fun getRoundBitmapByShader(
            bitmap: Bitmap?,
            outWidth: Int,
            outHeight: Int,
            radius: Float,
            boarder: Int
        ): Bitmap? {
            try {
                if (bitmap == null) {
                    return null
                }
                val width = bitmap.width
                val height = bitmap.height
                if (width <= 0 || height <= 0) {
                    return null
                }
                val widthScale = outWidth * 1f / width
                val heightScale = outHeight * 1f / height

                if (matrix == null) {
                    matrix = Matrix()
                }
                matrix!!.setScale(widthScale, heightScale)
                //创建输出的bitmap
                val desBitmap = createBitmap(outWidth, outHeight)
                //创建canvas并传入desBitmap，这样绘制的内容都会在desBitmap上
//        Canvas canvas = new Canvas(desBitmap);
                if (canvas == null) {
                    canvas = Canvas()
                }
                canvas!!.setBitmap(desBitmap)

                if (bitmapPaint == null) {
                    bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                }

                //创建着色器
                val bitmapShader =
                    BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)

                //给着色器配置matrix
                bitmapShader.setLocalMatrix(matrix)

                bitmapPaint!!.setShader(bitmapShader)

                //创建矩形区域并且预留出border
//        RectF rect = new RectF(boarder, boarder, outWidth - boarder, outHeight - boarder);
                if (rectf == null) {
                    rectf = RectF()
                }
                rectf!![boarder.toFloat(), boarder.toFloat(), (outWidth - boarder).toFloat()] =
                    (outHeight - boarder).toFloat()

                //把传入的bitmap绘制到圆角矩形区域内
                canvas!!.drawRoundRect(
                    rectf!!, radius, radius,
                    bitmapPaint!!
                )

                if (boarder > 0) {
                    //绘制boarder
                    if (boarderPaint == null) {
                        boarderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                    }
                    boarderPaint!!.color = Color.GREEN
                    boarderPaint!!.style =
                        Paint.Style.STROKE
                    boarderPaint!!.strokeWidth = boarder.toFloat()
                    canvas!!.drawRoundRect(
                        rectf!!, radius, radius,
                        boarderPaint!!
                    )
                }
                return desBitmap
            } catch (e: Exception) {
                e.printStackTrace()
                return bitmap
            }
        }
    }
}




