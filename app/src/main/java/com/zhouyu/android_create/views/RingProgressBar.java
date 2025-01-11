package com.zhouyu.android_create.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.zhouyu.android_create.R;

@SuppressLint("ViewConstructor")
public class RingProgressBar extends View {
    /**
     * 画笔对象的引用
     */
    private final Paint paint;

    /**
     * 圆环的颜色
     */
    private final int ringColor;
    /**
     * 中间圆的颜色
     */
    private final int circleColor;
    /**
     * 圆环进度的颜色
     */
    private final int progressColor;

    /**
     * 中间进度百分比的字符串的颜色
     */
    private final int textColor;

    /**
     * 中间进度百分比的字符串的字体
     */
    private final float textSize;

    /**
     * 外层圆圈的宽度
     */
    private final float outerRingWidth;

    /**
     * 最大进度
     */
    private final int max;

    /**
     * 当前进度
     */
    private int progress;
    /**
     * 是否显示中间的进度
     */
    private final boolean textIsVisibility;

    /**
     * 进度的风格，实心或者空心
     */
    private final int style;

    public static final int STROKE = 0;
    public static final int FILL = 1;

    public RingProgressBar(Context context) {
        this(context, null);
    }

    public RingProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RingProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        paint = new Paint();
        paint.setAntiAlias(true);//消除锯齿
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.RingProgressBar);
        //获取自定义属性和默认值
        max = mTypedArray.getInteger(R.styleable.RingProgressBar_rMax, 100);//进度最大值
        textColor = mTypedArray.getColor(R.styleable.RingProgressBar_rTextColor, Color.GREEN);//字体颜色
        textSize = mTypedArray.getDimension(R.styleable.RingProgressBar_rTextSize, 40);//字体大小
        circleColor = mTypedArray.getColor(R.styleable.RingProgressBar_rCircleColor, Color.TRANSPARENT);//中间圆的颜色,默认透明
        ringColor = mTypedArray.getColor(R.styleable.RingProgressBar_rRingColor, Color.TRANSPARENT);//外层初始圆环的颜色，默认透明
        progressColor = mTypedArray.getColor(R.styleable.RingProgressBar_rProgressColor, Color.BLUE);//外层进度环的颜色，默认蓝色
        textIsVisibility = mTypedArray.getBoolean(R.styleable.RingProgressBar_rTextIsVisibility, true);//文字是否可见，默认可见
        outerRingWidth = mTypedArray.getDimension(R.styleable.RingProgressBar_outerRingWidth, 10);
        progress = mTypedArray.getInteger(R.styleable.RingProgressBar_progress, 100);
        style = mTypedArray.getInt(R.styleable.RingProgressBar_style, 0);
        mTypedArray.recycle();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float ringWidth = outerRingWidth;
        //1.画最外层的圆
        float centre = getWidth() / 2f; //获取圆心的x坐标
        float radius = centre - ringWidth / 2; //圆环的半径

        paint.setStrokeWidth(ringWidth); //设置圆环的宽度
        paint.setColor(ringColor); //设置圆环的颜色
        paint.setStyle(Paint.Style.STROKE); //设置空心
        canvas.drawCircle(centre, centre, radius, paint); //画出圆环
        //2.画中间圆环
        paint.setColor(circleColor);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(centre, centre, centre - ringWidth, paint); //画出圆环
        //3.画进度圆环
        paint.setStrokeWidth(ringWidth); //设置圆环的宽度
        paint.setColor(progressColor); //设置进度的颜色
        @SuppressLint("DrawAllocation")
        RectF oval = new RectF(centre - radius, centre - radius, centre + radius, centre + radius); //用于定义的圆弧的形状和大小的界限
        switch (style) {
            case STROKE: {//空心进度环
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawArc(oval, -90, 360f * progress / max, false, paint);  //根据进度画圆弧
                break;
            }
            case FILL: {//实心进度环
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                if (progress != 0) {
                    canvas.drawArc(oval, -90, 360f * progress / max, true, paint);  //根据进度画圆弧
                }
                break;
            }
        }

        //4.画进度百分比
        if (textIsVisibility) {
            paint.setStrokeWidth(0);
            paint.setColor(textColor);
            paint.setTextSize(textSize);
            paint.setTypeface(Typeface.DEFAULT); //设置字体
            int percent = (int) (((float) progress / (float) max) * 100); //中间的进度百分比，先转换成float在进行除法运算，不然都为0
            float textWidth = paint.measureText(percent + "%");   //测量字体宽度，我们需要根据字体的宽度设置在圆环中间
            canvas.drawText(percent + "%", centre - textWidth / 2, centre + textSize / 2, paint); //画出进度百分比
        }
    }

    /**
     * 设置进度，此为线程安全控件，由于考虑多线的问题，需要同步
     * 刷新界面调用postInvalidate()能在非UI线程刷新
     */
    public synchronized void setProgress(int progress) {
        if (progress < 0) {
            throw new IllegalArgumentException("progress not less than 0");
        }
        if (progress > max) {
            progress = max;
        }
        this.progress = progress;
        postInvalidate();
    }
}

