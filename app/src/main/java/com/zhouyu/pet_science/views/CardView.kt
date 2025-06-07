package com.zhouyu.pet_science.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.LinearLayout
import com.zhouyu.pet_science.R

class CardView : LinearLayout {
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        setCorner(context, attrs)
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setCorner(context, attrs)
        init()
    }

    constructor(context: Context?) : super(context) {
        init()
    }

    private val roundRect = RectF()
    private var rect_radius = 0f
    private val maskPaint = Paint()
    private val zonePaint = Paint()

    private fun init() {
        maskPaint.isAntiAlias = true
        maskPaint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))
        zonePaint.isAntiAlias = true
        zonePaint.color = Color.WHITE
        val density = resources.displayMetrics.density
        rect_radius = rect_radius * density
    }

    fun setCorner(context: Context, attrs: AttributeSet?) {
        @SuppressLint("Recycle") val typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.CardView)
        rect_radius = typedArray.getDimension(R.styleable.CardView_view_radius, 0f)
        invalidate()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val w = width
        val h = height
        roundRect[0f, 0f, w.toFloat()] = h.toFloat()
    }

    override fun draw(canvas: Canvas) {
        canvas.saveLayer(roundRect, zonePaint, Canvas.ALL_SAVE_FLAG)
        canvas.drawRoundRect(roundRect, rect_radius, rect_radius, zonePaint)
        canvas.saveLayer(roundRect, maskPaint, Canvas.ALL_SAVE_FLAG)
        super.draw(canvas)
        canvas.restore()
    }
}