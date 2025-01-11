package com.zhouyu.android_create.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;


import com.zhouyu.android_create.R;
import com.zhouyu.android_create.activities.parent.BaseActivity;
import com.zhouyu.android_create.tools.CustomSys.MyToast;
import com.zhouyu.android_create.tools.HttpTool.HttpTool;
import com.zhouyu.android_create.tools.TextTool;
import com.zhouyu.android_create.tools.utils.ConsoleUtils;
import com.zhouyu.android_create.views.dialog.MyDialog;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;


public class WebBrowserActivity extends BaseActivity {
    private WebView webView;
    private TextView top_title;
    private ValueCallback<Uri[]> uploadMessageAboveL;
    private boolean notSkipApp;
    private String ua;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_browser);
        Intent intent = getIntent();
        originalUrl = intent.getStringExtra("url");
        singleUrl = intent.getStringExtra("singleUrl");
        ua = intent.getStringExtra("ua");
        notSkipApp = intent.getBooleanExtra("notSkipApp", false);

        if(!TextTool.isEmpty(singleUrl)){
            originalUrl = singleUrl;
        }
        currUrl = originalUrl;

        findViewById(R.id.finish_btn).setOnClickListener(v -> {
            isFinish = true;
            finish();
        });


        top_title = findViewById(R.id.top_title);

        String intentUrl = null;
        if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null) {
            intentUrl = intent.getData().toString();
        }
        if(TextTool.isEmpty(originalUrl) && TextTool.isEmpty(intentUrl)){
            MyToast.show("出错了",false);
            finish();
            return;
        }

        webView = findViewById(R.id.web_view);
        initWebSetting();


        if(intentUrl != null){
            webView.loadUrl(intentUrl);
        }else {
            webView.loadUrl(originalUrl);
        }

        ProgressBar progressBar = findViewById(R.id.web_progress_bar);
        webView.setWebViewClient(new WebViewClient(){
            @SuppressLint("SetTextI18n")
            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                return loading(view,url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return loading(view,url);
            }

            private boolean loading(WebView view, String url){
                if (TextUtils.isEmpty(url)) return false;
                if(!TextTool.isEmpty(singleUrl) && !url.contains("singleUrl")){
//                    if(singleUrl.contains("music.163.com/m/login")){
//                        if(url.contains("st.music.163.com")){
//                            return false; //跳转隐私协议
//                        }
//                        //网易云登录成功，返回刷新
//                        if(url.equals("https://y.music.163.com/m")){
//                            setResult(CommentWebBrowserActivity.WYY_LOGIN_RESULT_CODE);
//                            finish();
//                        }
//                    }
                    return true; // 阻止加载
                }
                if(url.contains("https%3A%2F%2Fm.youlaizhuan.com%2F%23%2Fpages%2Fshare%2Fdownload%3Fgid%3D103") || url.contains("https://m.youlaizhuan.com/#/pages/share/download?gid=103886")){
                    try {
                        Uri uri = Uri.parse("https://m.youlaizhuan.com/#/pages/share/download?gid=103886");
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }catch (Exception ignored){}
                    view.loadUrl(url);
                    return false;
                }

                if (url.startsWith("http:") || url.startsWith("https:") ){
                    ConsoleUtils.logErr(url);

                    //
                    if(url.contains("docs.qq.com") && url.contains("link.html?url=")){
                        Map<String, String> urlParse = TextTool.urlParse(url);
                        String jump = urlParse.get("url");
                        String decode = Uri.decode(jump);

                        //流量卡跳转、 广电终端适配查看
                        if(decode.contains("s=Is7uBouF197581")
                                || decode.contains("shouji.10099.com.cn")  ) {
                            url = decode;
                            Intent intent = new Intent(WebBrowserActivity.this, WebBrowserActivity.class);
                            intent.putExtra("url",url);
                            startActivity(intent);
                            return true;
                        }
//                        //广电终端适配查看
//                        if(decode.contains("shouji.10099.com.cn")){
//                            url = decode;
//                        }
                    }

//                    if(url.contains("s=Is7uBouF197581&id=")){
//                        MyDialog myDialog = new MyDialog(WebBrowserActivity.this);
//                        myDialog.setTitle("提示");
//                        myDialog.setMessage("请认真查看资费、合约等信息再申请");
//                        myDialog.setCanceledOnTouchOutside(false);
//                        myDialog.setYesOnclickListener("我知道了", myDialog::dismiss);
//                        myDialog.hideNoButton(true);
//                        myDialog.show();
//                    }




//                    if(decode.contains("s=6QbysTYi194247")){
//                        Map<String, String> urlParse = TextTool.urlParse(decode);
//                        String id = urlParse.get("id");
//                        ConsoleUtils.logErr(id);
//                        if(!TextTool.isEmpty(id)){
//                            url = "https://ym.ksjhaoka.com/show?s=6QbysTYi194247&id="+id;
//                        }else {
//                            url = "https://ym.ksjhaoka.com/show?s=6QbysTYi194247";
//                        }
//                    }

                    view.loadUrl(url);
                    return false;
                }else {
                    if(!notSkipApp){
                        MyDialog myDialog = new MyDialog(WebBrowserActivity.this);
                        myDialog.setTitle("打开其他APP");
                        myDialog.setMessage("网页请求打开其他APP");
                        String finalUrl = url;
                        myDialog.setYesOnclickListener("打开", () -> {
                            myDialog.dismiss();
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                intent.setData(Uri.parse(finalUrl));
                                startActivityForResult(intent,APP_SKIP_RESULT_CODE);
                            }catch (ActivityNotFoundException e){
                                MyToast.show("开启失败，找不到指定目标",false);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        });
                        myDialog.setNoOnclickListener("取消", myDialog::dismiss);
                        myDialog.show();
                        return true;
                    }else {
                        return false;
                    }
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                changeWebHtml(view,false);
                progressBar.setVisibility(View.VISIBLE);
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                changeWebHtml(view,true);
                progressBar.setVisibility(View.GONE);
                webView.loadUrl("javascript:window.java_fun.loadFinishHtml(document.body.innerHTML,'qq.doc_bottom');");
            }
        });

        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                uploadMessageAboveL = filePathCallback;
                openImageChooserActivity();
                return true;
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                super.onProgressChanged(view, newProgress);
                changeWebHtml(view,false);
            }

        });

        webView.addJavascriptInterface(new MyJavaScriptInterface(), "java_fun");


        findViewById(R.id.refresh_btn).setOnClickListener(v -> webView.reload());
        findViewById(R.id.open_web_browser).setOnClickListener(v -> {
            try {
                Uri uri = Uri.parse(webView.getUrl());
                Intent web_intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(web_intent);
            } catch (Exception e){
                MyToast.show("出错了", Toast.LENGTH_LONG,false);
            }
        });
        runTime();
    }

    private class MyJavaScriptInterface {
//        @JavascriptInterface
//        public void click() {}

        @JavascriptInterface
        public void loadFinishHtml(String html,String type){
            webView.postDelayed(() -> {
                if(!isFinishing() && webView != null){
                    if(type.trim().equals("qq.doc_bottom")){  //隐藏腾讯文档其他部分

                        //顶部内容
                        if(html.contains("id=\"header-container\"")){
                            String fun = "javascript:" +
                                    "function hideOther(){" +
                                    "document.querySelector('#header-container').style.display=\"none\";" +
                                    "document.querySelector('#header-container').remove();" +
                                    "}" +
                                    "hideOther();";
                            webView.loadUrl(fun);
                        }else {
                            webView.loadUrl("javascript:window.java_fun.loadFinishHtml(document.body.innerHTML,'qq.doc_bottom');");
                            return;
                        }

                        webView.postDelayed(() -> {
                            //底部内容
                            if(html.contains("id=\"bottom-exposure-container\"")){
                                String fun = "javascript:" +
                                        "function hideOther(){" +
                                        "document.querySelector('#bottom-exposure-container').style.display=\"none\";" +
                                        "document.querySelector('#bottom-exposure-container').remove();" +
                                        "}" +
                                        "hideOther();";
                                webView.loadUrl(fun);
                            }else {
                                webView.loadUrl("javascript:window.java_fun.loadFinishHtml(document.body.innerHTML,'qq.doc_bottom');");
                            }
                        },500);
                    }
                }
            },500);
        }
    }

    private void changeWebHtml(WebView view, boolean b) {
        String url = view.getUrl();
        if(TextTool.isEmpty(url)){
            return;
        }
        if(url.contains("docs.qq.com/doc/p/")){  //隐藏腾讯文档加载动画
            String fun = "javascript:" +
                    "function hideOther(){" +
                    "document.querySelector('#loading-logo').style.display=\"none\";" +
//                    "document.querySelector('#loading-logo').remove();" +
                    "}" +
                    "hideOther();";
            view.loadUrl(fun);
        }

        if(b){
            if(url.contains("docs.qq.com/doc/p/")){  //隐藏腾讯文档底部内容
                String fun = "javascript:" +
                        "function hideOther(){" +
                        "document.querySelector('.bottom-exposure-container').style.display=\"none\";" +
                        "document.querySelector('.bottom-exposure-container').remove();" +
                        "}" +
                        "hideOther();";
                view.loadUrl(fun);
            }
        }
    }

    @SuppressLint({"SetJavaScriptEnabled", "ObsoleteSdkInt"})
    private void initWebSetting(){
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);//允许使用js
        settings.setDomStorageEnabled(true);
        settings.setBlockNetworkImage(false);//解决图片不显示
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        if(!TextTool.isEmpty(ua)){
            if(ua.equals("windows")){
                //设置浏览器标识
                settings.setUserAgentString("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36");
                //自适应屏幕
                settings.setUseWideViewPort(true);
                settings.setLoadWithOverviewMode(true);
                //自动缩放
                settings.setBuiltInZoomControls(true);
                settings.setSupportZoom(true);
            }else if(ua.equalsIgnoreCase("android")){
                settings.setUserAgentString(HttpTool.randomUA("android"));
                settings.setUseWideViewPort(false);
                settings.setLoadWithOverviewMode(false);

                settings.setBuiltInZoomControls(false);
                settings.setSupportZoom(false);
            }
        }
    }



    private final int APP_SKIP_RESULT_CODE = 107;
    private final int FILE_CHOOSER_RESULT_CODE = 106;
    private void openImageChooserActivity() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        startActivityForResult(Intent.createChooser(i, "Image Chooser"), FILE_CHOOSER_RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (uploadMessageAboveL == null) return;
            onActivityResultAboveL(requestCode, resultCode, data);
        }
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onActivityResultAboveL(int requestCode, int resultCode, Intent intent) {
        if (requestCode != FILE_CHOOSER_RESULT_CODE || uploadMessageAboveL == null)
            return;
        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                String dataString = intent.getDataString();
                ClipData clipData = intent.getClipData();
                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }
                if (dataString != null)
                    results = new Uri[]{Uri.parse(dataString)};
            }
        }
        uploadMessageAboveL.onReceiveValue(results);
        uploadMessageAboveL = null;
    }


    private final Handler handler = new Handler(Looper.getMainLooper());
    private String currUrl;
    private String originalUrl;
    private String singleUrl;
    private String currTitle = "";
    private void runTime(){
        handler.post(new Runnable() {
            @SuppressLint("UseCompatLoadingForColorStateLists")
            @Override
            public void run() {
                if(currUrl != null){
                    String url = webView.getUrl();
                    if(!currUrl.equals(url)){
                        currUrl = url;
                    }
                    String title = webView.getTitle();
                    if(!currTitle.equals(title) && !TextTool.isEmpty(title)){
                        top_title.setText(title);
                        currTitle = title;
                    }
                }
                handler.postDelayed(this,100);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(webView != null){
            webView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(webView != null){
            webView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        if(handler != null){
            handler.removeCallbacksAndMessages(null);
        }
        if(webView != null){
            webView.removeAllViews();
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }

    private boolean isFinish;
    @Override
    public void finish() {
        if(TextTool.isEmpty(currUrl)){
            super.finish();
            return;
        }
        if(webView != null && webView.canGoBack() && !isFinish && !isDestroyed()){
            webView.goBack();
        }else {
            super.finish();
        }
    }
}