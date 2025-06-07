package com.zhouyu.pet_science.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.TextView

/**
 * 自动滚动文字
 */
@SuppressLint("AppCompatCustomView")
class RollTextView : TextView {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    override fun isFocused(): Boolean {
        return isOpen
    }

    private var isOpen: Boolean = true
    fun setOpen(isOpen: Boolean) {
        this.isOpen = isOpen
        requestLayout()
    }
}
