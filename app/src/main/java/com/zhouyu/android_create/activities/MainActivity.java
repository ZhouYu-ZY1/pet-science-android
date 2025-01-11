package com.zhouyu.android_create.activities;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.zhouyu.android_create.R;
import com.zhouyu.android_create.activities.parent.BaseActivity;
import com.zhouyu.android_create.fragments.AboutFragment;
import com.zhouyu.android_create.manager.ActivityManager;
import com.zhouyu.android_create.application.Application;
import com.zhouyu.android_create.tools.CleanCacheTool;
import com.zhouyu.android_create.tools.CustomSys.MyToast;
import com.zhouyu.android_create.tools.StorageTool;
import com.zhouyu.android_create.tools.Tool;
import com.zhouyu.android_create.tools.utils.PhoneMessage;
import com.zhouyu.android_create.tools.utils.TimeGreetings;
import com.zhouyu.android_create.views.CustomViewPager;
import com.zhouyu.android_create.views.dialog.MyDialog;
import com.zhouyu.android_create.views.dialog.MyProgressDialog;
import com.zhouyu.android_create.views.dialog.MySelectDialog;

import org.jetbrains.annotations.NotNull;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import me.ibrahimsn.lib.SmoothBottomBar;


public class MainActivity extends BaseActivity {
    private List<Fragment> fragmentList;
    private CustomViewPager view_pager;
    private SmoothBottomBar smoothBottomBar;
    public DrawerLayout drawer_layout;
    public static boolean isTestVersion = true;
    public static boolean allow_visit = false;
    public static boolean allow_retry = false;
    public static boolean useVerify = true;

    @Override
    protected void onPause() {
        handler.removeCallbacksAndMessages(null);
        super.onPause();
    }

    @Override
    protected void onResume() {
        runTime();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        saveUserMessageHandler.removeCallbacksAndMessages(null);
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    public static boolean isLogin = false;
    public final static String OPEN_PLAY_ACTIVITY = "play";
    public final static String OPEN_DESKTOP_LRC = "open_desktop_lrc";
    public final static String OPEN_STATUS_BAR_LRC = "open_status_bar_lrc";
    public final static String OPEN = "open";
    public final static String OPEN_DOWNLOAD_ACTIVITY = "download";
    public static boolean isUpdateNightMode = false;

    @SuppressLint("RtlHardcoded")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Application.application.appAlive = 1; //启动app后改变状态
        Application.application.main = 1;
        setContentView(R.layout.activity_main);
        StartActivity.isLoadComplete = true;

        // 初始化布局元素
        initViews();

        runTime();

        if(!isLogin){
            if(isUpdateNightMode){
                isUpdateNightMode = false;
                return;
            }
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                //提示语
                new TimeGreetings().showGreetings();
            },500);
        }else {
            isLogin = false;
        }

        if(!isAgreeStatement){
            showStatementDialog(this);
        }

        startSaveMessage();
    }

    public static boolean isAgreeStatement = false;
    public static void showStatementDialog(Context context) {
        MyDialog myDialog = new MyDialog(context,true);
        String app_name = context.getString(R.string.app_name);
        myDialog.setTitle(app_name);
        myDialog.setMessageGravity(Gravity.START);
        myDialog.setCanceledOnTouchOutside(false);

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

        spannableStringBuilder.append("本应用尊重并保护所有用户的个人隐私权。根据相关政策规定，在使用“").append(app_name).append("”之前，您需要认真阅读");

        //隐私政策点击
        ClickableSpan use_rules = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
//                Intent intent = new Intent(context, WebBrowserActivity.class);
//                intent.putExtra("url","https://docs.qq.com/doc/p/991885d9fcf3006fa10dd1f640e2f9d5bd296fcf");
//                context.startActivity(intent);
            }
            @Override
            public void updateDrawState(TextPaint ds) {
            }
        };
        SpannableString use_rules_span = new SpannableString(Html.fromHtml("<font color=\"#66C1FB\">《"+app_name+"软件使用条例》</font>"));
        use_rules_span.setSpan(use_rules,0,use_rules_span.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);   //设置点击事件
        use_rules_span.setSpan(new UnderlineSpan(),0,use_rules_span.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE); //设置下划线
        spannableStringBuilder.append(use_rules_span);


        spannableStringBuilder.append("和");
        //隐私政策点击
        ClickableSpan privacy_clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
//                Intent intent = new Intent(context, WebBrowserActivity.class);
//                intent.putExtra("url","https://docs.qq.com/doc/p/9b764cc0d3017c94a154f08d2bffc32854680dd1");
//                context.startActivity(intent);
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                // 可选：改变点击文本的外观，如颜色
//                ds.setColor(Color.BLUE);
//                ds.setUnderlineText(true); // 某些情况下需要手动添加下划线，但通常不是必须的
            }
        };
        SpannableString privacy_policy_span = new SpannableString(Html.fromHtml("<font color=\"#66C1FB\">《"+app_name+"隐私政策》</font>"));
        privacy_policy_span.setSpan(privacy_clickableSpan,0,privacy_policy_span.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);   //设置点击事件
        privacy_policy_span.setSpan(new UnderlineSpan(),0,privacy_policy_span.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE); //设置下划线
        spannableStringBuilder.append(privacy_policy_span);

        spannableStringBuilder.append("，您同意并接受全部条款后方可开始使用“").append(app_name).append("”。\n\n");
        spannableStringBuilder.append("为了保证软件的正常运行，软件可能会请求使用以下权限");
//        spannableStringBuilder.append(TextTool.setTextStyle("1.访问网络\n",14,true,false));
//        spannableStringBuilder.append("用于获取软件更新、访问Web网页\n\n");
//        spannableStringBuilder.append(TextTool.setTextStyle("2.读写存储\n",14,true,false));
//        spannableStringBuilder.append("数据（歌单、听歌数据等）备份和恢复\n\n");
//        spannableStringBuilder.append(TextTool.setTextStyle("3.前台服务\n",14,true,false));
//        spannableStringBuilder.append("用于前台音乐播放器\n\n");
//        spannableStringBuilder.append(TextTool.setTextStyle("4.悬浮窗\n",14,true,false));
//        spannableStringBuilder.append("用于桌面歌词，状态栏歌词\n\n");
//        spannableStringBuilder.append(TextTool.setTextStyle("5.通知\n",14,true,false));
//        spannableStringBuilder.append("用于在通知栏显示音乐播放控制\n\n");

//        myDialog.setMessage("一切设备以任何方式打开并浏览使用微音乐APP(以下简称“微音乐”)，即表明您已认真阅读此声明并同意接受以下条款与条件的约束，如您不同意下列条款与条件，请勿进入微音乐或访问微音乐中的任何信息。\n" +
//                "\n" +
//                "1.微音乐为免费软件，禁止倒卖或用于任何形式的商业用途。\n" +
//                "\n" +
//                "2.微音乐所有音乐、歌单等数据均在本地进行，不提供网络服务。\n" +
//                "\n" +
//                "3.微音乐只做学习测试使用，因黑客攻击、计算机病毒入侵等不可抗力因素或软件停止维护、软件BUG造成用户数据丢失，微音乐不承担任何责任。\n" +
//                "\n" +
//                "4.联系作者，邮箱：2179853437@qq.com。");

        myDialog.setMessage(spannableStringBuilder);

        myDialog.setYesOnclickListener("我同意", () -> {
            myDialog.dismiss();
            isAgreeStatement = true;
            StorageTool.put("isAgreeStatement", true);
        });
        myDialog.setNoOnclickListener("不同意", () -> {
            myDialog.dismiss();
            isAgreeStatement = false;
            StorageTool.put("isAgreeStatement", false);

        });
        myDialog.setOnDismissListener(dialog -> {
            if(!isAgreeStatement){
                ActivityManager.getInstance().finishApplication();
            }
        });
        myDialog.show();
    }


    private void showUseVerifyDialog(){
        MyDialog myDialog = new MyDialog(this);
        myDialog.setTitle("连接失败");
        myDialog.setMessage("连接失败，请重试!");
        myDialog.setCanceledOnTouchOutside(false);
        myDialog.setYesOnclickListener("我知道了", myDialog::dismiss);
        myDialog.hideNoButton(true);
        myDialog.setOnDismissListener(dialog -> ActivityManager.getInstance().finishApplication());
        myDialog.show();

    }

    private final Handler saveUserMessageHandler = new Handler(Looper.getMainLooper());
//    public static boolean myLikeListIsChange = false;
//    public static boolean songListIsChange = false;
//    public static boolean collectSongListIsChange = false;
    private void startSaveMessage(){
//        saveUserMessageHandler.removeCallbacksAndMessages(null);
//        int time = 1500 * 60 * 10; //15分钟保存一次
//        saveUserMessageHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if(!Application.loginInformation.isNotLogin()){
//                    executeThread(() -> CloudDiskTool.exportData(true));
//                }
//                saveUserMessageHandler.postDelayed(this,time);
//            }
//        }, time);
    }

    public static boolean noScanLocalMusic = true;
    /**
     * 在这里获取到每个需要用到的控件的实例，并给它们设置好必要的点击事件。
     */
    @SuppressLint("RtlHardcoded")
    private void initViews() {
        view_pager = findViewById(R.id.main_view_pager);
        fragmentList = new ArrayList<>();

        fragmentList.add(new Fragment());
        fragmentList.add(new Fragment());
        fragmentList.add(new AboutFragment());

        //设置预加载页数
        view_pager.setOffscreenPageLimit(4);

        view_pager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_SET_USER_VISIBLE_HINT) {
            @NotNull
            @Override
            public Fragment getItem(int i) {
                return fragmentList.get(i);
            }

            @Override
            public int getCount() {
                return fragmentList.size();
            }
        });

        view_pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
            @Override
            public void onPageSelected(int position) {
                if(smoothBottomBar != null){
                    smoothBottomBar.setItemActiveIndex(position);
                }
                view_pager.setCurrentItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

//        view_pager.setViewPageEdgeSlideListener(new CustomViewPager.ViewPageEdgeSlideListener() {
//            @Override
//            public void onStartPageEdgeSlide() {
//                drawer_layout.openDrawer(Gravity.LEFT);
//            }
//
//            @Override
//            public void onEndPageEdgeSlide() {}
//        });

        smoothBottomBar = findViewById(R.id.main_bottomBar);
        smoothBottomBar.setOnItemSelectedListener(i -> {
            view_pager.setCurrentItem(i);
            return false;
        });


        LinearLayout left_view = findViewById(R.id.main_left_view);
        left_view.setPadding(0,PhoneMessage.statusBarHeight,0,0);
        left_view.setOnClickListener(v -> {});//防止点击到底部组
        ViewGroup.LayoutParams leftViewLayoutParams = left_view.getLayoutParams();
        leftViewLayoutParams.width = (int) (PhoneMessage.getWidthPixels() * 0.8);
        left_view.setLayoutParams(leftViewLayoutParams);

        drawer_layout = findViewById(R.id.main_drawer_layout);
        drawer_layout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) { }
            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                isOpenDrawerLayout = true;
            }
            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                isOpenDrawerLayout = false;
            }
            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });


        initLeftView();


        StorageTool.put("noFirstOpenAPP",true);
    }


    public static boolean alreadyShowAdNotice = true;
    public static boolean noHintAdDialog = false;
    public static final String split = "====";


    public boolean isOpenDrawerLayout;
    public int drawerLayoutOpenIntent = 0;


    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean showMusicView;
    private void runTime(){

    }

    public static boolean isLoginActivityReturn;
    @Override
    protected void onStart() {
        super.onStart();
        if(isLoginActivityReturn){

            quit_login.setVisibility(View.GONE);
//            if(Application.loginInformation.isVisitorsLogin()){
//                transition_account.setVisibility(View.VISIBLE);
//            }else {
//                transition_account.setVisibility(View.GONE);
//            }
            isLoginActivityReturn = false;
        }
    }



    private final int MAIN_SCAN_LOCAL_MUSIC_FILE_CODE = 108;




    LinearLayout quit_login;
//    LinearLayout transition_account;

    @SuppressLint({"RtlHardcoded", "SetTextI18n"})
    private void initLeftView() {
        cache_size = findViewById(R.id.cache_size);


        quit_login = findViewById(R.id.quit_login);
//        transition_account = findViewById(R.id.transition_account);
//        if(Application.loginInformation.isNotLogin()){
//            quit_login.setVisibility(View.GONE);
//        }
//        if(Application.loginInformation.isVisitorsLogin()){
//            transition_account.setVisibility(View.VISIBLE);
//        }

//        //游客登录迁移至正式账号
//        transition_account.setOnClickListener(v -> {
//            if(Application.loginInformation.isVisitorsLogin()){
//                Intent intent = new Intent(this, ComponentActivity.class);
//                intent.putExtra("pageName","transition_account");
//                startActivity(intent);
//            }else {
//                transition_account.setVisibility(View.GONE);
//            }
//        });

        //退出登录点击事件
        quit_login.setOnClickListener(v -> {
            final BottomSheetDialog dialog = new BottomSheetDialog(this);
            TextView view1 = Tool.MyBottomSheetDialog.CreateTextView("仅退出页面，音乐继续播放");
            view1.setOnClickListener(v12 -> {
                dialog.dismiss();
                isOpenDrawerLayout = false;
                drawer_layout.closeDrawer(Gravity.LEFT);
                ActivityManager.getInstance().finishAllActivity();
            });
            TextView view2 = Tool.MyBottomSheetDialog.CreateTextView("关闭"+getString(R.string.app_name));
            view2.setOnClickListener(v12 -> {
                dialog.dismiss();
                isOpenDrawerLayout = false;
                drawer_layout.closeDrawer(Gravity.LEFT);
                ActivityManager.getInstance().finishApplication();
            });

            ArrayList<View> viewList = new ArrayList<>();
            viewList.add(view2);
            viewList.add(view1);
            Tool.MyBottomSheetDialog.showBottomSheetDialog(dialog,viewList);
        });

        /*findViewById(R.id.download).setOnClickListener(v -> {
            if(Application.loginInformation.isNotLogin()){
                MyToast.show("该功能需要先登录哦~");
                return;
            }
            final MyDialog myDialog=new MyDialog(this,R.style.MyDialog);
            myDialog.setTitle("下载");
            myDialog.setMessage("从云端同步数据");
            myDialog.setYesOnclickListener("确定", () -> {
                MyProgressDialog myProgressDialog = new MyProgressDialog(this);
                myProgressDialog.setTitleStr("下载数据");
                myProgressDialog.setHintStr("正在同步数据，请稍等...");
                myProgressDialog.show();
                //下载处理
                executeThread(() -> {
                    boolean b = CloudDiskTool.downloadData();
                    runOnUiThread(()-> new Handler().postDelayed(()->{
                        myProgressDialog.dismiss();
                        if(b){
                            MyToast.show("下载成功", Toast.LENGTH_LONG,true);
                        }else {
                            MyToast.show("下载失败", Toast.LENGTH_LONG,true);
                        }
                    },200));
                });
                myDialog.dismiss();
            });
            myDialog.setNoOnclickListener("取消", myDialog::dismiss);
            myDialog.show();
        });*/

//        //上传点击事件
//        findViewById(R.id.uploading).setOnClickListener(v -> {
//            if(Application.loginInformation.isNotLogin()){
//                MyToast.show("该功能需要先登录哦~");
//                return;
//            }
//            if(Application.loginInformation.isVisitorsLogin()){
//                MyToast.show("游客登录的账户不能上传数据哦~");
//                return;
//            }
//            final MyDialog myDialog=new MyDialog(this,R.style.MyDialog);
//            myDialog.setTitle("上传");
//            myDialog.setMessage("将本地数据上传至云端");
//            myDialog.setYesOnclickListener("确定", () -> {
//                //上传处理
//                MyProgressDialog myProgressDialog = new MyProgressDialog(this);
//                myProgressDialog.setTitleStr("上传数据");
//                myProgressDialog.setHintStr("正在同步数据，请稍等...");
//                myProgressDialog.show();
//                executeThread(() -> {
//                    startSaveMessage();
//                    boolean b = CloudDiskTool.uploadingData(false);
//                    runOnUiThread(()-> new Handler().postDelayed(()->{
//                        myProgressDialog.dismiss();
//                        if(b){
//                            MyToast.show("上传成功", Toast.LENGTH_LONG,true);
//                        }else {
//                            MyToast.show("上传失败，请重试", Toast.LENGTH_LONG,false);
//                        }
//                    },200));
//                });
//                myDialog.dismiss();
//            });
//            myDialog.setNoOnclickListener("取消", myDialog::dismiss);
//            myDialog.show();
//        });

        //数据导入导出
        findViewById(R.id.importOrExport).setOnClickListener(v -> {
            String[] items = {"导入（默认导出路径）","导出（保存至内部存储/微音乐）","分享已导出的数据文件","选择指定数据文件导入"};
            MySelectDialog mySelectDialog = new MySelectDialog(this,items,-1);
            mySelectDialog.setTitle("导入/导出数据");
            mySelectDialog.setItemOnClickListener((index2,item,dialog) -> {
                switch (index2){
                    case 0: {
//                        if(!Application.loginInformation.isNotLogin()){
//                            MyToast.show("请先退出当前账号！",Toast.LENGTH_LONG);
//                            return;
//                        }
                        String parent = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Application.context.getString(R.string.app_name);
                        String path = parent + "/MicroMusic.dat";
//                        executeImportData(path);
                        break;
                    }
                    case 1: {
                        MyDialog myDialog = new MyDialog(this);
                        myDialog.setTitle("导出数据");
                        myDialog.setThemeColor(getColor(R.color.Theme2));
                        myDialog.setMessage("导出当前数据（若有数据文件将会被覆盖）");
                        myDialog.setNoOnclickListener("取消", myDialog::dismiss);
                        myDialog.setYesOnclickListener("确定", () -> {
                            myDialog.dismiss();
                            MyProgressDialog myProgressDialog = new MyProgressDialog(this);
                            myProgressDialog.setTitleStr("导出");
                            myProgressDialog.setHintStr("正在导出数据，请稍等...");
                            myProgressDialog.show();
//                            executeThread(() -> {
//                                boolean b = CloudDiskTool.exportData(false);
//                                runOnUiThread(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
//                                    myProgressDialog.dismiss();
//                                    if (b) {
//                                        MyToast.show("数据导出成功,数据文件已保存至:内部存储/" + getString(R.string.app_name) + "/MicroMusic.dat", Toast.LENGTH_LONG, true);
//                                    } else {
//                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
//                                            final MyDialog myDialog2 = new MyDialog(this);
//                                            myDialog2.setTitle("所有文件访问权限");
//                                            myDialog2.setMessage("由于Android 11以上系统限制,需要所有文件访问权限才能保存数据文件");
//                                            myDialog2.setYesOnclickListener("去开启", () -> {
//                                                myDialog2.dismiss();
//                                                XXPermissions.with(this)
//                                                        // 适配 Android 11 需要这样写，这里无需再写 Permission.Group.STORAGE
//                                                        .permission(Permission.MANAGE_EXTERNAL_STORAGE)
//                                                        .request((permissions, all) -> {
//                                                        });
//                                            });
//                                            myDialog2.setNoOnclickListener("取消", myDialog2::dismiss);
//                                            myDialog2.show();
//                                        } else {
////                                        Application.executeThread(() -> {
////                                            Application.loginInformation.clear();
////                                            if (StorageTool.contains("login_information")) {
////                                                StorageTool.delete("login_information");
////                                            }
////                                            FileTool.clearUserData();
////                                        });
//                                            MyToast.show("数据导出发生异常，请重试", Toast.LENGTH_LONG, false);
//                                        }
//                                    }
//                                }, 200));
//                            });
                        });
                        myDialog.show();
                        break;
                    }
                    case 2:{
                        try {

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                                StrictMode.setVmPolicy(builder.build());
                            }
                            String parent = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Application.context.getString(R.string.app_name);
                            File dataFile = new File(parent + "/MicroMusic.dat");
                            if(!dataFile.exists()){
                                MyToast.show("分享失败，找不到数据文件，若文件存在请检查是否开启所有文件访问权限",Toast.LENGTH_LONG,false);
                                return;
                            }
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("application/octet-stream");
                            intent.putExtra(Intent.EXTRA_STREAM,Uri.fromFile(dataFile)); //需要分享的文件URI
                            startActivity(Intent.createChooser(intent, "分享"+Application.context.getString(R.string.app_name)+"数据文件"));
                        }catch (Exception e){
                            MyToast.show("分享出错了",false);
                            e.printStackTrace();
                        }
                        break;
                    }
                    case 3:{
//                        ConsoleUtils.logErr("aaa");
                        Tool.openFileSelector(this,1,
                                "/storage/emulated/0/微音乐/","dat");

//                        FilePickerManager
//                                .from(this)
//                                .maxSelectable(1)
////                                .filter(new AbstractFileFilter() {
////                                    @NonNull
////                                    @Override
////                                    public ArrayList<FileItemBeanImpl> doFilter(@NonNull ArrayList<FileItemBeanImpl> arrayList) {
////                                        ArrayList<FileItemBeanImpl> fileItemBeans = new ArrayList<>();
////                                        for (FileItemBeanImpl fileItemBean : arrayList){
////                                            if (fileItemBean.isChecked() || fileItemBean.getFileType() instanceof RasterImageFileType){
////                                                fileItemBeans.add(fileItemBean);
////                                            }
////                                        }
////                                        return fileItemBeans;
////                                    }
////                                })
//
//                                .showHiddenFiles(true) //显示隐藏文件
//                                .forResult(FilePickerManager.REQUEST_CODE);
                        break;
                    }
                }
                dialog.dismiss();
            });
            mySelectDialog.show();
        });

        //清除缓存
        findViewById(R.id.clear_cache).setOnClickListener(v -> {
            calculateCacheSize();
            cleanCacheTool.showDialog(this,cache_size);
        });

//        findViewById(R.id.setting).setOnClickListener(v -> {
//            Intent intent = new Intent(this, SettingActivity.class);
//            startActivity(intent);
//        });

        calculateCacheSize();
    }






    private final CleanCacheTool cleanCacheTool = CleanCacheTool.getInstance();
    private TextView cache_size;
    /**
     * 计算缓存大小
     */
    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    public void calculateCacheSize(){
        cleanCacheTool.calculateCacheSize(cache_size);
    }


    public static boolean isRemove = false;
    private long touchTime = 0;
    @SuppressLint({"WrongConstant", "ShowToast", "RtlHardcoded"})
    @Override
    public void finish() {
        if(isOpenDrawerLayout){
            drawer_layout.closeDrawer(Gravity.LEFT);
            return;
        }
        if(isRemove){
            super.finish();
            return;
        }
        //返回时提示再按一次退出程序
        long currentTime = System.currentTimeMillis();
        //等待的时间
        if((currentTime-touchTime)>= 1000L) {
            //让Toast的显示时间和等待时间相同
            MyToast.show("再按一次返回键退出", 1000);
            touchTime = currentTime;
        }else {
            moveTaskToBack(true);
        }
    }
}