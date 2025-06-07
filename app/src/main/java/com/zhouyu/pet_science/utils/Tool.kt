package com.zhouyu.pet_science.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.application.Application
import kotlin.math.abs

object Tool {
    /**
     * 设置全屏
     * @param activity 要全屏的activity
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    fun fullScreen(activity: Activity?) {
        if (activity == null) {
            return
        }
        val window = activity.window
        val layoutParams = window.attributes
        layoutParams.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        layoutParams.flags = layoutParams.flags or WindowManager.LayoutParams.FLAG_FULLSCREEN
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.attributes = layoutParams
    }

    /**
     * 时间转换
     * @param milliTime 毫秒值
     * @return 格式（00:00）
     */
    fun timeCycle(milliTime: Long): String {
        if (milliTime == 0L) {
            return "00:00"
        }
        val time = milliTime / 1000
        val m = time / 60
        val s = time % 60
        var mIsOver = false
        var sIsOver = false
        if (m < 10) {
            mIsOver = true
        }
        if (s < 10) {
            sIsOver = true
        }
        return if (!mIsOver && !sIsOver) {
            "$m:$s"
        } else if (mIsOver && !sIsOver) {
            "0$m:$s"
        } else if (!mIsOver) {
            "$m:0$s"
        } else {
            "0$m:0$s"
        }
    }


    @JvmStatic
    fun contextToActivity(context: Context?): Activity? {
        try {
            when (context) {
                null -> return null
                is Activity -> return context
                is ContextWrapper -> return contextToActivity(
                    context.baseContext
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * View渐渐消失动画效果
     */
    fun setHideAnimation(view: View, duration: Int) {
        view.visibility = View.VISIBLE
        view.alpha = 1f
        //为加载视图设置动画，使其不透明度为0％。动画结束后，将其可见性设置为GONE作为优化步骤（它将不参与布局传递，等等）.
        view.animate()
            .alpha(0f)
            .setDuration(duration.toLong()) //动画持续时间 毫秒单位
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.visibility = View.GONE
                }
            })
    }

    /**
     * View渐渐显示动画效果
     */
    fun setShowAnimation(view: View, duration: Int) {
        view.visibility = View.VISIBLE
        view.alpha = 0f
        //为加载视图设置动画，使其不透明度为0％。动画结束后，将其可见性设置为GONE作为优化步骤（它将不参与布局传递，等等）.
        view.animate()
            .alpha(1f)
            .setDuration(duration.toLong())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {}
            }) //动画持续时间 毫秒单位
    }

    /**
     * View渐渐消失动画效果
     */
    fun setHideAnimation(view: View, duration: Int, listener: Animator.AnimatorListener?) {
        view.visibility = View.VISIBLE
        view.alpha = 1f
        //为加载视图设置动画，使其不透明度为0％。动画结束后，将其可见性设置为GONE作为优化步骤（它将不参与布局传递，等等）.
        view.animate()
            .alpha(0f)
            .setDuration(duration.toLong()) //动画持续时间 毫秒单位
            .setListener(listener)
    }

    /**
     * View渐渐显示动画效果
     */
    fun setShowAnimation(view: View, duration: Int, listener: Animator.AnimatorListener?) {
        view.visibility = View.VISIBLE
        view.alpha = 0f
        //为加载视图设置动画，使其不透明度为0％。动画结束后，将其可见性设置为GONE作为优化步骤（它将不参与布局传递，等等）.
        view.animate()
            .alpha(1f)
            .setDuration(duration.toLong())
            .setListener(listener) //动画持续时间 毫秒单位
    }

    /**
     * View渐渐显示动画效果(显示到指定透明度)
     */
    fun setShowAnimation(view: View, duration: Int, alpha: Float) {
        view.visibility = View.VISIBLE
        view.alpha = 0f
        //为加载视图设置动画，使其不透明度为0％。动画结束后，将其可见性设置为GONE作为优化步骤（它将不参与布局传递，等等）.
        view.animate()
            .alpha(alpha)
            .setDuration(duration.toLong())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {}
            }) //动画持续时间 毫秒单位
    }

    /**
     * 底部弹框
     */
    object MyBottomSheetDialog {
        fun showBottomSheetDialog(dialog: BottomSheetDialog, viewList: ArrayList<View?>) {
            @SuppressLint("InflateParams") val dialogView =
                LayoutInflater.from(Application.context).inflate(R.layout.activity_bottom, null)
            val frame = dialogView.findViewById<LinearLayout>(R.id.frame)
            for (view in viewList) {
                frame.addView(view, 1)
            }
            val activity = contextToActivity(dialog.context)
            if (activity == null || activity.isDestroyed) {
                return
            }
            dialog.setContentView(dialogView)
            val sheetBehavior = dialog.behavior
            sheetBehavior.peekHeight = PhoneMessage.heightPixels
            //            dialog.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);
            dialog.show()
        }

        @JvmOverloads
        fun createBottomDialogView(icon: Int, text: String?, iconSize: Int = 0): LinearLayout {
            @SuppressLint("InflateParams") val view = LayoutInflater.from(Application.context)
                .inflate(R.layout.item_bottom_dialog_text, null) as LinearLayout
            val textView = view.findViewById<TextView>(R.id.bottom_dialog_text)
            textView.setLines(1)
            textView.ellipsize = TextUtils.TruncateAt.END
            textView.text = text
            val bottomDialogIcon = view.findViewById<ImageView>(R.id.bottom_dialog_icon)
            if (iconSize != 0) {
                val layoutParams = bottomDialogIcon.layoutParams
                val size = PhoneMessage.dpToPx(iconSize.toFloat())
                layoutParams.width = size
                layoutParams.height = size
                bottomDialogIcon.layoutParams = layoutParams
            }
            bottomDialogIcon.setImageResource(icon)
            return view
        }

        @SuppressLint("UseCompatLoadingForColorStateLists")
        fun createTextView(text: String?): TextView {
            val context = Application.context
            val view = TextView(context)
            view.text = text
            view.setTextColor(context.getColorStateList(R.color.textGeneral))
            view.setBackgroundResource(R.drawable.touch_anim_default)
            view.textSize = 16f
            view.setPadding(
                PhoneMessage.dpToPx(20f),
                PhoneMessage.dpToPx(20f),
                PhoneMessage.dpToPx(20f),
                PhoneMessage.dpToPx(20f)
            )
            view.gravity = Gravity.CENTER
            return view
        }
    }

    /**
     * 自定义双击单击判断
     */
    class OnDoubleClickListener(
        /**
         * 自定义回调接口
         */
        private val mCallback: DoubleClickCallback?
    ) : OnTouchListener {
        private var firstClick = false
        private val handler = Handler(Looper.getMainLooper())

        /**
         * 两次点击时间间隔，单位毫秒
         */
        private val totalTime = 300

        interface DoubleClickCallback {
            fun onDoubleClick(event: MotionEvent?)
            fun onClick()
        }

        private val handlerDouble = Handler(Looper.getMainLooper())

        /**
         * 触摸事件处理
         */
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            if (MotionEvent.ACTION_UP == event.action) { //按下抬起
                if (firstClick) {
                    handler.removeCallbacksAndMessages(null)
                    mCallback?.onDoubleClick(event)
                    handlerDouble.removeCallbacksAndMessages(null)
                    handlerDouble.postDelayed({ firstClick = false }, 300)
                    return true
                }
                firstClick = true
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed({
                    mCallback?.onClick()
                    firstClick = false
                    handler.removeCallbacksAndMessages(null)
                }, totalTime.toLong())
            }
            return true
        }
    }

    /**
     * 自定义长按事件监听
     */
    class OnLongPressListener(
        /**
         * 自定义回调接口
         */
        private val mCallback: OnLongPressCallback
    ) : OnTouchListener {
        interface OnLongPressCallback {
            fun onLongPress()
            fun onClick()
        }

        private var isPress = false
        private val handler = Handler(Looper.myLooper()!!)
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            /**
             * 按下多长时间回调长按
             */
            val pressTime = 500
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isPress = false
                    handler.removeCallbacksAndMessages(null)
                    handler.postDelayed({
                        isPress = true
                        mCallback.onLongPress()
                    }, pressTime.toLong())
                }

                MotionEvent.ACTION_UP -> {
                    handler.removeCallbacksAndMessages(null)
                    //判断是长按还是点击
                    if (!isPress) {
                        mCallback.onClick()
                    }
                }
            }
            return false
        }
    }

    /**
     * 自定义单击、双击、长按、移动事件监听
     */
    class OnCombineEventListener(
        /**
         * 回调接口
         */
        private val mCallback: OnCombineEventCallback
    ) : OnTouchListener {
        interface OnCombineEventCallback {
            fun onClick()
            fun onDoubleClick()
            fun onLongPress(longPressPositionX: Float, longPressPositionY: Float)
            fun onMove(event: MotionEvent?, startX: Float, lastY: Float)
        }

        private var mClickCount = 0 // 点击次数
        private var mDownX = 0
        private var mDownY = 0
        private var mLastDownTime: Long = 0
        private var mFirstClick: Long = 0
        private var mSecondClick: Long = 0
        private var isDoubleClick = false
        private val mBaseHandler = Handler(Looper.getMainLooper())
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    lastY = 0f
                    mLastDownTime = System.currentTimeMillis()
                    mDownX = event.x.toInt()
                    mDownY = event.y.toInt()
                    mClickCount++
                    mBaseHandler.removeCallbacks(mSingleClickTask)
                    if (!isDoubleClick) {
                        longPressPositionX = event.x
                        longPressPositionY = event.y
                        mBaseHandler.postDelayed(mLongPressTask, MAX_LONG_PRESS_TIME.toLong())
                    }
                    if (1 == mClickCount) {
                        mFirstClick = System.currentTimeMillis()
                    } else if (mClickCount == 2) { // 双击
                        mSecondClick = System.currentTimeMillis()
                        if (mSecondClick - mFirstClick <= MAX_LONG_PRESS_TIME) {
                            //处理双击
                            mDoubleClickTask()
                        }
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    val endY = event.y
                    if (lastY == 0f) {
                        lastY = endY
                        return false
                    }
                    lastY = endY
                    val mMoveX = event.x.toInt()
                    val mMoveY = event.y.toInt()
                    val absMx = abs((mMoveX - mDownX).toDouble()).toInt()
                    val absMy = abs((mMoveY - mDownY).toDouble()).toInt()
                    if (absMx > MIN_DISTANCE || absMy > MIN_DISTANCE) {
                        mBaseHandler.removeCallbacks(mLongPressTask)
                        mBaseHandler.removeCallbacks(mSingleClickTask)
                        isDoubleClick = false
                        mClickCount = 0 //移动了
                        mMoveTask(event)
                    }
                }

                MotionEvent.ACTION_UP -> {
                    val mLastUpTime = System.currentTimeMillis()
                    val mUpX = event.x.toInt()
                    val mUpY = event.y.toInt()
                    val mx = abs((mUpX - mDownX).toDouble()).toInt()
                    val my = abs((mUpY - mDownY).toDouble()).toInt()
                    if (mx <= MIN_DISTANCE && my <= MIN_DISTANCE) {
                        if (mLastUpTime - mLastDownTime <= MAX_LONG_PRESS_TIME) {
                            mBaseHandler.removeCallbacks(mLongPressTask)
                            if (!isDoubleClick) {
                                mBaseHandler.postDelayed(
                                    mSingleClickTask,
                                    MAX_SINGLE_CLICK_TIME.toLong()
                                )
                            }
                        } else {
                            //超出了双击间隔时间
                            mClickCount = 0
                        }
                    } else {
                        //移动了
                        mClickCount = 0
                    }
                    if (isDoubleClick) {
                        isDoubleClick = false
                    }
                }
            }
            return false
        }

        private val mSingleClickTask: Runnable = Runnable { // 处理单击
            mClickCount = 0
            mCallback.onClick()
        }

        private fun mDoubleClickTask() {
            //处理双击
            isDoubleClick = true
            mClickCount = 0
            mFirstClick = 0
            mSecondClick = 0
            mBaseHandler.removeCallbacks(mSingleClickTask)
            mBaseHandler.removeCallbacks(mLongPressTask)

            //双击
            mCallback.onDoubleClick()
        }

        private var startX = 0f //手指按下时的X坐标
        private var lastY = 0f
        private fun mMoveTask(event: MotionEvent) {
            //处理移动
            mCallback.onMove(event, startX, lastY)
        }

        private var longPressPositionX = 0f //长按位置
        private var longPressPositionY = 0f
        private val mLongPressTask = Runnable { //处理长按
            mClickCount = 0
            mCallback.onLongPress(longPressPositionX, longPressPositionY)
        }

        companion object {
            private const val MAX_LONG_PRESS_TIME = 400 // 长按/双击最长等待时间
            private const val MAX_SINGLE_CLICK_TIME = 220 // 单击后等待的时间
            private const val MIN_DISTANCE = 8 //最小滑动距离
        }
    }
}
