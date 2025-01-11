package com.zhouyu.android_create.tools.utils;

import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewUtils {
    private final RecyclerView recyclerView;

    public RecyclerViewUtils(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;

    }

    public int getScrollY(){
        try {
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            int position = layoutManager.findFirstVisibleItemPosition();
            View firstVisiableChildView = layoutManager.findViewByPosition(position);
            int itemHeight = firstVisiableChildView.getHeight();
            return (position) * itemHeight - firstVisiableChildView.getTop();
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    public int getScrollY(int headHeight){
        try {
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            int position = layoutManager.findFirstVisibleItemPosition();
            View firstVisiableChildView = layoutManager.findViewByPosition(position);
            if(firstVisiableChildView == null){
                return 0;
            }
            int itemHeight = firstVisiableChildView.getHeight();
            int scrollY  = position * itemHeight - firstVisiableChildView.getTop();
            if(position > 0){
                scrollY += headHeight;
            }
            return scrollY;
        }catch (Exception e){
            return 0;
        }
    }
}
