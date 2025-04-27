package com.zhouyu.pet_science.views.scroll

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.zhouyu.pet_science.utils.PhoneMessage.dpToPx

class BottomSpaceItemDecoration(bottomSpace: Int) : ItemDecoration() {
    private val bottomSpace: Int

    init {
        this.bottomSpace = dpToPx(bottomSpace.toFloat())
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        // 可以为所有项目添加底部空间，或者只为最后一个项目添加
        if (parent.adapter != null) {
            if (parent.getChildAdapterPosition(view) == parent.adapter!!.itemCount - 1) {
                outRect.bottom = bottomSpace
            }
            // 或者，如果你想要为所有项目都添加空间，可以去掉上面的if条件
        }
    }
}
