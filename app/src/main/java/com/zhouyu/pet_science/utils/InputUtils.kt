package com.zhouyu.pet_science.utils

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

object InputUtils {
    /**
     * 让输入框始终在组件下方
     * @param parentView 根布局
     * @param childView 需要显示的最下方View
     */
    fun addLayoutListener(parentView: View, childView: View) {
        parentView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            parentView.getWindowVisibleDisplayFrame(rect)
            val mainInvisibleHeight = parentView.rootView.height - rect.bottom
            if (mainInvisibleHeight > 100) {
                val location = IntArray(2)
                childView.getLocationInWindow(location)
                val srollHeight = location[1] + childView.height - rect.bottom
                parentView.scrollTo(0, srollHeight)
            } else {
                parentView.scrollTo(0, 0)
            }
        }
    }

    /**
     * 隐藏软键盘
     */
    fun inputHide(context: Context, input: EditText) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val isOpen = imm.isActive
        if (isOpen) {
            imm.hideSoftInputFromWindow(input.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    /**
     * 弹出软键盘
     */
    @JvmStatic
    fun inputShow(context: Context, input: EditText) {
        input.requestFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun isKeyboardShown(rootView: View): Boolean {
        val softKeyboardHeight = 100
        val r = Rect()
        rootView.getWindowVisibleDisplayFrame(r)
        val dm = rootView.resources.displayMetrics
        val heightDiff = rootView.bottom - r.bottom
        return heightDiff > softKeyboardHeight * dm.density
    }

    class KeyboardListener(activity: Activity, onKeyboardListener: OnKeyboardListener?) {
        // 屏幕可见区域
        private val windowVisibleDisplayFrame = Rect()

        // 保存最后可视区域高度
        private var lastKeyboardHeight = 0
        private val decorView: View
        private val onGlobalLayoutListener: OnGlobalLayoutListener

        init {
            decorView = activity.window.decorView
            onGlobalLayoutListener = OnGlobalLayoutListener {

                // 获取屏幕可视区域
                decorView.getWindowVisibleDisplayFrame(windowVisibleDisplayFrame)
                //屏幕高度
                val height = decorView.height
                // 获取键盘高度
                val currentKeyboardHeight = height - windowVisibleDisplayFrame.bottom
                if (lastKeyboardHeight != currentKeyboardHeight) {
                    if (isKeyboardShown(decorView)) { // 键盘可见时回调
                        onKeyboardListener?.onKeyboardShown(currentKeyboardHeight)
                    } else { // 键盘隐藏时回调
                        onKeyboardListener?.onKeyboardHidden()
                    }
                    lastKeyboardHeight = currentKeyboardHeight
                }
            }
            decorView.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)
        }

        fun removeKeyboardListener() {
            //防止内存泄漏
            decorView.viewTreeObserver.removeOnGlobalLayoutListener(onGlobalLayoutListener)
        }
    }

    interface OnKeyboardListener {
        fun onKeyboardShown(currentKeyboardHeight: Int)
        fun onKeyboardHidden()
    }
}
