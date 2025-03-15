package com.zhouyu.pet_science.tools;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import static android.content.Context.INPUT_METHOD_SERVICE;

import com.zhouyu.pet_science.tools.utils.PhoneMessage;

public class InputTool {

    /**
     * 让输入框始终在组件下方
     * @param parentView 根布局
     *  @param childView 需要显示的最下方View
     */
    public static void addLayoutListener(final View parentView, final View childView) {
        parentView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect rect = new Rect();
            parentView.getWindowVisibleDisplayFrame(rect);
            int mainInvisibleHeight = parentView.getRootView().getHeight() - rect.bottom;
            if (mainInvisibleHeight > 100) {
                int[] location = new int[2];
                childView.getLocationInWindow(location);
                int srollHeight = (location[1] + childView.getHeight()) - rect.bottom;
                parentView.scrollTo(0, srollHeight);
            } else {
                parentView.scrollTo(0, 0);
            }
        });
    }

    /**
     *  隐藏软键盘
     */
    public static void inputHide(Context context, EditText input){
        InputMethodManager imm = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
        boolean isOpen = imm.isActive();
        if (isOpen) {
            imm.hideSoftInputFromWindow(input.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 弹出软键盘
     */
    public static void inputShow(Context context,EditText input){
        input.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
    }




    public static class KeyboardListener {

        // 最小键盘高度的阈值
        final int MIN_KEYBOARD_HEIGHT_PX = PhoneMessage.dpToPx(5);
        // 屏幕可见区域
        private final Rect windowVisibleDisplayFrame = new Rect();
        // 保存最后可视区域高度
        private int lastKeyboardHeight;
        private final View decorView;
        private final ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener;

        public KeyboardListener(Activity activity,OnKeyboardListener onKeyboardListener) {
            decorView = activity.getWindow().getDecorView();
            onGlobalLayoutListener = () -> {
                // 获取屏幕可视区域
                decorView.getWindowVisibleDisplayFrame(windowVisibleDisplayFrame);
                //屏幕高度
                int height = decorView.getHeight();
                // 获取键盘高度
                int currentKeyboardHeight = height - windowVisibleDisplayFrame.bottom;
                if (lastKeyboardHeight != currentKeyboardHeight) {

                    if (isKeyboardShown(decorView)) {// 键盘可见时回调
                        if (onKeyboardListener != null) {
                            onKeyboardListener.onKeyboardShown(currentKeyboardHeight);
                        }
                    } else {// 键盘隐藏时回调
                        if (onKeyboardListener != null) {
                            onKeyboardListener.onKeyboardHidden();
                        }
                    }
                    lastKeyboardHeight = currentKeyboardHeight;
                }
            };

            decorView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
        }

        public void removeKeyboardListener(){
            //防止内存泄漏
            decorView.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
        }
    }

    private static boolean isKeyboardShown(View rootView) {
        final int softKeyboardHeight = 100;
        Rect r = new Rect();
        rootView.getWindowVisibleDisplayFrame(r);
        DisplayMetrics dm = rootView.getResources().getDisplayMetrics();
        int heightDiff = rootView.getBottom() - r.bottom;
        return heightDiff > softKeyboardHeight * dm.density;
    }

    public interface OnKeyboardListener {
        void onKeyboardShown(int currentKeyboardHeight);
        void onKeyboardHidden();
    }





//    // Threshold for minimal keyboard height.
//    final int MIN_KEYBOARD_HEIGHT_PX = 150;
//
//    // Top-level window decor view.
//    final View decorView = activity.getWindow().getDecorView();

//// Register global layout listener.
//decorView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//        private final Rect windowVisibleDisplayFrame = new Rect();
//        private int lastVisibleDecorViewHeight;
//
//        @Override
//        public void onGlobalLayout() {
//            // Retrieve visible rectangle inside window.
//            decorView.getWindowVisibleDisplayFrame(windowVisibleDisplayFrame);
//            final int visibleDecorViewHeight = windowVisibleDisplayFrame.height();
//
//            // Decide whether keyboard is visible from changing decor view height.
//            if (lastVisibleDecorViewHeight != 0) {
//                if (lastVisibleDecorViewHeight > visibleDecorViewHeight + MIN_KEYBOARD_HEIGHT_PX) {
//                    // Calculate current keyboard height (this includes also navigation bar height when in fullscreen mode).
//                    int currentKeyboardHeight = decorView.getHeight() - windowVisibleDisplayFrame.bottom;
//                    // Notify listener about keyboard being shown.
//                    listener.onKeyboardShown(currentKeyboardHeight);
//                } else if (lastVisibleDecorViewHeight + MIN_KEYBOARD_HEIGHT_PX < visibleDecorViewHeight) {
//                    // Notify listener about keyboard being hidden.
//                    listener.onKeyboardHidden();
//                }
//            }
//            // Save current decor view height for the next call.
//            lastVisibleDecorViewHeight = visibleDecorViewHeight;
//        }
//    });
}
