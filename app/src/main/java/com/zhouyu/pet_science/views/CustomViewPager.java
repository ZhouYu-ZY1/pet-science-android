package com.zhouyu.pet_science.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class CustomViewPager extends ViewPager {
    public CustomViewPager(@NonNull Context context) {
        super(context);
    }

    public CustomViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

//    @Override
//    public void setCurrentItem(int item) {
//        super.setCurrentItem(item);
//        currPosition = item;
//    }
//
//    float startX = 0;
//    float distance = 0;
//    float distanceEnd = 0;
//    private int currPosition = 0;
//    @SuppressLint("ClickableViewAccessibility")
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        try {
//            if(viewPageEdgeSlideListener != null){
//                float x = event.getX();
//                switch (event.getAction()){
//                    case MotionEvent.ACTION_MOVE:
//                        if(startX == 0){
//                            startX = x;
//                        }
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        distance = x - startX;
//                        distanceEnd = startX - x;
//                        startX = 0;
//                        if(currPosition == 0 && PhoneMessage.pxToDp(distance) > 50){
//                            viewPageEdgeSlideListener.onStartPageEdgeSlide();
//                        }
//                        if(currPosition == Objects.requireNonNull(getAdapter()).getCount()-1 && PhoneMessage.pxToDp(distanceEnd) > 50){
//                            viewPageEdgeSlideListener.onEndPageEdgeSlide();
//                        }
//                        break;
//                }
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        return super.onTouchEvent(event);
//    }
//
//    private ViewPageEdgeSlideListener viewPageEdgeSlideListener;
//    public void setViewPageEdgeSlideListener(ViewPageEdgeSlideListener viewPageEdgeSlideListener) {
//        this.viewPageEdgeSlideListener = viewPageEdgeSlideListener;
//    }
//
//    public interface ViewPageEdgeSlideListener{
//        void onStartPageEdgeSlide();
//        void onEndPageEdgeSlide();
//    }


    /**
     * 解决HorizontalScrollView和ViewPager滑动冲突问题
     */
    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        if (v instanceof HorizontalScrollView) {
            return true;
        }
        return super.canScroll(v, checkV, dx, x, y);
    }
}
