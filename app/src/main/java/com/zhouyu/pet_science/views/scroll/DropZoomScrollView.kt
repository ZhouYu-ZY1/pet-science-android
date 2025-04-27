package com.zhouyu.pet_science.views.scroll

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.ScrollView

/**
 * 下拉放大scrollView
 */
class DropZoomScrollView : ScrollView, OnTouchListener {
    // 记录首次按下位置
    private var mFirstPosition = 0f

    // 是否正在放大
    private var mScaling = false
    private var dropZoomView: View? = null
    private var dropZoomViewWidth = 0
    private var dropZoomViewHeight = 0
    private var isDisabled = false

    //是否禁用
    fun disabled(isDisabled: Boolean) {
        this.isDisabled = isDisabled
    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (isDisabled) {
            return
        }
        init()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun init() {
        if (isDisabled) {
            return
        }
        overScrollMode = OVER_SCROLL_NEVER
        if (getChildAt(0) != null) {
            val vg = getChildAt(0) as ViewGroup
            if (vg.getChildAt(0) != null) {
                dropZoomView = vg.getChildAt(0)
                setOnTouchListener(this)
            }
        }
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (isDisabled) {
            return false
        }
        if (dropZoomViewWidth <= 0 || dropZoomViewHeight <= 0) {
            dropZoomViewWidth = dropZoomView!!.measuredWidth
            dropZoomViewHeight = dropZoomView!!.measuredHeight
        }
        when (event.action) {
            MotionEvent.ACTION_UP -> {
                //手指离开后恢复图片
                mScaling = false
                replyImage()
            }

            MotionEvent.ACTION_MOVE -> {
                if (!mScaling) {
                    mFirstPosition = if (scrollY == 0) {
                        event.y // 滚动到顶部时记录位置，否则正常返回
                    } else {
                        return false
                    }
                }
                var distance = ((event.y - mFirstPosition) * 0.6).toInt() // 滚动距离乘以一个系数
                if (distance < 0) { // 当前位置比记录位置要小，正常返回
                    return false
                }
                if (distance > 300) {
                    distance = 300
                }

                // 处理放大
                mScaling = true
                setZoom((1 + distance).toFloat())
                return true // 返回true表示已经完成触摸事件，不再处理
            }
        }
        return false
    }

    // 回弹动画 (使用了属性动画)
    fun replyImage() {
        if (isDisabled) {
            return
        }
        val distance = (dropZoomView!!.measuredWidth - dropZoomViewWidth).toFloat()

        // 设置动画
        val anim = ObjectAnimator.ofFloat(0.0f, 1.0f).setDuration((distance * 0.7).toLong())
        anim.addUpdateListener { animation: ValueAnimator ->
            val cVal = animation.animatedValue as Float
            setZoom(distance - distance * cVal)
        }
        anim.start()
    }

    //缩放
    fun setZoom(s: Float) {
        if (isDisabled) {
            return
        }
        if (dropZoomViewHeight <= 0 || dropZoomViewWidth <= 0) {
            return
        }
        val lp = dropZoomView!!.layoutParams
        lp.width = (dropZoomViewWidth + s).toInt()
        lp.height = (dropZoomViewHeight * ((dropZoomViewWidth + s) / dropZoomViewWidth)).toInt()
        dropZoomView!!.layoutParams = lp
    }
}
