package com.zhouyu.pet_science.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.utils.PhoneMessage.dpToPx
import com.zhouyu.pet_science.utils.PhoneMessage.dpToPxFloat
import kotlin.math.abs

class RightLetterSort : View {
    private val textPaint = Paint()
    private val textPaint2 = Paint()
    private val textPaint3 = Paint()
    private val bubblePaint = Paint()

    //鼠标点击、滑动时选择的字母
    private var choose = -1
    private var now = ""
    private var bubbleRadius = 25
    private var path: Path? = null

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initPaint()
    }

    constructor(context: Context?) : super(context) {
        initPaint()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initPaint()
    }

    private fun initPaint() {
        val context = context
        textPaint.textSize = dpToPx(11f).toFloat()
        textPaint.isAntiAlias = true
        textPaint.color = Color.parseColor("#818792")

        textPaint2.textSize = dpToPx(27f).toFloat()
        textPaint2.isAntiAlias = true
        textPaint2.setTypeface(Typeface.DEFAULT_BOLD)
        textPaint2.color = Color.WHITE

        textPaint3.textSize = dpToPx(12f).toFloat()
        textPaint3.isAntiAlias = true
        textPaint3.setTypeface(Typeface.DEFAULT_BOLD)
        textPaint3.color = context.getColor(R.color.Theme)

        bubblePaint.color = -0x333334
        bubblePaint.style = Paint.Style.FILL
        bubblePaint.strokeWidth = 5f
        bubblePaint.isAntiAlias = true


        bubbleRadius = dpToPx(bubbleRadius.toFloat())
        path = Path()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //画字母
        drawText(canvas)
        drawBuble(canvas)
    }


    private fun drawBuble(canvas: Canvas) {
        if (choose >= 0) {
            val x = (-width * 3f) / 2f - dpToPxFloat(15f)
            val singleHeight = height.toFloat() / letter.size
            val y = singleHeight / 2f + (singleHeight * choose)
            canvas.drawCircle(x, y, bubbleRadius.toFloat(), bubblePaint)


            path!!.reset()
            path!!.moveTo(x, y - bubbleRadius)
            path!!.lineTo(x, y + bubbleRadius)
            path!!.lineTo(x + bubbleRadius / 4f * 6, y)
            path!!.close()
            canvas.drawPath(path!!, bubblePaint)
            val baseLineY =
                (y + (abs((textPaint2.ascent() + textPaint2.descent()).toDouble()) / 2)).toFloat()
            canvas.drawText(now, x - textPaint2.measureText(now) / 2, baseLineY, textPaint2)
        }
    }


    /**
     * 画字母
     */
    private fun drawText(canvas: Canvas) {
        val width = width
        val height = height
        //获取每个字母的高度
        val singleHeight = height / letter.size
        //画字母
        for (i in letter.indices) {
            if (i == choose) {
                val x = (width - textPaint3.measureText(letter[i])) / 2
                val baseLineY = (i * singleHeight + (singleHeight / 2f) + (abs(
                    (textPaint3.ascent() + textPaint3.descent()).toDouble()
                ) / 2)).toFloat()
                canvas.drawText(letter[i], x, baseLineY, textPaint3)
            } else {
                val x = (width - textPaint.measureText(letter[i])) / 2
                val baseLineY = (i * singleHeight + (singleHeight / 2f) + (abs(
                    (textPaint.ascent() + textPaint.descent()).toDouble()
                ) / 2)).toFloat()
                canvas.drawText(letter[i], x, baseLineY, textPaint)
            }
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        //计算选中字母
        var index = (event.y / height * letter.size).toInt()
        //防止脚标越界
        if (index >= letter.size) {
            index = letter.size - 1
        } else if (index < 0) {
            index = 0
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                if (choose == index) {
                    return true
                }
                choose = index
                now = letter[choose]
                invalidate()
                if (listener != null) {
                    listener!!.touchCharacterListener(now)
                }
            }

            else -> {
                choose = -1
                invalidate()
            }
        }
        return true
    }

    private var listener: onTouchCharacterListener? = null

    interface onTouchCharacterListener {
        fun touchCharacterListener(s: String?)
    }

    fun setListener(listener: onTouchCharacterListener?) {
        this.listener = listener
    }

    fun invalidateView() {
        if (Looper.getMainLooper().thread === Thread.currentThread()) {
            invalidate()
        } else {
            postInvalidate()
        }
    }

    companion object {
        val letter: Array<String> = arrayOf(
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
            "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z"
        )
    }
}

