package com.zhouyu.pet_science.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;

public class CustomSurfaceView extends SurfaceView {

    private int videoWidth;
    private int videoHeight;

    public CustomSurfaceView(Context context) {
        super(context);
    }

    public CustomSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 设置测量尺寸为视频的实际尺寸
        setMeasuredDimension(videoWidth, videoHeight);
    }

    public void setVideoSize(int width, int height) {
        if (width == videoWidth && height == videoHeight) {
            return;
        }
        videoWidth = width;
        videoHeight = height;
        requestLayout();
    }
}
