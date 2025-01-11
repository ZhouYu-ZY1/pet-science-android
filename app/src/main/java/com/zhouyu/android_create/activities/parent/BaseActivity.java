package com.zhouyu.android_create.activities.parent;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.zhouyu.android_create.manager.ActivityManager;


import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.hide();
        }

//        View main = findViewById(R.id.main);
//        if(main != null){
//            ViewCompat.setOnApplyWindowInsetsListener(main, (v, insets) -> {
//                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//                return insets;
//            });
//        }

        //状态栏透明
        setStatusBarFullTransparent(getWindow());

        //添加Activity到管理器
        ActivityManager.getInstance().addActivity(this);
    }

    public Runnable executeThread(Runnable runnable){
        try {
            new Thread(runnable).start();
        }catch (Exception e){
            e.printStackTrace();
        }
        return runnable;
    }

    @Override
    public void finish() {
        ActivityManager.getInstance().removeActivity(this);
        super.finish();
    }

    public void showToast(String text){
        showToast(text, Toast.LENGTH_SHORT);
    }
    public void showToast(String text,int duration){
        Toast.makeText(this, text, duration).show();
    }


    public void setStatusBarTextColor(boolean dark) {
        View decor = getWindow().getDecorView();
        if (dark) {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    /**
     * 状态栏透明
     */
    public static void setStatusBarFullTransparent(Window window) {
        if (Build.VERSION.SDK_INT >= 21) {//21表示5.0
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else { //19表示4.4
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //虚拟键盘也透明
            //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }


}
