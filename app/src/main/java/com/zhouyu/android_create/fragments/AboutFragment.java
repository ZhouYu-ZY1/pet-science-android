package com.zhouyu.android_create.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.zhouyu.android_create.R;
import com.zhouyu.android_create.activities.MainActivity;
import com.zhouyu.android_create.activities.StartActivity;
import com.zhouyu.android_create.activities.WebBrowserActivity;
import com.zhouyu.android_create.application.Application;
import com.zhouyu.android_create.manager.ActivityManager;
import com.zhouyu.android_create.tools.CleanCacheTool;
import com.zhouyu.android_create.tools.CustomSys.MyToast;
import com.zhouyu.android_create.tools.Tool;
import com.zhouyu.android_create.tools.utils.PhoneMessage;
import com.zhouyu.android_create.views.dialog.MyDialog;
import com.zhouyu.android_create.views.dialog.MyProgressDialog;

import org.json.JSONObject;

import java.util.ArrayList;

public class AboutFragment extends Fragment {
    private View view;
    private MainActivity activity;
    private final CleanCacheTool cleanCacheTool = CleanCacheTool.getInstance();
    public static String downLoadUrl = "";

    public AboutFragment(){
        this.activity = (MainActivity) getActivity();
    }
    public AboutFragment(MainActivity activity) {
        this.activity = activity;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_about, container, false);
        initView();
        return view;
    }

    private TextView exemptAdTime;
    private TextView adNum;

    @SuppressLint("SetTextI18n")
    @Override
    public void onStart() {
        super.onStart();
    }



    @SuppressLint("SetTextI18n")
    private void initView() {
        LinearLayout about_content = view.findViewById(R.id.about_content);
        RelativeLayout.LayoutParams setting_contentLayoutParams = (RelativeLayout.LayoutParams) about_content.getLayoutParams();
        setting_contentLayoutParams.topMargin = PhoneMessage.statusBarHeight;
        about_content.setLayoutParams(setting_contentLayoutParams);


        TextView app_name_text = view.findViewById(R.id.app_name_text);
        app_name_text.setText(R.string.app_name);
        //APP版本
        TextView version_text = view.findViewById(R.id.version_text);
        version_text.setText("当前版本：V"+ PhoneMessage.getAppVersionName());

        //检查更新
        view.findViewById(R.id.check_update).setOnClickListener(v -> {
            MyProgressDialog myProgressDialog = new MyProgressDialog(activity);
            myProgressDialog.setTitleStr("检查中");
            myProgressDialog.setHintStr("正在检测新版本，请稍等...");
            myProgressDialog.show();
            activity.executeThread(() -> {
                try {
//                    Beta.checkUpgrade(false,true);
//                    activity.runOnUiThread(myProgressDialog::dismiss);
//                    Map<String, Integer> dirList = CloudDiskTool.getDirList(Application.loginInformation.getCloudDiskNID());
//                    if(dirList == null){
//                        Application.mainHandler.post(() -> MyToast.show("连接出现问题，请加入QQ群或关注微信公众号“微音乐助手”获取最新版本",MyToast.LENGTH_LONG,false));
//                        return;
//                    }

//                    String doc = CloudDiskTool.getDoc(CloudDiskTool.versionsFileID);
                    String doc = StartActivity.getVersionMessage();
                    if (doc != null) {
                        JSONObject jsonObject = new JSONObject(doc);
                        String versionName = jsonObject.getString("versionName");
                        long versionCode = jsonObject.getLong("versionCode");
                        String versionIntroduce = jsonObject.getString("versionIntroduce");

                        String appVersionName = PhoneMessage.getAppVersionName();
                        long appVersionCode = PhoneMessage.getAppVersionCode();

                        myProgressDialog.dismiss();
                        if(versionCode > appVersionCode){
                            final String url = jsonObject.getString("downloadUrl");
                            activity.runOnUiThread(() -> {
                                final MyDialog myDialog = new MyDialog(activity,R.style.MyDialog);
                                myDialog.setTitle("检测到新版本");
                                myDialog.setCanceledOnTouchOutside(false);

                                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
                                spannableStringBuilder.append("当前版本：").append(appVersionName)
                                        .append("\n最新版本：").append(versionName)
                                        .append("\n\n更新内容:\n");

                                spannableStringBuilder.append(Html.fromHtml(
                                        versionIntroduce));
//                                spannableStringBuilder.append(Html.fromHtml("<font color=\"red\"></font>"));
//                                spannableStringBuilder.append(Html.fromHtml("<font color=\"#18A2FD\">蓝色文本</font>"));
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
                                myDialog.show();
                            });
                        }else {
                            Application.mainHandler.post(()-> MyToast.show("已是最新版本",true));
                        }
                    }else {
                        myProgressDialog.dismiss();
                        Application.mainHandler.post(() -> MyToast.show("连接出现问题，请加入QQ群或关注微信公众号“微音乐助手”获取最新版本",MyToast.LENGTH_LONG,false));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });


//        //软件开发者
//        view.findViewById(R.id.developer).setOnClickListener(v -> {
//            Intent intent = new Intent(activity, WebBrowserActivity.class);
//            //https://support.qq.com/products/331421/blog/506703
//            intent.putExtra("url","https://support.qq.com/embed/phone/331421/blog/506703");
//            startActivity(intent);
//        });

        //使用文档

        //常见问题
//        view.findViewById(R.id.course).setOnClickListener(v -> {
//            Intent intent = new Intent(activity, WebBrowserActivity.class);
//            intent.putExtra("url","https://docs.qq.com/doc/DTXpBc3lCSXBITUNZ");
//            startActivity(intent);
//        });


        //软件使用条例
        view.findViewById(R.id.statement).setOnClickListener(v -> {

            Intent intent = new Intent(activity, WebBrowserActivity.class);
            intent.putExtra("url","https://docs.qq.com/doc/p/991885d9fcf3006fa10dd1f640e2f9d5bd296fcf");
            startActivity(intent);
        });

        //隐私政策
        view.findViewById(R.id.privacy_policy).setOnClickListener(v -> {
            Intent intent = new Intent(activity, WebBrowserActivity.class);
            intent.putExtra("url","https://docs.qq.com/doc/p/9b764cc0d3017c94a154f08d2bffc32854680dd1");
            startActivity(intent);
        });


        //意见反馈
        view.findViewById(R.id.feedback).setOnClickListener(v -> {
            Intent intent = new Intent(activity,WebBrowserActivity.class);
            intent.putExtra("url","https://support.qq.com/embed/phone/660381");
            startActivity(intent);
//            int index = -1;
//            String[] items = {"QQ群反馈（紧急优先选择）","反馈页面留言"};
//            MySelectDialog mySelectDialog = new MySelectDialog(activity,items,index);
//            mySelectDialog.setTitle("请选择反馈方式");
//            mySelectDialog.setItemOnClickListener((index2,item,dialog) -> {
//                switch (index2){
//                    case 0:{
//                        AboutFragment.joinQQGroup(AboutFragment.qqGroupKey,activity);
//                        break;
//                    }
//                    case 1:{
//                        Intent intent = new Intent(activity,WebBrowserActivity.class);
//                        intent.putExtra("url","https://support.qq.com/embed/phone/331421");
//                        startActivity(intent);
//                        break;
//                    }
//                }
//                dialog.dismiss();
//            });
//            mySelectDialog.show();
        });

        //打赏
        view.findViewById(R.id.give_reward).setOnClickListener(v -> {
            Intent intent = new Intent(activity, WebBrowserActivity.class);
            intent.putExtra("url","https://support.qq.com/embed/phone/331421/blog/506711");
            startActivity(intent);
        });



        View sim = view.findViewById(R.id.sim_card_btn);
        if(isShowSIM){
            //流量卡
            sim.setOnClickListener(v -> {
                Intent intent = new Intent(activity, WebBrowserActivity.class);
//            intent.putExtra("url","https://support.qq.com/embed/phone/660180/blog/1079220");
//            intent.putExtra("url","https://docs.qq.com/doc/DSlJiQnlWT2tLakpJ");
//                intent.putExtra("url","https://docs.qq.com/doc/p/4a275306192649a3670332a05d642fd4e0598894");
                intent.putExtra("url","https://ym.ksjhaoka.com/?s=Is7uBouF197581");
                startActivity(intent);
            });
            sim.setVisibility(View.VISIBLE);
        }else {
            sim.setVisibility(View.GONE);
        }


        //微信公众号
        view.findViewById(R.id.wx_official_accounts).setOnClickListener(v -> {
            MyDialog myDialog = new MyDialog(activity);
            myDialog.setTitle("微信公众号");
            myDialog.setMessage("是否复制并跳转到微信（需手动粘贴搜索）");
            myDialog.setYesOnclickListener("复制并跳转", () -> {
                PhoneMessage.copy("MicroMusic");
                try {
                    Uri uri = Uri.parse("weixin://");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                } catch (Exception e) {
                    MyToast.show("跳转出错了，请手动打开微信", Toast.LENGTH_LONG);
                    e.printStackTrace();
                }
                myDialog.dismiss();
            });
            myDialog.setNoOnclickListener("取消", myDialog::dismiss);
            myDialog.show();
        });

        //加入QQ群
        view.findViewById(R.id.join_qq_group).setOnClickListener(v -> joinQQGroup(qqGroupKey,activity));

//        //设置
//        view.findViewById(R.id.setting).setOnClickListener(v -> {
//            Intent intent = new Intent(activity, SettingActivity.class);
//            startActivity(intent);
//        });

        //清理缓存
        view.findViewById(R.id.clear_cache).setOnClickListener(v -> {
            cleanCacheTool.calculateCacheSize(null);
            cleanCacheTool.showDialog(activity,null);
        });

        //分享
//        view.findViewById(R.id.share).setOnClickListener(v -> {
//            Intent intent = new Intent(Intent.ACTION_SEND);
//            intent.setType("text/plain");
//            intent.putExtra(Intent.EXTRA_TEXT, "我发现了一个不错的听歌软件，快来下载体验吧："+downLoadUrl);
//            startActivity(Intent.createChooser(intent, "分享给："));
//        });



        //退出/关闭
        view.findViewById(R.id.quit_login).setOnClickListener(v -> {
            final BottomSheetDialog dialog = new BottomSheetDialog(activity);
//            TextView view1 = Tool.MyBottomSheetDialog.CreateTextView("仅退出页面，音乐继续播放");
//            view1.setOnClickListener(v12 -> {
//                dialog.dismiss();
//                ActivityManager.getInstance().finishAllActivity();
//            });
            TextView view2 = Tool.MyBottomSheetDialog.CreateTextView("关闭"+getString(R.string.app_name));
            view2.setOnClickListener(v12 -> {
                dialog.dismiss();
                ActivityManager.getInstance().finishApplication();
            });
            ArrayList<View> viewList = new ArrayList<>();
            viewList.add(view2);
//            viewList.add(view1);
            Tool.MyBottomSheetDialog.showBottomSheetDialog(dialog,viewList);
        });
    }

    public static boolean isShowSIM = false;
    private static final String statement = "本软件所有功能全部免费，请勿盗卖或用于任何形式的商业用途。软件所有数据皆来自互联网接口，如侵犯了您的权益请联系：2179853437@qq.com，我们会第一时间删除屏蔽相关信息。";

    public static final String qqGroupKey = "mDQ76rxe9NULzlXibCe4jc1thTt7OmbO&authKey=YJktfO9TMQEBm3PLm2lAR7al9eAqfa%2Ft8FXGDkCT%2F7kltkSdodBQ62FqxVjSxj%2BU";
    /****************
     *
     * 发起添加群流程。群号：xxxxxxxx 的 key 为： ydoaDyyAM5sk9VxPefuQJo-w6jf9pfK-
     * 调用 joinQQGroup(ydoaDyyAM5sk9VxPefuQJo-w6jf9pfK-) 即可发起手Q客户端申请加群 某某群(xxxxxxxx)
     *
     * @param key 由官网生成的key
     ******************/
    public static void joinQQGroup(String key, Context context) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key));
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            MyToast.show("未安装QQ或安装的版本不支持", Toast.LENGTH_LONG,false);
        }
    }
}
