package com.zhouyu.pet_science.views.scroll;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zhouyu.pet_science.tools.utils.PhoneMessage;

public class BottomSpaceItemDecoration extends RecyclerView.ItemDecoration {
    private final int bottomSpace;

    public BottomSpaceItemDecoration(int bottomSpace) {
        this.bottomSpace = PhoneMessage.dpToPx(bottomSpace);
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        // 可以为所有项目添加底部空间，或者只为最后一个项目添加
        if(parent.getAdapter() != null){
            if (parent.getChildAdapterPosition(view) == parent.getAdapter().getItemCount() - 1) {
                outRect.bottom = bottomSpace;
            }
            // 或者，如果你想要为所有项目都添加空间，可以去掉上面的if条件
        }

    }
}
