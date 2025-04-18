package com.zhouyu.pet_science.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhouyu.pet_science.R;
import com.zhouyu.pet_science.application.Application;

public class MyToast extends Toast {
    private static ImageView toast_img;
    /**
     * 图标状态 不显示图标
     */
    private static final int TYPE_HIDE = -1;
    /**
     * 图标状态 显示√
     */
    private static final int TYPE_TRUE = 0;
    /**
     * 图标状态 显示×
     */
    private static final int TYPE_FALSE = 1;

    /**
     * Toast消失计时器
     */
    private static final Handler handler = new Handler(Looper.getMainLooper());

    private static boolean loadImportanceToast;
    /**
     * 显示Toast
     *
     * @param text    显示的文本
     * @param time    显示时长
     * @param imgType 图标状态
     */
    private static void showToast(CharSequence text, int time, int imgType,boolean isImportance) {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            showToastF(text,time,imgType,isImportance);
        }else {
            Application.mainHandler.post(() -> {
                showToastF(text,time,imgType,isImportance);
            });
        }
    }
    private static void showToastF(CharSequence text, int time, int imgType,boolean isImportance){
        try {
            if(loadImportanceToast){
                return;
            }
            loadImportanceToast = isImportance;
            // 初始化一个新的Toast对象
            initToast(Application.context,text);

            handler.removeCallbacksAndMessages(null);
            // 设置显示时长
            if (time == Toast.LENGTH_LONG) {
                toast.setDuration(Toast.LENGTH_LONG);
                handler.postDelayed(() -> {
                    if(loadImportanceToast){
                        loadImportanceToast = false;
                    }
                },4000);
            } else{
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(() -> {
                    if(toast != null){
                        toast.cancel();
                    }
                },1000);
            }

            // 判断图标是否该显示，显示√还是×
            if (imgType == TYPE_HIDE) {
                toast_img.setVisibility(View.GONE);
            } else {
                if (imgType == TYPE_TRUE) {
                    toast_img.setBackgroundResource(R.drawable.toast_y);
                } else {
                    toast_img.setBackgroundResource(R.drawable.toast_n);
                }
                toast_img.setVisibility(View.VISIBLE);

      /*      //旋转动画
            if (time == Toast.LENGTH_LONG) {
                ObjectAnimator.ofFloat(toast_img, "rotationY", 0, 360).setDuration(1700).start();
            } else{
                ObjectAnimator.ofFloat(toast_img, "rotationY", 0, 360).setDuration(1000).start();
            }*/
            }

            // 显示Toast
            toast.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 显示一个纯文本吐司
     *
     * @param text    显示的文本
     */
    public static void show(CharSequence text) {
        showToast(text, Toast.LENGTH_SHORT, TYPE_HIDE,false);
    }

    /**
     * 显示一个带图标的吐司
     *
     * @param text      显示的文本
     * @param isSucceed 显示【对号图标】还是【叉号图标】
     */
    public static void show(CharSequence text, boolean isSucceed) {
        showToast(text, Toast.LENGTH_SHORT, isSucceed ? TYPE_TRUE : TYPE_FALSE,false);
    }

    /**
     * 显示一个纯文本吐司
     *
     * @param text    显示的文本
     * @param time    持续的时间
     */
    public static void show(CharSequence text, int time) {
        showToast(text, time, TYPE_HIDE,false);
    }

    /**
     * 显示一个带图标的吐司
     *
     * @param text      显示的文本
     * @param time      持续的时间
     * @param isSucceed 显示【对号图标】还是【叉号图标】
     */
    public static void show(CharSequence text, int time, boolean isSucceed) {
        showToast(text, time, isSucceed ? TYPE_TRUE : TYPE_FALSE,false);
    }

    public static void show(CharSequence text, int time, boolean isSucceed,boolean isImportance) {
        showToast(text, time, isSucceed ? TYPE_TRUE : TYPE_FALSE,isImportance);
    }


    /**
     * 初始化Toast
     *
     * @param context 上下文
     * @param text    显示的文本
     */
    private static void initToast(Context context, CharSequence text) {
        try {
            cancelToast();

            toast = new MyToast(context);

            // 获取LayoutInflater对象
            LayoutInflater inflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // 由layout文件创建一个View对象
            @SuppressLint("InflateParams") View layout = inflater.inflate(R.layout.layout_toast, null);

            // 吐司上的图片
            toast_img = layout.findViewById(R.id.toast_img);

            // 吐司上的文字
            TextView toast_text = layout.findViewById(R.id.toast_text);
            toast_text.setText(text);
            toast.setView(layout);
            toast.setGravity(Gravity.CENTER, 0, 70);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Toast单例
     */
    private static MyToast toast;

    /**
     * 构造
     */
    public MyToast(Context context) {
        super(context);
    }
    /**
     * 隐藏当前Toast
     */
    public static void cancelToast() {
        if (toast != null) {
            toast.cancel();
            toast = null;
        }
        if(toast_img != null){
            toast_img = null;
        }
    }

    @Override
    public void cancel() {
        if(loadImportanceToast){
            loadImportanceToast = false;
        }
        toast = null;
        super.cancel();
    }

    @Override
    public void show() {
        super.show();
    }

}
