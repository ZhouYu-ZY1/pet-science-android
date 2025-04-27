package com.zhouyu.pet_science.views.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zhouyu.pet_science.R;
import com.zhouyu.pet_science.activities.base.BaseActivity;
import com.zhouyu.pet_science.utils.FileUtils;
import com.zhouyu.pet_science.utils.PhoneMessage;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;

public class MyDownloadProgressDialog extends Dialog {
    private Button backDown;//后台下载
    private Button no;//取消按钮
    private ProgressBar progressBar;
    private String hint_text;
    private TextView progress;
    private TextView curr_size;
    private TextView total_size;
    private TextView hint;
    private onNoOnclickListener noOnclickListener;//取消按钮被点击了的监听器
    private onYesOnclickListener yesOnclickListener;//确定按钮被点击了的监听器
    private boolean canceledOnTouchOutside = false; //点击空白处消失

    public MyDownloadProgressDialog(@NonNull Context context){
        super(context,R.style.MyDialog);
    }

    public MyDownloadProgressDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
    }


    /**
     * 设置取消按钮的显示内容和监听
     */
    public void setNoOnclickListener(onNoOnclickListener onNoOnclickListener) {
        this.noOnclickListener = onNoOnclickListener;
    }

    /**
     * 设置确定按钮的显示内容和监听
     */
    public void setYesOnclickListener(onYesOnclickListener yesOnclickListener) {
        this.yesOnclickListener = yesOnclickListener;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_download_progess_dialog);
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
        BaseActivity.setStatusBarFullTransparent(getWindow());

        getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;


        LinearLayout content_view = findViewById(R.id.content_view);
        ViewGroup.LayoutParams contentViewLayoutParams = content_view.getLayoutParams();
        contentViewLayoutParams.width = (PhoneMessage.getWidthPixels()*1000)/1250;
        content_view.setLayoutParams(contentViewLayoutParams);

        //初始化界面控件
        initView();

        //初始化界面控件的事件
        initEvent();
    }

    /**
     * 初始化界面控件
     */
    private void initView() {
        backDown = findViewById(R.id.yes);
        no = findViewById(R.id.no);
        progressBar = findViewById(R.id.progress_bar);
        progress = findViewById(R.id.progress);

        hint = findViewById(R.id.hint);
        hint.setText(hint_text);

        curr_size = findViewById(R.id.curr_size);
        total_size = findViewById(R.id.total_size);
    }

    /**
     * 初始化界面的确定和取消监听
     */
    private void initEvent() {
        if(canceledOnTouchOutside){
            findViewById(R.id.content).setOnClickListener(v -> dismiss());
            findViewById(R.id.content_view).setOnClickListener(v-> {});
        }

        //设置确定按钮被点击后，向外界提供监听
        backDown.setOnClickListener(v -> {
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
    }

    @SuppressLint("SetTextI18n")
    public void updateProgress(int progressText){
        if(progressBar == null || progress == null){
            return;
        }
        if(progressText > 100){
            progressText = 100;
        }else if(progressText < 0){
            progressText = 0;
        }
        progressBar.setProgress(progressText);
        progress.setText(progressText+"%");
    }
    @SuppressLint("DefaultLocale")
    public void updateCurrentSize(long size){
        String format = FileUtils.longToMBStr(size);
        curr_size.setText(format);
    }

    @SuppressLint("DefaultLocale")
    public void updateTotalSize(long size){
        String format = FileUtils.longToMBStr(size);
        total_size.setText(format);
    }


    public interface onNoOnclickListener {
        void onNoClick();
    }

    public interface onYesOnclickListener {
        void onYesOnclick();
    }

    public String getHint_text() {
        return hint_text;
    }

    public void setHint_text(String hint_text) {
        this.hint_text = hint_text;
    }

    public void updateHintText(String hint_text){
        this.hint_text = hint_text;
        if(hint != null){
            hint.setText(hint_text);
        }
    }

    @Override
    public void setCanceledOnTouchOutside(boolean cancel) {
        canceledOnTouchOutside = cancel;
    }
}


