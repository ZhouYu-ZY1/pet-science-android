package com.zhouyu.pet_science.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import com.zhouyu.pet_science.utils.PhoneMessage;

import java.util.Arrays;

public class AudioWaveAnimationView extends View {
    private Paint paint;
    private float[] waveHeights;
    private int currentIndex = 0; // 当前活跃的竖线索引
    private boolean isAnimating = true; // 是否正在动画
    private boolean isAscending = true; // 竖线是否正在上升
    private boolean isPositive = true; // 竖线是否正在上升
    private Handler handler = new Handler();
    private Runnable animatorRunnable;

    public AudioWaveAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(PhoneMessage.dpToPx(5));
        waveHeights = new float[3];
        Arrays.fill(waveHeights, getHeight()); // 初始时竖线在底部


//        final int changeHeight = PhoneMessage.dpToPx(1);
        animatorRunnable = new Runnable() {
            @Override
            public void run() {
                if (isAnimating) {
                    if (isAscending) {
                        // 竖线上升
                        updateWaveHeight(currentIndex, true);
                        if (waveHeights[currentIndex] >= getHeight()) {
                            // 到达顶部，准备回缩
                            isAscending = false;
                        }
                        postDelayed(this, 10);
                    } else {
                        // 竖线回缩
                        updateWaveHeight(currentIndex, false);
                        if (waveHeights[currentIndex] <= 0) {
                            // 回到底部，准备切换到下一条线
                            if(isPositive){
                                currentIndex = currentIndex + 1;
                                if(currentIndex == waveHeights.length - 1){
                                    isPositive = false;
                                }
                            }else {
                                currentIndex = currentIndex - 1;
                                if(currentIndex == 0){
                                    isPositive = true;
                                }
                            }
                            isAscending = true; // 总是开始下一条线的上升

                        }
                        postDelayed(this, 30);
                    }
                    invalidate(); // 请求重绘
                }
            }
        };

        handler.postDelayed(animatorRunnable, 0); // 启动动画
    }

    private void updateWaveHeight(int index, boolean isUp) {
        if (isUp) {
            waveHeights[index] = Math.min(waveHeights[index] + 5, getHeight());
        } else {
            waveHeights[index] = Math.max(waveHeights[index] - 5, 0);
        }
    }

    private final float someSpacing = PhoneMessage.dpToPx(1);
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int waveCount = waveHeights.length;
        if (waveCount % 2 == 0) {
           return;
        }

        float centerX = getWidth() / 2f; // 视图中心的 x 坐标
        float lineSpacing = (getWidth() - (waveCount - 1) * someSpacing) / waveCount; // 线条之间的间隔，someSpacing 是你想要的额外间隔

        for (int i = 0; i < waveHeights.length; i++) {
            float x = centerX - (waveCount / 2 - i) * lineSpacing; // 计算每个线条的 x 坐标
            float startY = getHeight(); // 线条的起始 y 坐标（通常是底部）
            float endY = waveHeights[i]; // 线条的结束 y 坐标（根据波高）
            canvas.drawLine(x, startY, x, endY, paint); // 绘制线条
        }
    }

    // 暂停和继续方法
    public void pauseAnimation() {
        isAnimating = false;
        handler.removeCallbacks(animatorRunnable);
    }

    public void resumeAnimation() {
        isAnimating = true;
        // 如果需要，可以从当前状态继续动画，但在这个简单的例子中，我们直接重新开始
        // 或者你可以保存当前状态并在恢复时恢复它
        handler.postDelayed(animatorRunnable, 0);
    }
}