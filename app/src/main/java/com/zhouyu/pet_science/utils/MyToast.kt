package com.zhouyu.pet_science.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.zhouyu.pet_science.R
import com.zhouyu.pet_science.application.Application

class MyToast
/**
 * 构造
 */
    (context: Context?) : Toast(context) {
    override fun cancel() {
        if (loadImportanceToast) {
            loadImportanceToast = false
        }
        toast = null
        super.cancel()
    }

    override fun show() {
        super.show()
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var toast_img: ImageView? = null

        /**
         * 图标状态 不显示图标
         */
        private const val TYPE_HIDE = -1

        /**
         * 图标状态 显示√
         */
        private const val TYPE_TRUE = 0

        /**
         * 图标状态 显示×
         */
        private const val TYPE_FALSE = 1

        /**
         * Toast消失计时器
         */
        private val handler = Handler(Looper.getMainLooper())
        private var loadImportanceToast = false

        /**
         * 显示Toast
         *
         * @param text    显示的文本
         * @param time    显示时长
         * @param imgType 图标状态
         */
        private fun showToast(text: CharSequence, time: Int, imgType: Int, isImportance: Boolean) {
            if (Thread.currentThread() === Looper.getMainLooper().thread) {
                showToastF(text, time, imgType, isImportance)
            } else {
                Application.mainHandler.post { showToastF(text, time, imgType, isImportance) }
            }
        }

        private fun showToastF(text: CharSequence, time: Int, imgType: Int, isImportance: Boolean) {
            try {
                if (loadImportanceToast) {
                    return
                }
                loadImportanceToast = isImportance
                // 初始化一个新的Toast对象
                initToast(Application.context, text)
                handler.removeCallbacksAndMessages(null)
                // 设置显示时长
                if (time == LENGTH_LONG) {
                    toast!!.duration = LENGTH_LONG
                    handler.postDelayed({
                        if (loadImportanceToast) {
                            loadImportanceToast = false
                        }
                    }, 4000)
                } else {
                    handler.removeCallbacksAndMessages(null)
                    handler.postDelayed({
                        if (toast != null) {
                            toast!!.cancel()
                        }
                    }, 1000)
                }

                // 判断图标是否该显示，显示√还是×
                if (imgType == TYPE_HIDE) {
                    toast_img!!.visibility = View.GONE
                } else {
                    if (imgType == TYPE_TRUE) {
                        toast_img!!.setBackgroundResource(R.drawable.toast_y)
                    } else {
                        toast_img!!.setBackgroundResource(R.drawable.toast_n)
                    }
                    toast_img!!.visibility = View.VISIBLE

                    /*      //旋转动画
            if (time == Toast.LENGTH_LONG) {
                ObjectAnimator.ofFloat(toast_img, "rotationY", 0, 360).setDuration(1700).start();
            } else{
                ObjectAnimator.ofFloat(toast_img, "rotationY", 0, 360).setDuration(1000).start();
            }*/
                }

                // 显示Toast
                toast!!.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /**
         * 显示一个纯文本吐司
         *
         * @param text    显示的文本
         */
        fun show(text: CharSequence) {
            showToast(text, LENGTH_SHORT, TYPE_HIDE, false)
        }

        /**
         * 显示一个带图标的吐司
         *
         * @param text      显示的文本
         * @param isSucceed 显示【对号图标】还是【叉号图标】
         */
        fun show(text: CharSequence, isSucceed: Boolean) {
            showToast(text, LENGTH_SHORT, if (isSucceed) TYPE_TRUE else TYPE_FALSE, false)
        }

        /**
         * 显示一个纯文本吐司
         *
         * @param text    显示的文本
         * @param time    持续的时间
         */
        @JvmStatic
        fun show(text: CharSequence, time: Int) {
            showToast(text, time, TYPE_HIDE, false)
        }

        /**
         * 显示一个带图标的吐司
         *
         * @param text      显示的文本
         * @param time      持续的时间
         * @param isSucceed 显示【对号图标】还是【叉号图标】
         */
        @JvmStatic
        fun show(text: CharSequence, time: Int, isSucceed: Boolean) {
            showToast(text, time, if (isSucceed) TYPE_TRUE else TYPE_FALSE, false)
        }

        fun show(text: CharSequence, time: Int, isSucceed: Boolean, isImportance: Boolean) {
            showToast(text, time, if (isSucceed) TYPE_TRUE else TYPE_FALSE, isImportance)
        }

        /**
         * 初始化Toast
         *
         * @param context 上下文
         * @param text    显示的文本
         */
        private fun initToast(context: Context, text: CharSequence) {
            try {
                cancelToast()
                toast = MyToast(context)

                // 获取LayoutInflater对象
                val inflater =
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

                // 由layout文件创建一个View对象
                @SuppressLint("InflateParams") val layout =
                    inflater.inflate(R.layout.layout_toast, null)

                // 吐司上的图片
                toast_img = layout.findViewById(R.id.toast_img)

                // 吐司上的文字
                val toastText = layout.findViewById<TextView>(R.id.toast_text)
                toastText.text = text
                toast!!.view = layout
                toast!!.setGravity(Gravity.CENTER, 0, 70)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /**
         * Toast单例
         */
        @SuppressLint("StaticFieldLeak")
        private var toast: MyToast? = null

        /**
         * 隐藏当前Toast
         */
        private fun cancelToast() {
            if (toast != null) {
                toast!!.cancel()
                toast = null
            }
            if (toast_img != null) {
                toast_img = null
            }
        }
    }
}
