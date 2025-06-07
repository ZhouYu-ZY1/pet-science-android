package com.zhouyu.pet_science.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import com.zhouyu.pet_science.R

@Suppress("NAME_SHADOWING")
@SuppressLint("ViewConstructor", "UseKtx")
class RingProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) :
    View(context, attrs, defStyle) {
    /**
     * 画笔对象的引用
     */
    private val paint = Paint()

    /**
     * 圆环的颜色
     */
    private val ringColor: Int

    /**
     * 中间圆的颜色
     */
    private val circleColor: Int

    /**
     * 圆环进度的颜色
     */
    private val progressColor: Int

    /**
     * 中间进度百分比的字符串的颜色
     */
    private val textColor: Int

    /**
     * 中间进度百分比的字符串的字体
     */
    private val textSize: Float

    /**
     * 外层圆圈的宽度
     */
    private val outerRingWidth: Float

    /**
     * 最大进度
     */
    private val max: Int

    /**
     * 当前进度
     */
    private var progress: Int

    /**
     * 是否显示中间的进度
     */
    private val textIsVisibility: Boolean

    /**
     * 进度的风格，实心或者空心
     */
    private val style: Int

    init {
        paint.isAntiAlias = true //消除锯齿
        val mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.RingProgressBar)
        //获取自定义属性和默认值
        max = mTypedArray.getInteger(R.styleable.RingProgressBar_rMax, 100) //进度最大值
        textColor = mTypedArray.getColor(R.styleable.RingProgressBar_rTextColor, Color.GREEN) //字体颜色
        textSize = mTypedArray.getDimension(R.styleable.RingProgressBar_rTextSize, 40f) //字体大小
        circleColor = mTypedArray.getColor(
            R.styleable.RingProgressBar_rCircleColor,
            Color.TRANSPARENT
        ) //中间圆的颜色,默认透明
        ringColor = mTypedArray.getColor(
            R.styleable.RingProgressBar_rRingColor,
            Color.TRANSPARENT
        ) //外层初始圆环的颜色，默认透明
        progressColor = mTypedArray.getColor(
            R.styleable.RingProgressBar_rProgressColor,
            Color.BLUE
        ) //外层进度环的颜色，默认蓝色
        textIsVisibility = mTypedArray.getBoolean(
            R.styleable.RingProgressBar_rTextIsVisibility,
            true
        ) //文字是否可见，默认可见
        outerRingWidth = mTypedArray.getDimension(R.styleable.RingProgressBar_outerRingWidth, 10f)
        progress = mTypedArray.getInteger(R.styleable.RingProgressBar_progress, 100)
        style = mTypedArray.getInt(R.styleable.RingProgressBar_style, 0)
        mTypedArray.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val ringWidth = outerRingWidth
        //1.画最外层的圆
        val centre = width / 2f //获取圆心的x坐标
        val radius = centre - ringWidth / 2 //圆环的半径

        paint.strokeWidth = ringWidth //设置圆环的宽度
        paint.color = ringColor //设置圆环的颜色
        paint.style = Paint.Style.STROKE //设置空心
        canvas.drawCircle(centre, centre, radius, paint) //画出圆环
        //2.画中间圆环
        paint.color = circleColor
        paint.style = Paint.Style.FILL
        canvas.drawCircle(centre, centre, centre - ringWidth, paint) //画出圆环
        //3.画进度圆环
        paint.strokeWidth = ringWidth //设置圆环的宽度
        paint.color = progressColor //设置进度的颜色
        @SuppressLint("DrawAllocation") val oval = RectF(
            centre - radius,
            centre - radius,
            centre + radius,
            centre + radius
        ) //用于定义的圆弧的形状和大小的界限
        when (style) {
            STROKE -> {
                //空心进度环
                paint.style = Paint.Style.STROKE
                canvas.drawArc(oval, -90f, 360f * progress / max, false, paint) //根据进度画圆弧
            }

            FILL -> {
                //实心进度环
                paint.style = Paint.Style.FILL_AND_STROKE
                if (progress != 0) {
                    canvas.drawArc(oval, -90f, 360f * progress / max, true, paint) //根据进度画圆弧
                }
            }
        }

        //4.画进度百分比
        if (textIsVisibility) {
            paint.strokeWidth = 0f
            paint.color = textColor
            paint.textSize = textSize
            paint.setTypeface(Typeface.DEFAULT) //设置字体
            val percent =
                ((progress.toFloat() / max.toFloat()) * 100).toInt() //中间的进度百分比，先转换成float在进行除法运算，不然都为0
            val textWidth = paint.measureText("$percent%") //测量字体宽度，我们需要根据字体的宽度设置在圆环中间
            canvas.drawText(
                "$percent%",
                centre - textWidth / 2,
                centre + textSize / 2,
                paint
            ) //画出进度百分比
        }
    }

    /**
     * 设置进度，此为线程安全控件，由于考虑多线的问题，需要同步
     * 刷新界面调用postInvalidate()能在非UI线程刷新
     */
    @Synchronized
    fun setProgress(progress: Int) {
        var progress = progress
        require(progress >= 0) { "progress not less than 0" }
        if (progress > max) {
            progress = max
        }
        this.progress = progress
        postInvalidate()
    }

    companion object {
        const val STROKE: Int = 0
        const val FILL: Int = 1
    }
}

