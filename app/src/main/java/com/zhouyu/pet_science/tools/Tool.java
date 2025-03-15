package com.zhouyu.pet_science.tools;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.zhouyu.pet_science.R;
import com.zhouyu.pet_science.application.Application;
import com.zhouyu.pet_science.tools.utils.ConsoleUtils;
import com.zhouyu.pet_science.tools.utils.PhoneMessage;
import com.zhouyu.pet_science.views.dialog.MyDialog;

import java.util.ArrayList;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class Tool {
    /**
     * 设置全屏
     * @param activity 要全屏的activity
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    public static void fullScreen(final Activity activity) {
        if(activity == null){
            return;
        }
        Window window = activity.getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        layoutParams.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.setAttributes(layoutParams);
    }

    /**
     * 时间转换
     * @param milli_time 毫秒值
     * @return 格式（00:00）
     */
    public static String timeCycle(long milli_time){
        if(milli_time == 0){
            return "00:00";
        }
        long time = milli_time / 1000;
        long m = time/60;
        long s = time%60;

        boolean mIsOver = false;
        boolean sIsOver = false;
        if(m < 10){
            mIsOver = true;
        }
        if(s < 10){
            sIsOver = true;
        }

        if(!mIsOver && !sIsOver){
            return m + ":" + s;
        }else if(mIsOver && !sIsOver){
            return "0"+m + ":" + s;
        }else if(!mIsOver){
            return m + ":0" + s;
        }else{
            return "0"+ m + ":0" + s;
        }
    }

    public final static int FILE_SELECT_CODE = 10011;
    public static void openFileSelector(AppCompatActivity activity,int maxCount,String defaultPath,String... fileTypes){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            final MyDialog myDialog2 = new MyDialog(activity);
            myDialog2.setTitle("所有文件访问权限");
            myDialog2.setMessage("由于Android 11以上系统限制,需要所有文件访问权限才能获取文件目录");
            myDialog2.setYesOnclickListener("去开启", () -> {
                myDialog2.dismiss();
                XXPermissions.with(activity)
                        // 适配 Android 11 需要这样写，这里无需再写 Permission.Group.STORAGE
                        .permission(Permission.MANAGE_EXTERNAL_STORAGE)
                        .request((permissions, all) -> {
                        });
            });
            myDialog2.setNoOnclickListener("取消", myDialog2::dismiss);
            myDialog2.show();
        }else {

        }
    }


    /**
     * 底部弹框
     */
    public static class MyBottomSheetDialog{
        public static void showBottomSheetDialog(final BottomSheetDialog dialog,ArrayList<View> viewList){
            @SuppressLint("InflateParams")
            View dialogView = LayoutInflater.from(Application.context).inflate(R.layout.activity_bottom,null);

            LinearLayout frame = dialogView.findViewById(R.id.frame);

            for (View view : viewList) {
                frame.addView(view,1);
            }

            Activity activity = contextToActivity(dialog.getContext());
            if(activity == null || activity.isDestroyed()){
                return;
            }
            dialog.setContentView(dialogView);
            BottomSheetBehavior<FrameLayout> sheetBehavior = dialog.getBehavior();
            sheetBehavior.setPeekHeight(PhoneMessage.getHeightPixels());
//            dialog.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundResource(android.R.color.transparent);
            dialog.show();
        }
        public static LinearLayout CreateBottomDialogView(int icon,String text,int icon_size){
            @SuppressLint("InflateParams")
            LinearLayout view = (LinearLayout) LayoutInflater.from(Application.context).inflate(R.layout.item_bottom_dialog_text,null);
            TextView textView = view.findViewById(R.id.bottom_dialog_text);
            textView.setLines(1);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setText(text);
            ImageView bottom_dialog_icon = view.findViewById(R.id.bottom_dialog_icon);
            if(icon_size != 0){
                ViewGroup.LayoutParams layoutParams = bottom_dialog_icon.getLayoutParams();
                int size = PhoneMessage.dpToPx(icon_size);
                layoutParams.width = size;
                layoutParams.height = size;
                bottom_dialog_icon.setLayoutParams(layoutParams);
            }
            bottom_dialog_icon.setImageResource(icon);
            return view;
        }

        public static LinearLayout CreateBottomDialogView(int icon,String text){
            return CreateBottomDialogView(icon,text,0);
        }


        @SuppressLint("UseCompatLoadingForColorStateLists")
        public static TextView CreateTextView(String text){
            Context context = Application.context;
            TextView view = new TextView(context);
            view.setText(text);
            view.setTextColor(context.getColorStateList(R.color.textGeneral));
            view.setBackgroundResource(R.drawable.touch_anim_default);
            view.setTextSize(16);
            view.setPadding(PhoneMessage.dpToPx(20),PhoneMessage.dpToPx(20),PhoneMessage.dpToPx(20),PhoneMessage.dpToPx(20));
            view.setGravity(Gravity.CENTER);
            return view;
        }
    }

    public static Activity contextToActivity(Context context) {
        try {
            if (context == null)
                return null;
            else if (context instanceof Activity)
                return (Activity) context;
            else if (context instanceof ContextWrapper)
                return contextToActivity(((ContextWrapper) context).getBaseContext());
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * View渐渐消失动画效果
     */
    public static void setHideAnimation(final View view, int duration) {
        view.setVisibility(View.VISIBLE);
        view.setAlpha(1f);
        //为加载视图设置动画，使其不透明度为0％。动画结束后，将其可见性设置为GONE作为优化步骤（它将不参与布局传递，等等）.
        view.animate()
                .alpha(0f)
                .setDuration(duration) //动画持续时间 毫秒单位
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.GONE);
                    }
                });
    }

    /**
     * View渐渐显示动画效果
     */
    public static void setShowAnimation(final View view, int duration) {
        view.setVisibility(View.VISIBLE);
        view.setAlpha(0f);
        //为加载视图设置动画，使其不透明度为0％。动画结束后，将其可见性设置为GONE作为优化步骤（它将不参与布局传递，等等）.
        view.animate()
                .alpha(1f)
                .setDuration(duration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {

                    }
                }); //动画持续时间 毫秒单位
    }

    /**
     * View渐渐消失动画效果
     */
    public static void setHideAnimation(final View view, int duration,Animator.AnimatorListener listener) {
        view.setVisibility(View.VISIBLE);
        view.setAlpha(1f);
        //为加载视图设置动画，使其不透明度为0％。动画结束后，将其可见性设置为GONE作为优化步骤（它将不参与布局传递，等等）.
        view.animate()
                .alpha(0f)
                .setDuration(duration) //动画持续时间 毫秒单位
                .setListener(listener);
    }


    /**
     * View渐渐显示动画效果
     */
    public static void setShowAnimation(final View view, int duration,Animator.AnimatorListener listener) {
        view.setVisibility(View.VISIBLE);
        view.setAlpha(0f);
        //为加载视图设置动画，使其不透明度为0％。动画结束后，将其可见性设置为GONE作为优化步骤（它将不参与布局传递，等等）.
        view.animate()
                .alpha(1f)
                .setDuration(duration)
                .setListener(listener); //动画持续时间 毫秒单位
    }

    /**
     * View渐渐显示动画效果(显示到指定透明度)
     */
    public static void setShowAnimation(final View view, int duration,float alpha) {
        view.setVisibility(View.VISIBLE);
        view.setAlpha(0f);
        //为加载视图设置动画，使其不透明度为0％。动画结束后，将其可见性设置为GONE作为优化步骤（它将不参与布局传递，等等）.
        view.animate()
                .alpha(alpha)
                .setDuration(duration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {

                    }
                }); //动画持续时间 毫秒单位
    }


    /**
     * 自定义双击单击判断
     */
    public static class OnDoubleClickListener implements View.OnTouchListener{
        private boolean firstClick = false;
        private final Handler handler = new Handler(Looper.getMainLooper());
        /**
         * 两次点击时间间隔，单位毫秒
         */
        private final int totalTime = 300;
        /**
         * 自定义回调接口
         */
        private final DoubleClickCallback mCallback;
        public interface DoubleClickCallback {
            void onDoubleClick(MotionEvent event);
            void onClick();
        }
        public OnDoubleClickListener(DoubleClickCallback callback) {
            super();
            this.mCallback = callback;
        }

        private final Handler handler_double = new Handler(Looper.getMainLooper());
        /**
         * 触摸事件处理
         */
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (MotionEvent.ACTION_UP == event.getAction()) {//按下抬起
                if(firstClick){
                    handler.removeCallbacksAndMessages(null);
                    if (mCallback != null) {
                        mCallback.onDoubleClick(event);
                    }
                    handler_double.removeCallbacksAndMessages(null);
                    handler_double.postDelayed(() -> firstClick = false,300);
                    return true;
                }
                firstClick = true;
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(() -> {
                    if (mCallback != null) {
                        mCallback.onClick();
                    }
                    firstClick = false;
                    handler.removeCallbacksAndMessages(null);
                },totalTime);
            }
            return true;
        }
    }

    /**
     * 自定义长按事件监听
     */
    public static class OnLongPressListener implements View.OnTouchListener{
        /**
         * 自定义回调接口
         */
        private final OnLongPressCallback mCallback;
        public interface  OnLongPressCallback {
            void OnLongPress();
            void onClick();
        }

        public OnLongPressListener(OnLongPressCallback callback) {
            super();
            this.mCallback = callback;
        }

        private boolean isPress = false;

        private final Handler handler = new Handler(Looper.myLooper());


        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            /**
             * 按下多长时间回调长按
             */
            int press_time = 500;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: //按下
                    isPress = false;
                    handler.removeCallbacksAndMessages(null);
                    handler.postDelayed(() -> {
                        isPress = true;
                        mCallback.OnLongPress();
                    }, press_time);
                    break;
                case MotionEvent.ACTION_UP:  //松手
                    handler.removeCallbacksAndMessages(null);
                    //判断是长按还是点击
                    if(!isPress){
                        mCallback.onClick();
                    }
            }
            return false;
        }

    }


    /**
     * 自定义单击、双击、长按、移动事件监听
     */
    public static class OnCombineEventListener implements View.OnTouchListener{
        /**
         * 回调接口
         */
        private final OnCombineEventCallback mCallback;
        public interface OnCombineEventCallback {
            void onClick();
            void onDoubleClick();
            void OnLongPress(float longPressPositionX,float longPressPositionY);
            void OnMove(MotionEvent event,float startX,float lastY);
        }

        public OnCombineEventListener(OnCombineEventCallback callback) {
            this.mCallback = callback;
        }

        private static final int MAX_LONG_PRESS_TIME = 400;// 长按/双击最长等待时间
        private static final int MAX_SINGLE_CLICK_TIME = 220;// 单击后等待的时间
        private static final int MIN_DISTANCE = 8; //最小滑动距离
        private int mClickCount;// 点击次数
        private int mDownX;
        private int mDownY;
        private long mLastDownTime;
        private long mFirstClick;
        private long mSecondClick;
        private boolean isDoubleClick = false;
        private final Handler mBaseHandler = new Handler(Looper.getMainLooper());

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = event.getX();
                    lastY = 0;
                    mLastDownTime = System.currentTimeMillis();
                    mDownX = (int) event.getX();
                    mDownY = (int) event.getY();
                    mClickCount++;
                    if (mSingleClickTask != null) {
                        mBaseHandler.removeCallbacks(mSingleClickTask);
                    }
                    if(!isDoubleClick){
                        longPressPositionX = event.getX();
                        longPressPositionY = event.getY();
                        mBaseHandler.postDelayed(mLongPressTask,MAX_LONG_PRESS_TIME);
                    }
                    if (1 == mClickCount) {
                        mFirstClick = System.currentTimeMillis();
                    }else if(mClickCount == 2) {// 双击
                        mSecondClick = System.currentTimeMillis();
                        if (mSecondClick - mFirstClick <= MAX_LONG_PRESS_TIME) {
                            //处理双击
                            mDoubleClickTask();
                        }
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    float endY = event.getY();
                    if(lastY == 0){
                        lastY = endY;
                        return false;
                    }
                    lastY = endY;

                    int mMoveX = (int) event.getX();
                    int mMoveY = (int) event.getY();
                    int absMx = Math.abs(mMoveX - mDownX);
                    int absMy = Math.abs(mMoveY - mDownY);
                    ConsoleUtils.logErr(absMx);
                    if (absMx > MIN_DISTANCE || absMy > MIN_DISTANCE) {
                        mBaseHandler.removeCallbacks(mLongPressTask);
                        mBaseHandler.removeCallbacks(mSingleClickTask);
                        isDoubleClick= false;
                        mClickCount = 0;//移动了

                        mMoveTask(event);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    long mLastUpTime = System.currentTimeMillis();
                    int mUpX = (int) event.getX();
                    int mUpY = (int) event.getY();
                    int mx = Math.abs(mUpX - mDownX);
                    int my = Math.abs(mUpY - mDownY);
                    if (mx <= MIN_DISTANCE && my <= MIN_DISTANCE) {
                        if((mLastUpTime - mLastDownTime) <= MAX_LONG_PRESS_TIME) {
                            mBaseHandler.removeCallbacks(mLongPressTask);
                            if (!isDoubleClick){
                                mBaseHandler.postDelayed(mSingleClickTask,MAX_SINGLE_CLICK_TIME);
                            }
                        }else{
                            //超出了双击间隔时间
                            mClickCount = 0;
                        }
                    } else{
                        //移动了
                        mClickCount = 0;
                    }
                    if(isDoubleClick){
                        isDoubleClick = false;
                    }
                    break;
            }
            return false;
        }

        private final Runnable mSingleClickTask = new Runnable() {
            @Override
            public void run() {
                // 处理单击
                mClickCount =0;

                mCallback.onClick();
            }
        };

        private void mDoubleClickTask(){
            //处理双击
            isDoubleClick=true;
            mClickCount = 0;
            mFirstClick = 0;
            mSecondClick = 0;
            mBaseHandler.removeCallbacks(mSingleClickTask);
            mBaseHandler.removeCallbacks(mLongPressTask);

            //双击
            mCallback.onDoubleClick();
        }

        private float startX = 0; //手指按下时的X坐标
        private float lastY = 0;
        private void mMoveTask(MotionEvent event){
            //处理移动
            mCallback.OnMove(event,startX,lastY);
        }

        private float longPressPositionX; //长按位置
        private float longPressPositionY;
        private final Runnable mLongPressTask =  new Runnable() {
            @Override
            public void run() {
                //处理长按
                mClickCount = 0;
                mCallback.OnLongPress(longPressPositionX,longPressPositionY);
            }
        };

    }
}
