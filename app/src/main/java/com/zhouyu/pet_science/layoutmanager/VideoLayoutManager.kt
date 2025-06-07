package com.zhouyu.pet_science.layoutmanager

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class VideoLayoutManager(context: Context) : LinearLayoutManager(context) {
    
    private val visibilityThreshold = 0.5f // 50%可见度阈值
    
    init {
        orientation = VERTICAL
    }
    
    // 重写此方法以实现自定义的滑动速度和阻尼效果
    override fun smoothScrollToPosition(recyclerView: RecyclerView, state: RecyclerView.State, position: Int) {
        val smoothScroller = object : androidx.recyclerview.widget.LinearSmoothScroller(recyclerView.context) {
            override fun calculateSpeedPerPixel(displayMetrics: android.util.DisplayMetrics): Float {
                // 调整滑动速度，值越大滑动越慢
                return 100f / displayMetrics.densityDpi
            }
        }
        smoothScroller.targetPosition = position
        startSmoothScroll(smoothScroller)
    }
    
    // 重写此方法以实现边缘阻尼效果
    override fun scrollVerticallyBy(dy: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State): Int {
        // 如果已经到达边缘，应用阻尼效果
        if ((dy > 0 && findLastCompletelyVisibleItemPosition() == itemCount - 1) ||
            (dy < 0 && findFirstCompletelyVisibleItemPosition() == 0)) {
            // 应用阻尼效果，减少滑动距离
            return super.scrollVerticallyBy((dy * 0.5f).toInt(), recycler, state)
        }
        return super.scrollVerticallyBy(dy, recycler, state)
    }
    
    // 查找第一个可见的项位置（基于可见面积）
    fun findFirstVisibleItemPositionByArea(): Int {
        val firstVisiblePos = findFirstVisibleItemPosition()
        val lastVisiblePos = findLastVisibleItemPosition()
        
        var maxVisiblePosition = RecyclerView.NO_POSITION
        var maxVisibleArea = 0f
        
        for (i in firstVisiblePos..lastVisiblePos) {
            val view = findViewByPosition(i) ?: continue
            val visibleArea = getVisibleAreaRatio(view)
            
            if (visibleArea > maxVisibleArea && visibleArea >= visibilityThreshold) {
                maxVisibleArea = visibleArea
                maxVisiblePosition = i
            }
        }
        
        return maxVisiblePosition
    }
    
    // 计算视图的可见面积比例
    private fun getVisibleAreaRatio(view: View): Float {
        val itemHeight = view.height.toFloat()
        if (itemHeight <= 0) return 0f
        
        val parent = view.parent as? RecyclerView ?: return 0f
        
        val parentTop = 0
        val parentBottom = parent.height
        
        val viewTop = view.top
        val viewBottom = view.bottom
        
        val visibleTop = Math.max(parentTop, viewTop)
        val visibleBottom = Math.min(parentBottom, viewBottom)
        
        val visibleHeight = Math.max(0, visibleBottom - visibleTop)
        return visibleHeight / itemHeight
    }
    
    // 优化View回收
    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        super.onLayoutChildren(recycler, state)
        if (itemCount <= 0 || state.isPreLayout) return
        
        // 回收不可见的View
        recycleInvisibleViews(recycler)
    }
    
    private fun recycleInvisibleViews(recycler: RecyclerView.Recycler) {
        // 获取屏幕可见区域
        val displayFrame = android.graphics.Rect(0, 0, width, height)
        
        // 检查每个子View是否在可见区域内
        for (i in 0 until childCount) {
            val child = getChildAt(i) ?: continue
            val position = getPosition(child)
            
            // 如果View不在可见区域内，回收它
            if (!displayFrame.intersects(child.left, child.top, child.right, child.bottom)) {
                removeAndRecycleViewAt(i, recycler)
            }
        }
    }
}