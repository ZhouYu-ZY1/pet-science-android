package com.zhouyu.pet_science.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.zhouyu.pet_science.R;
import com.zhouyu.pet_science.tools.utils.PhoneMessage;

public class RightLetterSort extends View {
    private final Paint textPaint = new Paint();
    private final Paint textPaint2 = new Paint();
    private final Paint textPaint3 = new Paint();
    private final Paint bubblePaint = new Paint();
    public static final String[] letter = new String[]{
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
            "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z"};
    //鼠标点击、滑动时选择的字母
    private int choose = -1;
    private String now = "";
    private int bubbleRadius = 25;
    private Path path;

    public RightLetterSort(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }
    public RightLetterSort(Context context) {
        super(context);
        initPaint();
    }
    public RightLetterSort(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }
    private void initPaint() {
        Context context = getContext();
        textPaint.setTextSize(PhoneMessage.dpToPx(11));
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.parseColor("#818792"));

        textPaint2.setTextSize(PhoneMessage.dpToPx(27));
        textPaint2.setAntiAlias(true);
        textPaint2.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint2.setColor(Color.WHITE);

        textPaint3.setTextSize(PhoneMessage.dpToPx(12));
        textPaint3.setAntiAlias(true);
        textPaint3.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint3.setColor(context.getColor(R.color.Theme));

        bubblePaint.setColor(0xffcccccc);
        bubblePaint.setStyle(Paint.Style.FILL);
        bubblePaint.setStrokeWidth(5);
        bubblePaint.setAntiAlias(true);


        bubbleRadius = PhoneMessage.dpToPx(bubbleRadius);
        path = new Path();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //画字母
        drawText(canvas);
        drawBuble(canvas);
    }


    private void drawBuble(Canvas canvas){
        if(choose >= 0){
            float x = (-getWidth() * 3f) / 2f - PhoneMessage.dpToPxFloat(15);
            float singleHeight = (float) getHeight() / letter.length;
            float y= singleHeight/2f + (singleHeight*choose);
            canvas.drawCircle(x, y, bubbleRadius, bubblePaint);


            path.reset();
            path.moveTo(x,y-bubbleRadius);
            path.lineTo(x,y+bubbleRadius);
            path.lineTo(x + bubbleRadius/ 4f * 6,y);
            path.close();
            canvas.drawPath(path,bubblePaint);
            float baseLineY = y+ (Math.abs(textPaint2.ascent() + textPaint2.descent()) / 2);
            canvas.drawText(now, x-textPaint2.measureText(now)/2,baseLineY, textPaint2);
        }
    }



    /**
     * 画字母
     */
    private void drawText(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        //获取每个字母的高度
        int singleHeight = height / letter.length;
        //画字母
        for (int i = 0; i < letter.length; i++) {
            if(i == choose){
                float x =(width- textPaint3.measureText(letter[i]))/2;
                float baseLineY =i*singleHeight+ (singleHeight / 2f) + (Math.abs(textPaint3.ascent() + textPaint3.descent()) / 2);
                canvas.drawText(letter[i], x, baseLineY, textPaint3);
            }else {
                float x =(width- textPaint.measureText(letter[i]))/2;
                float baseLineY =i*singleHeight+ (singleHeight / 2f) + (Math.abs(textPaint.ascent() + textPaint.descent()) / 2);
                canvas.drawText(letter[i], x, baseLineY, textPaint);
            }
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        //计算选中字母
        int index = (int) (event.getY() / getHeight() * letter.length);
        //防止脚标越界
        if (index >= letter.length) {
            index = letter.length - 1;
        } else if (index < 0) {
            index = 0;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if(choose == index){
                    return true;
                }
                choose = index;
                now= letter[choose];
                invalidate();
                if (listener != null) {
                    listener.touchCharacterListener(now);
                }
                break;
            default:
                choose = -1;
                invalidate();
                break;
        }
        return true;
    }
    public onTouchCharacterListener listener;
    public interface onTouchCharacterListener {
        void touchCharacterListener(String s);
    }
    public void setListener(onTouchCharacterListener listener) {
        this.listener = listener;
    }

    public void invalidateView() {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            invalidate();
        } else {
            postInvalidate();
        }
    }
}

