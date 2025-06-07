package com.zhouyu.pet_science.utils

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import com.zhouyu.pet_science.utils.ConsoleUtils.logErr
import com.zhouyu.pet_science.utils.PhoneMessage.dpToPx
import com.zhouyu.pet_science.utils.PhoneMessage.heightPixels
import com.zhouyu.pet_science.utils.PhoneMessage.widthPixels
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class EventUtils {
    /**
     * 自定义双击单击判断
     */
    class OnDoubleClickListener(
        /**
         * 自定义回调接口
         */
        private val mCallback: DoubleClickCallback?, context: Context?
    ) : OnTouchListener {
        private var firstClick = false
        private val handler = Handler(Looper.getMainLooper())

        /**
         * 两次点击时间间隔，单位毫秒
         */
        private val totalTime = 250
        private var downX: Float = 0f
        private var downY: Float = 0f
        private val gestureDetector: GestureDetector
        private var isLongPress = false

        interface DoubleClickCallback {
            fun onDoubleClick(event: MotionEvent?)
            fun onClick(event: MotionEvent?)
            fun onLongPress(event: MotionEvent?)
            fun onLongPressFinish(event: MotionEvent?)
            fun onTouch(view: View?, event: MotionEvent?): Boolean {
                return true
            }
        }

        private val handlerDouble = Handler(Looper.getMainLooper())

        init {
            gestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
                override fun onLongPress(e: MotionEvent) {
                    super.onLongPress(e)
                    isLongPress = true
                    mCallback!!.onLongPress(e)
                }
            })
        }

        /**
         * 触摸事件处理
         */
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            gestureDetector.onTouchEvent(event)

            // 记录按下的位置
            if (MotionEvent.ACTION_DOWN == event.action) {
                downX = event.x
                downY = event.y
            } else if (MotionEvent.ACTION_UP == event.action) { //按下抬起
                // 计算按下和抬起的距离
                val upX = event.x
                val upY = event.y
                val distance = sqrt((upX - downX).toDouble().pow(2.0) + (upY - downY).toDouble().pow(2.0)).toFloat()

                // 如果距离超过阈值，则不触发点击事件
                val maxClickDistance = dpToPx(5f) // 设置最大允许的距离
                if (distance > maxClickDistance) {
                    return mCallback!!.onTouch(v, event)
                }

                if (firstClick) {
                    handler.removeCallbacksAndMessages(null)
                    mCallback?.onDoubleClick(event)
                    handlerDouble.removeCallbacksAndMessages(null)
                    handlerDouble.postDelayed({ firstClick = false }, totalTime.toLong())
                    return mCallback!!.onTouch(v, event)
                }
                firstClick = true
                if (isLongPress) {
                    mCallback!!.onLongPressFinish(event)
                    isLongPress = false
                    firstClick = false
                } else {
                    handler.removeCallbacksAndMessages(null)
                    handler.postDelayed({
                        mCallback?.onClick(event)
                        firstClick = false
                        handler.removeCallbacksAndMessages(null)
                    }, totalTime.toLong())
                }
            }
            return mCallback!!.onTouch(v, event)
        }
    }

    class OnMoreEventListener(
        /**
         * 自定义回调接口
         */
        private val mEventCallback: MoreEventCallback
    ) : OnTouchListener {
        interface MoreEventCallback {
            fun onDoubleClick(event: MotionEvent?)
            fun onClick()
            fun onLongPress()
            fun onLongPressFinish()
            fun longPressFinishLeft() {}
            fun longPressFinishRight() {}
            fun onTouch(view: View?, event: MotionEvent?): Boolean {
                return true
            }

            fun onMoveLeftUp() {}
            fun onMoveRightUp() {}
            fun onMoveRightDown() {}
            fun onMoveLeftDown() {}
        }

        private var isVertical = false //纵向滑动
        private var isHorizontal = false //横向滑动
        private var startX = 0f //手指按下时的X坐标
        private var startY = 0f //手指按下时的Y坐标
        private var lastCalcX = 0f
        private var lastCalcY = 0f
        private var mClickCount = 0 // 点击次数
        private var mDownX = 0
        private var mDownY = 0
        private var mLastDownTime: Long = 0
        private var mFirstClick: Long = 0
        private var mSecondClick: Long = 0
        private var isDoubleClick = false
        private val mBaseHandler = Handler(Looper.getMainLooper())
        private var isUp = false
        @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val x = event.x
                    val y = event.y
                    isLongPress = false
                    startX = x
                    startY = y
                    lastCalcX = x
                    lastCalcY = y
                    mLastDownTime = System.currentTimeMillis()
                    mDownX = x.toInt()
                    mDownY = y.toInt()
                    mClickCount++
                    if (mSingleClickTask != null) {
                        mBaseHandler.removeCallbacks(mSingleClickTask)
                    }
                    if (!isDoubleClick) {
                        longPressPositionX = x
                        isUp = false
                        mBaseHandler.postDelayed(mLongPressTask, MAX_LONG_PRESS_TIME.toLong())
                    }
                    if (1 == mClickCount) {
                        mFirstClick = System.currentTimeMillis()
                    } else if (mClickCount >= 2) { // 双击
                        mSecondClick = System.currentTimeMillis()
                        if (mSecondClick - mFirstClick <= MAX_LONG_PRESS_TIME) {
                            //处理双击
                            mDoubleClickTask(event)
                        }
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    val x = event.x
                    val y = event.y
                    val mMoveX = x.toInt()
                    val mMoveY = y.toInt()
                    val absMx = abs((mMoveX - mDownX).toDouble()).toInt()
                    val absMy = abs((mMoveY - mDownY).toDouble()).toInt()
                    if (absMx > MIN_DISTANCE || absMy > MIN_DISTANCE) {
                        mBaseHandler.removeCallbacks(mLongPressTask)
                        mBaseHandler.removeCallbacks(mSingleClickTask!!)
                        isDoubleClick = false
                        mClickCount = 0 //移动了
                    }
                    if (absMx > MIN_DISTANCE || absMy > MIN_DISTANCE) {
                        mBaseHandler.removeCallbacks(mLongPressTask)
                        mBaseHandler.removeCallbacks(mSingleClickTask!!)
                        isDoubleClick = false
                        mClickCount = 0 //移动了
                        val distanceY: Float
                        var isUP = false
                        if (y > lastCalcY) {
                            distanceY = y - lastCalcY
                        } else {
                            distanceY = lastCalcY - y
                            isUP = true
                        }
                        val distanceX: Float
                        var isRight = false
                        if (x > lastCalcX) {
                            distanceX = x - lastCalcX
                            isRight = true
                        } else {
                            distanceX = lastCalcX - x
                        }
                        //                            float distanceX = lastx - x;
//                            float absDistanceY = Math.abs(distanceY);
//                            float absDistanceX = Math.abs(distanceX);
                        if (!isVertical && !isHorizontal) { //判断本次滑动是横向还是纵向
                            if (distanceX > dpToPx(10f)) {
                                isHorizontal = true
                            } else if (distanceY > dpToPx(5f)) {
                                isVertical = true
                            }
                        }


                        //横向滑动
                        if (isHorizontal) {
                            val maxSize = widthPixels
                            val one = maxSize / 1000
                            val changePosition = (distanceX / one).toInt() * 150
                            if (distanceX >= one) {
                                lastCalcX = x
                            }
                        }
                        if (isVertical) {
                            //竖向滑动
                            val maxSize = heightPixels / 2
                            val one = maxSize / 100
                            if (startX > v.width.toFloat() / 2) { //起始位置在屏幕右边（改变音量）
                                if (distanceY >= one) {
                                    if (isUP) {
                                        //上滑
                                        mEventCallback.onMoveRightUp()
                                    } else {
                                        //下滑
                                        mEventCallback.onMoveRightDown()
                                    }
                                    lastCalcY = y
                                }
                            } else { //起始位置在屏幕左边（改变亮度）
                                if (distanceY >= one) {
                                    if (isUP) {
                                        mEventCallback.onMoveLeftUp()
                                    } else {
                                        mEventCallback.onMoveLeftDown()
                                    }
                                    lastCalcY = y
                                }
                            }
                        }
                    }
                }

                MotionEvent.ACTION_UP -> {
                    logErr("UP")
                    isHorizontal = false
                    isVertical = false
                    val x = event.x
                    val y = event.y
                    isUp = true
                    if (isLongPress) {
                        longPressFinish()
                        return true
                    }
                    val mLastUpTime = System.currentTimeMillis()
                    val mUpX = x.toInt()
                    val mUpY = y.toInt()
                    val mx = abs((mUpX - mDownX).toDouble()).toInt()
                    val my = abs((mUpY - mDownY).toDouble()).toInt()
                    if (mx <= MIN_DISTANCE && my <= MIN_DISTANCE) {
                        if (mLastUpTime - mLastDownTime <= MAX_LONG_PRESS_TIME) {
                            mBaseHandler.removeCallbacks(mLongPressTask)
                            if (!isDoubleClick) {
                                mBaseHandler.postDelayed(
                                    mSingleClickTask!!,
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
            return mEventCallback.onTouch(v, event)
        }

        private val mSingleClickTask: Runnable? = Runnable { // 处理单击
            if (isDoubleClick) {
                return@Runnable
            }
            mClickCount = 0
            mEventCallback.onClick()
        }

        private fun mDoubleClickTask(event: MotionEvent) {
            //处理双击
            isDoubleClick = true
            mClickCount = 0
            mFirstClick = 0
            mSecondClick = 0
            mBaseHandler.removeCallbacks(mSingleClickTask!!)
            mBaseHandler.removeCallbacks(mLongPressTask)
            mEventCallback.onDoubleClick(event)
        }

        private var longPressPositionX = 0f //长按位置
        private var isLongPress = false
        private val isLongPressLeft = false
        private val right_speed_view_animate: ObjectAnimator? = null
        private val left_speed_view_animate: ObjectAnimator? = null
        private val mLongPressTask = Runnable {
            try {
                if (isUp) {
                    return@Runnable
                }
                //处理长按
                mClickCount = 0
                isLongPress = true
                mEventCallback.onLongPress()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun longPressFinish() {
            isLongPress = false
            //长按结束
            if (isLongPressLeft) {
                mEventCallback.longPressFinishLeft()
            } else {
                mEventCallback.longPressFinishRight()
            }
            mEventCallback.onLongPressFinish()
        }

        companion object {
            private const val MAX_LONG_PRESS_TIME = 400 // 长按/双击最长等待时间
            private const val MAX_SINGLE_CLICK_TIME = 420 // 单击后等待的时间
            private const val MIN_DISTANCE = 3 //最小滑动距离
        }
    }
}
