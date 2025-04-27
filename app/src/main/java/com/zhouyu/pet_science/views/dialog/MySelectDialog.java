package com.zhouyu.pet_science.views.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
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

public class MySelectDialog extends Dialog {
    private boolean canceledOnTouchOutside = true; //点击空白处消失
    private final String[] selectItems;
    private final int default_select_index;
    private String title_text;

    public MySelectDialog(@NonNull Context context,String[] selectItems,int default_select_index){
        super(context, R.style.MyDialog);
        this.selectItems = selectItems;
        this.default_select_index = default_select_index;
    }


    public void setTitle(String title) {
        this.title_text = title;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_select_dialog);
        Window window = getWindow();

        WindowManager.LayoutParams layoutParams = window.getAttributes();//获取dialog布局的参数
        layoutParams.width=WindowManager.LayoutParams.MATCH_PARENT;//全屏
        layoutParams.height=WindowManager.LayoutParams.MATCH_PARENT;//全屏
        window.setAttributes(layoutParams);

        BaseActivity.setStatusBarFullTransparent(window); //设置透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN| View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        getWindow().getAttributes().windowAnimations = R.style.PauseDialogAnimation;

        //设置状态栏为白色
        BaseActivity.setStatusBarFullTransparent(getWindow());

        LinearLayout content_view = findViewById(R.id.content_view);
        ViewGroup.LayoutParams contentViewLayoutParams = content_view.getLayoutParams();
        contentViewLayoutParams.width = (PhoneMessage.getWidthPixels()*1000)/1300;
        content_view.setLayoutParams(contentViewLayoutParams);

        initEvent();

        initData();
    }


    @SuppressLint("UseCompatLoadingForColorStateLists")
    private void initData() {
        if(title_text != null){
            TextView title = findViewById(R.id.title);
            title.setText(title_text);
        }

        if(selectItems == null || selectItems.length == 0){
            return;
        }
        LinearLayout text_container = findViewById(R.id.text_container);
        for (int i = 0; i < selectItems.length; i++) {
            String selectItem = selectItems[i];
            @SuppressLint("InflateParams")
            LinearLayout view = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.item_my_select_dialog, null);
            TextView textView = view.findViewById(R.id.text_view);
            if(default_select_index == i){
                 textView.setTextColor(getContext().getColorStateList(R.color.ThemeTinge));
            }
            textView.setText(selectItem);

            final int finalI = i;
            view.setOnClickListener(v -> {
                if(itemOnClickListener != null){
                    itemOnClickListener.OnClick(finalI,selectItem,this);
                }
            });

            text_container.addView(view);
        }
    }

    /**
     * 定义item点击回调
     */
    private ItemOnClickListener itemOnClickListener;
    public interface ItemOnClickListener {
        void OnClick(int index,String item,MySelectDialog dialog);
    }
    public void setItemOnClickListener(ItemOnClickListener itemOnClickListener){
        this.itemOnClickListener = itemOnClickListener;
    }

    /**
     * 初始化界面的确定和取消监听
     */
    private void initEvent() {
        if(canceledOnTouchOutside){
            findViewById(R.id.content).setOnClickListener(v -> dismiss());
            findViewById(R.id.content_view).setOnClickListener(v-> {});
        }
    }

    @Override
    public void setCanceledOnTouchOutside(boolean cancel) {
        canceledOnTouchOutside = cancel;
    }

}
