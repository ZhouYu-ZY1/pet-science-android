package com.zhouyu.pet_science.views.dialog;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhouyu.pet_science.R;
import com.zhouyu.pet_science.activities.base.BaseActivity;
import com.zhouyu.pet_science.utils.InputUtils;
import com.zhouyu.pet_science.utils.AndroidTextUtils;
import com.zhouyu.pet_science.utils.Tool;
import com.zhouyu.pet_science.utils.PhoneMessage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

/**
 * 自定义的Dialog
 */

public class MyDialog extends Dialog {
    private Button yes;//确定按钮
    private Button no;//取消按钮
    private Button no_hint;//不再提示按钮
    private TextView titleTV;//消息标题文本
    private TextView message;//消息提示文本
    private int messageGravity = Gravity.CENTER;
    private String titleStr;//从外界设置的title文本
    private int titleSize = -1;
    private CharSequence messageStr;//从外界设置的消息文本
    private EditText dialog_input; //输入框
    private int dialog_input_type = InputType.TYPE_CLASS_TEXT;
    private String dialog_input_hint = ""; //输入框提示信息
    private String dialog_input_default_value = "";
    //确定文本和取消文本的显示的内容
    private String yesStr,noStr,noHintStr;
    private onNoOnclickListener noOnclickListener;//取消按钮被点击了的监听器
    private onYesOnclickListener yesOnclickListener;//确定按钮被点击了的监听器
    private onNoHintOnclickListener noHintOnclickListener;//确定按钮被点击了的监听器
    private boolean isHideNo = false; //是否隐藏取消按钮
    private boolean isHideYes = false; //是否隐藏确定按钮
    private boolean isShowNoHint = false; //是否显示不再提示按钮
    private boolean result = false; //返回值
    private String result_str = ""; //返回值
    private boolean canceledOnTouchOutside = true; //点击空白处消失
    private boolean isScroll;
//    private boolean isShowRadioButton;

    public MyDialog(@NonNull Context context){
        super(context,R.style.MyDialog);
    }

    public MyDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
    }
    public MyDialog(@NonNull Context context,boolean isScroll){
        super(context,R.style.MyDialog);
        this.isScroll = isScroll;
    }

    public int getMessageGravity() {
        return messageGravity;
    }

    public void setMessageGravity(int messageGravity) {
        this.messageGravity = messageGravity;
    }

    public void setDialog_input_default_value(String value){
        dialog_input_default_value = value;
    }

    /**
     * 设置取消按钮的显示内容和监听
     */
    public void setNoOnclickListener(String str, onNoOnclickListener onNoOnclickListener) {
        if (str != null) {
            noStr = str;
        }

        this.noOnclickListener = onNoOnclickListener;
    }

    /**
     * 设置确定按钮的显示内容和监听
     */
    public void setYesOnclickListener(String str, onYesOnclickListener yesOnclickListener) {
        if (str != null) {
            yesStr = str;
        }
        this.yesOnclickListener = yesOnclickListener;
    }

    /**
     * 设置确定按钮的显示内容和监听
     */
    public void setNoHintOnclickListener(String str, onNoHintOnclickListener noHintOnclickListener) {
        if (str != null) {
            noHintStr = str;
        }
        this.noHintOnclickListener = noHintOnclickListener;
        setShowNoHint(true);
    }



    //是否隐藏取消按钮
    public void hideNoButton(boolean isHide){
        isHideNo = isHide;
    }

    //是否隐藏确定按钮
    public void hideYesButton(boolean isHide){
        isHideYes = isHide;
    }
    //是否显示不再提示按钮
    public void setShowNoHint(boolean showNoHint) {
        isShowNoHint = showNoHint;
    }

    //是否为输入框弹框
    private boolean isInput = false;
    public void isInputDialog(boolean isInput){
        this.isInput = isInput;
    }



    private LinearLayout my_dialog_content_view;
    private LinearLayout content_view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(isScroll){
            setContentView(R.layout.my_scroll_dialog);

            View scroll = findViewById(R.id.scroll);
            ViewGroup.LayoutParams layoutParams = scroll.getLayoutParams();
            layoutParams.height = (int) (PhoneMessage.getHeightPixels() * 0.4);
            scroll.setLayoutParams(layoutParams);
        }else {
            setContentView(R.layout.my_dialog);
        }
        Window window = getWindow();

        WindowManager.LayoutParams layoutParams = window.getAttributes();//获取dialog布局的参数
        layoutParams.width=WindowManager.LayoutParams.MATCH_PARENT;//全屏
        layoutParams.height=WindowManager.LayoutParams.MATCH_PARENT;//全屏
        window.setAttributes(layoutParams);

        BaseActivity.setStatusBarFullTransparent(window); //设置透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN| View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        //设置状态栏为白色
        BaseActivity.setStatusBarFullTransparent(window);

        window.getAttributes().windowAnimations = R.style.PauseDialogAnimation;


        my_dialog_content_view = findViewById(R.id.my_dialog_content_view);
        ViewGroup.LayoutParams contentViewLayoutParams = my_dialog_content_view.getLayoutParams();
        contentViewLayoutParams.width = (PhoneMessage.getWidthPixels()*1000)/1250;
        my_dialog_content_view.setLayoutParams(contentViewLayoutParams);
        content_view = findViewById(R.id.content_view);

        //初始化界面控件
        initView();

        //初始化界面数据
        initData();
        //初始化界面控件的事件
        initEvent();
    }

    /**
     * 初始化界面控件
     */
    private void initView() {
        yes = findViewById(R.id.yes);
        no = findViewById(R.id.no);
        no_hint = findViewById(R.id.no_hint);
        titleTV = findViewById(R.id.title);
        message = findViewById(R.id.message);
        dialog_input = findViewById(R.id.dialog_input);
        View center_view = findViewById(R.id.center_view);
        if(isHideNo){
            center_view.setVisibility(View.GONE);
            no.setVisibility(View.GONE);
        }
        if(isHideYes){
            center_view.setVisibility(View.GONE);
            yes.setVisibility(View.GONE);
        }
        if(isShowNoHint){
            findViewById(R.id.no_hint_view).setVisibility(View.VISIBLE);
            findViewById(R.id.center_view2).setVisibility(View.VISIBLE);
        }
        if(isInput){
            findViewById(R.id.dialog_input_view).setVisibility(View.VISIBLE);
            dialog_input.setHint(dialog_input_hint);
            dialog_input.setText(dialog_input_default_value);
            dialog_input.setInputType(dialog_input_type);
            if(messageStr == null || AndroidTextUtils.isEmpty(messageStr.toString())){
                message.setVisibility(View.GONE);
            }
        }

    }

    /**
     * 初始化界面控件的显示数据
     */
    private void initData() {
        //如果用户自定了title和message
        if (titleStr != null) {
            titleTV.setText(titleStr);
        }
        if(titleSize != -1){
            titleTV.setTextSize(titleSize);
        }
        if (messageStr != null) {
            message.setText(messageStr);
            if(isScroll){
                message.setMovementMethod(LinkMovementMethod.getInstance()); //响应ClickableSpan点击事件必须设置
            }
        }
        message.setGravity(messageGravity);
        //如果设置按钮文字
        if (yesStr != null) {
            yes.setText(yesStr);
        }
        if (noStr != null) {
            no.setText(noStr);
        }
        if(noHintStr != null && no_hint != null){
            no_hint.setText(noHintStr);
        }
        if(themeColor != -1){
            titleTV.setTextColor(themeColor);
            yes.setTextColor(themeColor);
        }
    }

    /**
     * 初始化界面的确定和取消监听
     */
    private void initEvent() {
        if(canceledOnTouchOutside){
            findViewById(R.id.content).setOnClickListener(v -> dismiss());
            my_dialog_content_view.setOnClickListener(v-> {});
        }

        //设置确定按钮被点击后，向外界提供监听
        yes.setOnClickListener(v -> {
            if (yesOnclickListener != null) {
                yesOnclickListener.onYesOnclick();
            }
        });
        //设置取消按钮被点击后，向外界提供监听
        no.setOnClickListener(v -> {
            if (noOnclickListener != null) {
                noOnclickListener.onNoClick();
            }
        });

        if(no_hint != null){
            no_hint.setOnClickListener(v -> {
                if (noHintOnclickListener != null) {
                    noHintOnclickListener.onNoHintOnclick();
                }
            });
        }
    }

    public int getDialog_input_type() {
        return dialog_input_type;
    }

    public void setDialog_input_type(int dialog_input_type) {
        this.dialog_input_type = dialog_input_type;
    }

    /**
     * 从外界Activity为Dialog设置标题
     */
    public void setTitle(String title) {
        titleStr = title;
    }

    /**
     * 设置标题字体大小，单位dp
     */
    public void setTitleSize(int size){
        titleSize = size;
    }

    /**
     * 从外界Activity为Dialog设置message
     */
    public void setMessage(CharSequence message) {
        messageStr = message;
    }

    /**
     * 设置输入框提示信息
     */
    public void setDialogInputHint(String hint){
        dialog_input_hint = hint;
    }

    public String getDialogInputText(){
        if(isInput){
            return dialog_input.getText().toString();
        }else {
            return "";
        }
    }

    public interface onNoOnclickListener {
        void onNoClick();
    }

    public interface onYesOnclickListener {
        void onYesOnclick();
    }
    public interface onNoHintOnclickListener {
        void onNoHintOnclick();
    }

    public boolean getResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getResult_str() {
        return result_str;
    }

    public void setResult_str(String result_str) {
        this.result_str = result_str;
    }

    private int themeColor = -1;
    public void setThemeColor(int color){
        themeColor = color;
    }

    private InputUtils.KeyboardListener keyboardListener;
    @Override
    public void show() {
        Activity activity = Tool.contextToActivity(getContext());
        if(activity == null || activity.isDestroyed()){
            return;
        }
        super.show();
        if(isInput){
            EditText input = findViewById(R.id.dialog_input);
            keyboardListener = new InputUtils.KeyboardListener(activity, new InputUtils.OnKeyboardListener() {

                private int moveHeight;
                @Override
                public void onKeyboardShown(int currentKeyboardHeight) {
                    try {
                        int i = (PhoneMessage.getHeightPixels() + PhoneMessage.statusBarHeight) / 2 - my_dialog_content_view.getHeight() / 2 - currentKeyboardHeight;
                        moveHeight = i;
                        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(my_dialog_content_view,"translationY",0,i);
                        objectAnimator.setInterpolator(new DecelerateInterpolator()); //设置动画为0匀速执行
                        objectAnimator.setDuration(300);  //动画执行一次的时间
                        objectAnimator.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onKeyboardHidden() {
                    try {
                        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(my_dialog_content_view,"translationY",moveHeight,0);
                        objectAnimator.setInterpolator(new DecelerateInterpolator()); //设置动画为0匀速执行
                        objectAnimator.setDuration(300);  //动画执行一次的时间
                        objectAnimator.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            new Handler().postDelayed(() -> {
                //弹起输入框
                InputUtils.inputShow(activity, input);
            },200);

            if(!AndroidTextUtils.isEmpty(dialog_input_default_value)){
                input.selectAll();
            }
        }
    }

    @Override
    public void setOnDismissListener(@Nullable OnDismissListener listener) {
        if(listener == null || !isInput){
            super.setOnDismissListener(listener);
            return;
        }
        super.setOnDismissListener(dialog -> {
            if(keyboardListener != null){
                keyboardListener.removeKeyboardListener();
            }
            listener.onDismiss(dialog);
        });
    }

    @Override
    public void setCanceledOnTouchOutside(boolean cancel) {
        canceledOnTouchOutside = cancel;
    }
}
