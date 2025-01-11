package com.zhouyu.android_create.adapter.itemHelper;

import androidx.recyclerview.widget.RecyclerView;

public interface ItemTouchHelperAdapter {

    //数据交换
    void onItemMove(RecyclerView.ViewHolder source, RecyclerView.ViewHolder target);

    //数据删除
    void onItemDismiss(RecyclerView.ViewHolder source);

    //drag或者swipe选中
    void onItemSelect(RecyclerView.ViewHolder source);

    //状态清除
    void onItemClear(RecyclerView.ViewHolder source);
}
