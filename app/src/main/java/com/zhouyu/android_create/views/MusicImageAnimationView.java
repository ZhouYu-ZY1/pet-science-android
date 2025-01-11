package com.zhouyu.android_create.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import com.zhouyu.android_create.tools.utils.PhoneMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MusicImageAnimationView extends View {
    /** 扩散圆圈颜色 */
    private int mColor = 0xFFEDE8E8;
    /** 中心圆半径 */
//    private float mCoreRadius = (PhoneMessage.getWidthPixels() * 1000f) / 1700 / 2;
    private float mCoreRadius = PhoneMessage.dpToPxFloat(250) / 2f;

    /** 扩散圆宽度 */
    private float mDiffuseWidth = PhoneMessage.dpToPxFloat(6); //ok
//    private int mDiffuseWidth = 4;

    /** 扩散圆边框宽度*/
    private final int mRingWidth = PhoneMessage.dpToPx(1.5f); //ok

    /** 最大宽度 */
    private Integer mMaxWidth = PhoneMessage.getWidthPixels(); //ok

    /** 扩散速度 */
    private float mDiffuseSpeed = 0.7f; //1542
//    private float mDiffuseSpeed = 0.8f;

    /** 是否正在扩散中 */
    private boolean mIsDiffuse = false;
    // 透明度集合
    private final List<Float> mAlphas = new ArrayList<>();
    // 扩散圆半径集合
    private final List<Float> mWidths = new ArrayList<>();

    private Paint mPaint;
    private Paint dotPaint;
    /** 圆点Color*/
    private int dotColor = 0xFFFFFFFF;
    /** 圆点位置集合*/
    private final List<Integer> locationRList = new ArrayList<>();
    /** 圆点大小集合*/
    private final List<Integer> sizeRList = new ArrayList<>();
    private final int[] sizes = {3,4,4,5};

    public MusicImageAnimationView(Context context) {
        this(context, null);
    }

    public MusicImageAnimationView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public MusicImageAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mAlphas.add(150f);
        mWidths.add(0f);
        locationRList.add(random.nextInt(4));
        sizeRList.add(sizes[random.nextInt(4)]);

        dotPaint = new Paint();
        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setAntiAlias(true);
    }

    public void invalidateView() {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }



    private float currDegrees = 0;
    private final Random random = new Random();
    @Override
    public void onDraw(Canvas canvas) {
        currDegrees -= 0.1f;
        //旋转
        canvas.rotate(currDegrees,getWidth()/2f,getHeight()/2f);
        // 绘制扩散圆
        mPaint.setColor(mColor);
        dotPaint.setColor(dotColor);
        mPaint.setStrokeWidth(mRingWidth);
        for (int i = 0; i < mAlphas.size(); i ++) {
            // 设置透明度
            Float alpha = mAlphas.get(i);
            int intValue = alpha.intValue();
            mPaint.setAlpha(intValue);
            dotPaint.setAlpha(intValue);
            // 绘制扩散圆
            Float width = mWidths.get(i);
            canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, mCoreRadius + width, mPaint);

            @SuppressLint("DrawAllocation")
            int r = locationRList.get(i);
            int size = sizeRList.get(i);
            switch (r){
                case 0:
                    canvas.drawCircle(getWidth() / 2f,getHeight() / 2f - (mCoreRadius + width),PhoneMessage.dpToPx(size),dotPaint);
                    break;
                case 1:
                    canvas.drawCircle(getWidth() / 2f - (mCoreRadius + width),getHeight() / 2f,PhoneMessage.dpToPx(size),dotPaint);
                    break;
                case 2:
                    canvas.drawCircle(getWidth() / 2f + (mCoreRadius + width),getHeight() / 2f,PhoneMessage.dpToPx(size),dotPaint);
                    break;
                case 3:
                    canvas.drawCircle(getWidth() / 2f,getHeight() / 2f + (mCoreRadius + width),PhoneMessage.dpToPx(size),dotPaint);
                    break;

            }
            if(alpha > 0 && width < mMaxWidth){
                mAlphas.set(i, alpha - 1 > 0 ? alpha - 0.5f : 0);
                mWidths.set(i, width + mDiffuseSpeed);
            }
        }
        if(!isStop){
            // 判断当扩散圆扩散到指定宽度时添加新扩散圆
            if (mWidths.get(mWidths.size() - 1) >= (float) mMaxWidth / mDiffuseWidth) {
                mAlphas.add(150f);
                mWidths.add(0f);
                locationRList.add(random.nextInt(4));
                sizeRList.add(sizes[random.nextInt(4)]);
            }
        }

        if(mWidths.size() == 0){
            return;
        }

        // 超过10个扩散圆，删除最外层
        if(mWidths.size() >= 5){
            mWidths.remove(0);
            mAlphas.remove(0);
            locationRList.remove(0);
            sizeRList.remove(0);
        }

        if(mIsDiffuse){
            invalidateView();
        }
    }

    /**
     * 开始扩散
     */
    public void start() {
        if(mWidths.size() == 0){
            mAlphas.add(150f);
            mWidths.add(0f);
            locationRList.add(random.nextInt(4));
            sizeRList.add(sizes[random.nextInt(4)]);
        }

        mIsDiffuse = true;
        isStop = false;
        invalidateView();
    }

    private boolean isStop;
    /**
     * 停止扩散
     */
    public void stop() {
        isStop = true;
        invalidateView();
    }

    /**
     * 暂停扩散
     */
    public void pause(){
        mIsDiffuse = false;
        isStop = false;
        invalidateView();
    }

    /**
     * 清除扩散效果
     */
    public void clear(){
        mIsDiffuse = false;
        isStop = true;
        mAlphas.clear();
        mWidths.clear();
        locationRList.clear();
        sizeRList.clear();
        invalidateView();
    }

    /**
     * 是否扩散中
     */
    public boolean isDiffuse(){
        return mIsDiffuse;
    }

    /**
     * 设置扩散圆颜色
     */
    public void setColor(int colorId){
        mColor = colorId;
    }



    /**
     * 设置中心圆半径
     */
    public void setCoreRadius(int radius){
        mCoreRadius = radius;
    }

    /**
     * 设置扩散圆宽度(值越小宽度越大)
     */
    public void setDiffuseWidth(int width){
        mDiffuseWidth = width;
    }

    /**
     * 设置最大宽度
     */
    public void setMaxWidth(int maxWidth){
        mMaxWidth = maxWidth;
    }

    /**
     * 设置扩散速度，值越大速度越快
     */
    public void setDiffuseSpeed(int speed){
        mDiffuseSpeed = speed;
    }


    public void setDotColor(int dotColor) {
        this.dotColor = dotColor;
    }
}