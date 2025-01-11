package com.zhouyu.android_create.views;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.PopupMenu;

import androidx.annotation.RequiresApi;

public class MyPopupMenu extends PopupMenu {
    public MyPopupMenu(Context context, View anchor) {
        super(context, anchor);
    }

    public MyPopupMenu(Context context, View anchor, int gravity) {
        super(context, anchor, gravity);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    public MyPopupMenu(Context context, View anchor, int gravity, int popupStyleAttr, int popupStyleRes) {
        super(context, anchor, gravity, popupStyleAttr, popupStyleRes);
    }

    //显示icon
    @Override
    public void setForceShowIcon(boolean forceShowIcon) {
        super.setForceShowIcon(false);
    }
}
