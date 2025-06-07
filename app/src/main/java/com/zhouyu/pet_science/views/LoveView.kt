package com.zhouyu.pet_science.views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.TimeInterpolator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.utils.PhoneMessage.dpToPx
import java.util.Random

class LoveView : RelativeLayout {
    private var mContext: Context? = null
    private val num = floatArrayOf(-30f, -20f, 0f, 20f, 30f) //随机心形图片角度

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView(context)
    }

    private fun initView(context: Context) {
        mContext = context
    }

    fun addLoveView(event: MotionEvent) {
        val imageView = ImageView(mContext)
        val size = dpToPx(120f)
        val params = LinearLayout.LayoutParams(
            size, size
        ) //爱心高宽
        val offset = dpToPx(10f)
        //根据点击位置确定爱心显示位置
        params.leftMargin = event.x.toInt() - (size / 2 + offset)
        params.topMargin = event.y.toInt() - (size + offset)
        imageView.setImageResource(R.drawable.like_icon_y)
        imageView.layoutParams = params
        addView(imageView)
        val animatorSet = AnimatorSet()
        val num = floatArrayOf(-35f, -20f, 0f, 20f, 35f) //随机心形图片角度
        animatorSet.play(scale(imageView, "scaleX", 2f, 0.9f, 100, 0))
            .with(scale(imageView, "scaleY", 2f, 0.9f, 100, 0))
            .with(rotation(imageView, 0, 0, num[Random().nextInt(4)]))
            .with(alpha(imageView, 0f, 1f, 100, 0))
            .with(scale(imageView, "scaleX", 0.9f, 1f, 50, 150))
            .with(scale(imageView, "scaleY", 0.9f, 1f, 50, 150))
            .with(translationY(imageView, 0f, -1200f, 1000, 400))
            .with(alpha(imageView, 1f, 0f, 300, 400))
            .with(scale(imageView, "scaleX", 1f, 2f, 700, 400))
            .with(scale(imageView, "scaleY", 1f, 2f, 700, 400))
        animatorSet.start()
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                removeViewInLayout(imageView)
            }
        })
    }

    companion object {
        fun scale(
            view: View?,
            propertyName: String?,
            from: Float,
            to: Float,
            time: Long,
            delayTime: Long
        ): ObjectAnimator {
            val translation = ObjectAnimator.ofFloat(
                view, propertyName, from, to
            )
            translation.interpolator = LinearInterpolator()
            translation.startDelay = delayTime
            translation.setDuration(time)
            return translation
        }

        fun translationX(
            view: View?,
            from: Float,
            to: Float,
            time: Long,
            delayTime: Long
        ): ObjectAnimator {
            val translation = ObjectAnimator.ofFloat(
                view, "translationX", from, to
            )
            translation.interpolator = LinearInterpolator()
            translation.startDelay = delayTime
            translation.setDuration(time)
            return translation
        }

        fun translationY(
            view: View?,
            from: Float,
            to: Float,
            time: Long,
            delayTime: Long
        ): ObjectAnimator {
            val translation = ObjectAnimator.ofFloat(
                view, "translationY", from, to
            )
            translation.interpolator = LinearInterpolator()
            translation.startDelay = delayTime
            translation.setDuration(time)
            return translation
        }

        fun alpha(
            view: View?,
            from: Float,
            to: Float,
            time: Long,
            delayTime: Long
        ): ObjectAnimator {
            val translation = ObjectAnimator.ofFloat(
                view, "alpha", from, to
            )
            translation.interpolator = LinearInterpolator()
            translation.startDelay = delayTime
            translation.setDuration(time)
            return translation
        }

        fun rotation(
            view: View?,
            time: Long,
            delayTime: Long,
            vararg values: Float
        ): ObjectAnimator {
            val rotation = ObjectAnimator.ofFloat(view, "rotation", *values)
            rotation.setDuration(time)
            rotation.startDelay = delayTime
            rotation.interpolator = TimeInterpolator { input: Float -> input }
            return rotation
        }
    }
}
