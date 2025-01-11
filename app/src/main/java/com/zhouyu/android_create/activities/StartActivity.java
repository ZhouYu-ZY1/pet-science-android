package com.zhouyu.android_create.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zhouyu.android_create.R;
import com.zhouyu.android_create.activities.parent.BaseActivity;
import com.zhouyu.android_create.manager.ActivityManager;
import com.zhouyu.android_create.application.Application;
import com.zhouyu.android_create.tools.CloudDisk.TXDoc;
import com.zhouyu.android_create.tools.CustomSys.MyToast;
import com.zhouyu.android_create.tools.StorageTool;
import com.zhouyu.android_create.tools.TextTool;
import com.zhouyu.android_create.tools.Tool;
import com.zhouyu.android_create.tools.utils.NightModeUtils;
import com.zhouyu.android_create.tools.utils.PhoneMessage;
import com.zhouyu.android_create.views.dialog.MyDialog;

import org.json.JSONObject;

import java.io.File;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class StartActivity extends AppCompatActivity {
    private boolean isDestroy;
    private String openActivityName;
    public static boolean isChangeNightMode;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(ActivityManager.isFinishApplication){
            super.onCreate(null);
            finish();
            return;
        }

        EdgeToEdge.enable(this);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.hide();
        }

        Application.application.isStartActivity = true;


        //夜间模式
        if(StorageTool.contains("NightMode")){
            Application.NightMode = StorageTool.get("NightMode");
        }
        NightModeUtils.updateNightMode(Application.NightMode);

        //获取要启动的页面参数
        Bundle openBundle = getIntent().getBundleExtra("openActivityName");
        if(openBundle != null){
            CharSequence openActivityName = openBundle.getCharSequence("openActivityName");
            if(openActivityName != null){
                this.openActivityName = openActivityName.toString();
            }
        }

        if(ActivityManager.activities.size() > 0){
            ActivityManager.getInstance().finishAllActivity();
        }

        isLoadComplete = false;
        super.onCreate(savedInstanceState);

        //进入全屏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            //适配刘海屏水滴屏，扩展到状态栏
            Tool.fullScreen(this);
        }else {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }


        if((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0){
            finish();
            return;
        }

        Application.application.appAlive = 1; //启动app后改变状态
        BaseActivity.setStatusBarFullTransparent(getWindow());

        setContentView(R.layout.activity_start);

        //加载启动图片
        Application.executeThread(() -> {
            try {
                File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath() + "/startBackground.jpg");
                ImageView back_image = findViewById(R.id.back_image);
                runOnUiThread(() -> {
                    try {
                        if(file.exists()){
                            Glide.with(Application.context).load(file)
                                    .skipMemoryCache(true)// 不使用内存缓存
                                    .diskCacheStrategy(DiskCacheStrategy.NONE) // 不使用磁盘缓存
                                    .transition(withCrossFade()).into(back_image);
                        }else {
                            Glide.with(Application.context).load(R.mipmap.start_background).transition(withCrossFade()).into(back_image);
                        }

                        skipHandler.removeCallbacksAndMessages(null);
                        skipHandler.postDelayed(this::skip,1000);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        });
        init();
    }

    public static boolean isLoadComplete;
    private boolean isLoadInit;
    private boolean notSkip;
    public static boolean noInterceptVPN = false;
    private void init(){
        if(isLoadInit){
            return;
        }

        isLoadInit = true;
        Application.executeThread(new Runnable() {
            @SuppressLint("WrongConstant")
            @Override
            public void run() {

                try {
                    if(!isChangeNightMode){


                        if(Application.application.main != 1){
                            long l = System.currentTimeMillis();
                            long period = 1000 * 60 * 60 * 24 * 3; //间隔3天获取更新
                            long lastVerifyVersionTime = 0;
                            if(StorageTool.contains("lastVerifyVersionTime")){
                                lastVerifyVersionTime = StorageTool.get("lastVerifyVersionTime");
                            }
                            if(l - lastVerifyVersionTime > period){
                                StorageTool.put("lastVerifyVersionTime", l);
                                verifyVersion();
                            }else {
                                isUpdate = false;
                            }
                        }else {
                            isUpdate = false;
                        }

                        if(StorageTool.contains("noFirstOpenAPP")){
                            Application.noFirstOpenAPP = StorageTool.get("noFirstOpenAPP");
                        }

                        if(StorageTool.contains("alreadyShowAdNotice")){
                            MainActivity.alreadyShowAdNotice = StorageTool.get("alreadyShowAdNotice");
                        }
                        if(StorageTool.contains("noHintAdDialog")){
                            MainActivity.noHintAdDialog = StorageTool.get("noHintAdDialog");
                        }

//                        if(StorageTool.contains("NoShieldVPN")){
//                            StorageTool.delete("NoShieldVPN");
//                        }



                        //同意声明
                        if(StorageTool.contains("isAgreeStatement")){
                            MainActivity.isAgreeStatement = StorageTool.get("isAgreeStatement");
                        }


//                        //下载目录uri
//                        SharedPreferences sp = getSharedPreferences("DirPermission", Context.MODE_PRIVATE);
//                        String uriTree = sp.getString("uriTree", "");
//                        if (!TextTool.isEmpty(uriTree)) {
//                            try {
//                                Uri uri = Uri.parse(uriTree);
//                                final int takeFlags = getIntent().getFlags()
//                                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
//                                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//                                getContentResolver().takePersistableUriPermission(uri, takeFlags);
//                                Application.saveFileDocumentFile = DocumentFile.fromTreeUri(getApplicationContext(), uri);
//                                SettingActivity.saveFileDocumentFileUri = uri;
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                                // 重新授权
//                            }
//                        }

//                        //登录信息
//                        if(StorageTool.contains("login_information")){
//                            Application.loginInformation = StorageTool.get("login_information");
//                        }
                    }else {
                        isChangeNightMode = false;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    final Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(isLoadComplete){
                                finish();
                            }else {
                                handler.postDelayed(this,100);
                            }
                        }
                    });
                }
            }
        });
    }

    private static boolean isUpdate = true;
    public static boolean isSkipUpdate = false;
    public static boolean isNoNetwork;
    private boolean isMaintenance = false;
    private boolean isVerifyVersionError = false;
    public static boolean closeFeePort = false;

    private void verifyVersion() {
        isUpdate = false;
        MainActivity.isTestVersion = false;
        try {
            try {
                String doc = TXDoc.getDoc("DTXJIdnhDU0lFaFhl");  //数据文档
                if(!TextTool.isEmpty(doc)){
                    JSONObject jsonObject = new JSONObject(doc);

                    String notice = jsonObject.getString("notice");
                    if(!TextTool.isEmpty(notice)){
//                        UserFragment.bNotice = notice;
                    }

                    //云音乐cookie（通知加载）
//                    String cloudCookie = jsonObject.getString("cloudCookie");
//                    if(!TextTool.isEmpty(cloudCookie)){
//                        WYYMusicHttpTool.vipCookie = cloudCookie;
//                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }

            try {
//                String doc = TXDoc.getDoc("DTXpic1lFR0lOZHRE"); //更新文档
                String doc = getVersionMessage();
//                String doc = "";
                if (!TextTool.isEmpty(doc)) {
                    JSONObject jsonObject = new JSONObject(doc);
                    if(!jsonObject.isNull("error")){
                       String error = jsonObject.getString("error");
                       if(!TextTool.isEmpty(error)){
                           isMaintenance = true;
                           runOnUiThread(() -> {
                               MyDialog myDialog = new MyDialog(this);
                               myDialog.setTitle("Error");
                               myDialog.setMessage(error);
                               myDialog.setCanceledOnTouchOutside(false);
                               myDialog.setYesOnclickListener("我知道了", myDialog::dismiss);
                               myDialog.hideNoButton(true);
                               myDialog.setOnDismissListener(dialog -> ActivityManager.getInstance().finishApplication());
                               myDialog.show();
                           });
                           return;
                       }
                    }


                    String versionName = jsonObject.getString("versionName");
                    long versionCode = jsonObject.getLong("versionCode");
                    String appVersionName = PhoneMessage.getAppVersionName();
                    long appVersionCode = PhoneMessage.getAppVersionCode();

                    String versionIntroduce = jsonObject.getString("versionIntroduce");
                    boolean isCoerceUpdate = false;
                    boolean isMaintenance = false;
                    boolean isLessMinVersion = false;
//                    if(!jsonObject.isNull("isCoerceUpdate")){
//                        isCoerceUpdate = jsonObject.getBoolean("isCoerceUpdate");
//                    }

                    if(!jsonObject.isNull("isCoerceUpdate")){
                        isCoerceUpdate = jsonObject.getBoolean("isCoerceUpdate");
                    }


                    if(!jsonObject.isNull("minVersion")){
                        int minVersion = jsonObject.getInt("minVersion");
                        if(minVersion > appVersionCode){
                            isLessMinVersion = true;
                        }
                    }

                    if(!jsonObject.isNull("isMaintenance")){
                        isMaintenance = jsonObject.getBoolean("isMaintenance");
                    }

                    StorageTool.put("isMaintenance", isMaintenance);
                    if(isMaintenance){
                        showMaintenanceDialog();
                        return;
                    }

                    final String url = jsonObject.getString("downloadUrl");
//                    AboutFragment.downLoadUrl = url;

                    boolean alert = false;
                    if(!jsonObject.isNull("alert")){
                        alert  = jsonObject.getBoolean("alert");
                    }
                    if(versionCode > appVersionCode && alert){
                        isUpdate = true;
                        boolean finalIsCoerceUpdate = isCoerceUpdate;
                        boolean finalIsLessMinVersion = isLessMinVersion;
                        runOnUiThread(() -> {
                            final MyDialog myDialog = new MyDialog(this);
                            myDialog.setTitle("检测到新版本");
                            myDialog.setCanceledOnTouchOutside(false);

                            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                            spannableStringBuilder.append("当前版本：").append(appVersionName)
                                    .append("\n最新版本：").append(versionName)
                                    .append("\n\n更新内容:\n");
                            spannableStringBuilder.append(Html.fromHtml(
                                    versionIntroduce));
                            myDialog.setMessage(spannableStringBuilder);

                            myDialog.setMessageGravity(Gravity.START);
                            myDialog.setYesOnclickListener("立即更新", () -> {
                                try {
                                    Uri uri = Uri.parse(url);
                                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                    startActivity(intent);
                                }catch (Exception e){
                                    MyToast.show("出错了，请前往微信公众号“微音乐助手”获取最新版下载链接",Toast.LENGTH_LONG,false);
                                }
                            });
                            myDialog.setNoOnclickListener("下次更新", myDialog::dismiss);

                            myDialog.setOnDismissListener(dialog -> {
                                if(finalIsCoerceUpdate || finalIsLessMinVersion){
                                    ActivityManager.getInstance().finishApplication(); //如果为强制更新或小于最低支持版本则退出APP
                                }else {
                                    isUpdate = false;
                                    isSkipUpdate = true;
                                    MainActivity.isTestVersion = true;
                                    notSkip = false;
                                }
                            });
                            //                        StorageTool.put("isCoerceUpdate",finalIsCoerceUpdate || finalIsLessMinVersion);
                            myDialog.show();
                        });
                    }else {
                        isUpdate = false;
                        isSkipUpdate = true;
                        MainActivity.isTestVersion = true;
                        notSkip = false;
                    }
                }else {
                    MainActivity.isTestVersion = true;
                    isUpdate = false;
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    //获取版本信息
    public static String getVersionMessage() {
//        String doc = TXDoc.getDoc("DTXpic1lFR0lOZHRE");
        String doc = TXDoc.getDoc("DSkF3eU5Ua3NkdFJG");
//        doc = "";
        if(TextTool.isEmpty(doc)){
            doc = TXDoc.getYouDaoDoc("f48f8388d99ee82efc235b8f9a61f145");
        }
        return doc;
    }

    private void showMaintenanceDialog(){
        isMaintenance = true;
        runOnUiThread(() -> {
            MyDialog myDialog = new MyDialog(this);
            myDialog.setTitle("软件维护中");
            myDialog.setMessage("软件正在维护，请稍后再试！可至微信公众号【微音乐助手】获取最新消息。");
            myDialog.setCanceledOnTouchOutside(false);
            myDialog.setYesOnclickListener("我知道了", myDialog::dismiss);
            myDialog.hideNoButton(true);
            myDialog.setOnDismissListener(dialog -> ActivityManager.getInstance().finishApplication());
            myDialog.show();
        });
    }

    private final Handler skipHandler = new Handler(Looper.getMainLooper());
    private void skip(){
        if(isDestroy){
            return;
        }

        //页面跳转
        if(!isUpdate){
            skipActivity();
        }

        skipHandler.postDelayed(this::skip,100);
    }
    private void skipActivity(){
        if(isMaintenance){
            return;
        }
        Intent intent = new Intent(StartActivity.this, MainActivity.class);
        intent.putExtra("openActivityName",openActivityName);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        skipHandler.removeCallbacksAndMessages(null);
        skipHandler.postDelayed(this::skip,1000);
    }


    @Override
    public void finish() {
        isDestroy = true;
        if(skipHandler != null){
            skipHandler.removeCallbacksAndMessages(null);
        }
        super.finish();
    }
}