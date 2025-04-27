package com.zhouyu.pet_science.views.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhouyu.pet_science.R;
import com.zhouyu.pet_science.activities.base.BaseActivity;
import com.zhouyu.pet_science.utils.PhoneMessage;

import androidx.annotation.NonNull;

public class MyProgressDialog extends Dialog {
    private String titleStr; //标题文本
    private String hintStr; //提示文本

    public void setTitleStr(String titleStr) {
        this.titleStr = titleStr;
    }

    public void setHintStr(String hintStr) {
        this.hintStr = hintStr;
    }

    public void changeHint(String hintStr){
        if(hintStr!=null && hint != null){
            hint.setText(hintStr);
        }
    }

    public MyProgressDialog(@NonNull Context context){
        super(context, R.style.MyDialog);
    }


    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_progress_dialog);
        Window window = getWindow();

        WindowManager.LayoutParams layoutParams = window.getAttributes();//获取dialog布局的参数
        layoutParams.width=WindowManager.LayoutParams.MATCH_PARENT;//全屏
        layoutParams.height=WindowManager.LayoutParams.MATCH_PARENT;//全屏
        window.setAttributes(layoutParams);
        BaseActivity.setStatusBarFullTransparent(window); //设置透明
        window.getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN| View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;

        //设置状态栏为白色
        BaseActivity.setStatusBarFullTransparent(getWindow());

        LinearLayout content_view = findViewById(R.id.content_view);
        ViewGroup.LayoutParams contentViewLayoutParams = content_view.getLayoutParams();
        contentViewLayoutParams.width = (PhoneMessage.getWidthPixels()*1000)/1250;
        content_view.setLayoutParams(contentViewLayoutParams);

        //初始化界面控件
        initView();
    }

    private TextView hint = findViewById(R.id.hint);
    /**
     * 初始化界面控件
     */
    private void initView() {
        TextView title = findViewById(R.id.title);
        if(titleStr != null){
            title.setText(titleStr);
        }

        hint = findViewById(R.id.hint);
        if(hintStr != null){
            hint.setText(hintStr);
        }
    }
}
