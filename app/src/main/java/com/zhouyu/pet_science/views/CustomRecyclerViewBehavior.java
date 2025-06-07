package com.zhouyu.pet_science.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;

public class CustomRecyclerViewBehavior extends CoordinatorLayout.Behavior<RecyclerView> {
    public CustomRecyclerViewBehavior() {
    }

    public CustomRecyclerViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                       @NonNull RecyclerView child,
                                       @NonNull View directTargetChild,
                                       @NonNull View target,
                                       int axes,
                                       int type) {
        return true;
    }

    @Override
    public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                  RecyclerView child,
                                  @NonNull View target,
                                  int dx,
                                  int dy,
                                  @NonNull int[] consumed,
                                  int type) {
        // 让NestedScrollView先处理滚动
        ViewParent parent = child.getParent();
        if (parent instanceof CoordinatorLayout) {
            CoordinatorLayout coordinator = (CoordinatorLayout) parent;
            for (int i = 0; i < coordinator.getChildCount(); i++) {
                View view = coordinator.getChildAt(i);
                if (view instanceof NestedScrollView) {
                    view.dispatchNestedPreScroll(dx, dy, consumed, null);
                    break;
                }
            }
        }
        
        // 如果NestedScrollView没有消耗完滚动量，再让RecyclerView处理
        int remainingDy = dy - consumed[1];
        if (remainingDy != 0) {
            super.onNestedPreScroll(coordinatorLayout, child, target, dx, remainingDy, consumed, type);
        }
    }
}