package com.zhouyu.android_create.tools.HttpTool;

import android.annotation.SuppressLint;

import java.net.Proxy;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpTool {
//    public static String serverURL = "http://103.45.177.179:8000/";
    public static SSLSocketFactory getSSLSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, getTrustManager(), new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    //获取TrustManager
    @SuppressLint("CustomX509TrustManager")
    private static TrustManager[] getTrustManager() {
        return new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) { }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType)  {
//                        String name = chain[0].getSubjectDN().getName();
//                        ConsoleUtils.logErr(name);
//                        ConsoleUtils.logErr(Arrays.toString(chain[0].getSignature()));
//                        X509Certificate myCrt = myCrt
//                        boolean isExist = false;
//                        for (String s : allowCallDomain) {
//                            if(name.contains(s)){
//                                isExist = true;
//                                break;
//                            }
//                        }
//                        if (isExist) {
//                            throw new SSLHandshakeException("验证失败！");
//                        }
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
        };
    }
    public static X509TrustManager getX509TrustManager() {
        X509TrustManager trustManager = null;
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
            }
            trustManager = (X509TrustManager) trustManagers[0];
        } catch (Exception e) {
            e.printStackTrace();
        }

        return trustManager;
    }

//    private static String[] allowCallDomain = new String[]{"quqi.com","music.163.com","qq.com","kuwo.cn","kugou.com"};

    //获取HostnameVerifier
    private static HostnameVerifier getHostnameVerifier() {
        return (s, sslSession) -> {
//            ConsoleUtils.logErr(s);
//            boolean isExist = false;
//            for (String name : allowCallDomain) {
//                if(s.contains(name)){
//                    isExist = true;
//                    break;
//                }
//            }
//            return isExist;
            return true;
        };
    }

    public static boolean isShowTestVersionDialog = false;
    public static OkHttpClient client = new OkHttpClient()
            .newBuilder()
            .sslSocketFactory(getSSLSocketFactory(), getX509TrustManager())
//            .hostnameVerifier(getHostnameVerifier())
//            .addInterceptor(chain -> {
//                Request request = chain.request();
//                String url = request.url().toString();
//                Request.Builder builder = request.newBuilder();
//
//                boolean allow = true;
//                if(MainActivity.limit){
//                    allow = Application.listeningTime > MainActivity.limit_listen_time;
//                }
//
//                if(!MainActivity.useVerify){
//                    builder.url("");
//                } else if(!allow) {
//                    builder.url(""); //访问空链接
//                } else if(BaseActivity.isVpnUsed() || BaseActivity.isWifiProxy()){  //如果使用了VPN代理或未验证版本则禁止请求
//                    builder.url(""); //访问空链接
//                } else if(!MainActivity.isTestVersion){ //如果未验证版本则先验证版本
//                    String doc = StartActivity.getVersionMessage();
//                    if (TextTool.isEmpty(doc)) {
//                        //判断是否连接网络
//                        if(!isShowTestVersionDialog && PhoneMessage.isConnectNetwork(Application.context)) {
//                            boolean b = HttpTool.testUrlConnection("http://www.baidu.com");
//                            //判断网络是否可用
//                            if (b) {
//                                if(!isShowTestVersionDialog){
//                                    Activity currActivity = ActivityManager.getInstance().getCurrActivity();
//                                    currActivity.runOnUiThread(() -> {
//                                        MyDialog myDialog = new MyDialog(currActivity);
//                                        myDialog.setTitle("版本验证失败");
//                                        //请重启APP尝试或前往[微音乐助手]公众号下载最新版
//                                        myDialog.setMessageGravity(Gravity.START);
//                                        myDialog.setMessage("版本信息验证失败，原因可能如下：\n\n1.版本验证出错，可尝试重启APP\n2.当前版本已不再维护，前往[微音乐助手]公众号下载最新版\n3.软件正在维护更新，请耐心等待维护完成");
//                                        myDialog.setYesOnclickListener("重试", myDialog::dismiss);
//                                        myDialog.setCanceledOnTouchOutside(false);
//                                        myDialog.setNoOnclickListener("退出", () -> {
//                                            myDialog.dismiss();
//                                            ActivityManager.getInstance().finishApplication();
//                                        });
//                                        myDialog.setOnDismissListener(dialog -> isShowTestVersionDialog = false);
//                                        if(!isShowTestVersionDialog){
//                                            isShowTestVersionDialog = true;
//                                            myDialog.show();
//                                        }
//                                    });
//                                }
//                            }
//                        }
//
//                        MainActivity.isTestVersion = false;
//                        builder.url(""); //访问空链接
//                    }else {
//
//                        /*
//                         * 不获取付费接口,断网进入APP重新加载无法使用付费接口
//                         * 但可以保证旧版本，强制更新/未验证KEY，断网进入的用户也无法获取到付费接口
//                         *
//                         */
//                        //验证信息
//                        boolean verify = false;
//                        try {
//                            JSONObject jsonObject = new JSONObject(doc);
//                            long versionCode = jsonObject.getLong("versionCode");
//                            long appVersionCode = PhoneMessage.getAppVersionCode();
//                            boolean isCoerceUpdate = false;
//                            boolean isMaintenance = false;
//                            boolean isLessMinVersion = false;
//                            //测试密钥
//                            if(!jsonObject.isNull("key")){
//                                StartActivity.secretKey = jsonObject.getString("key");
//                            }
//                            if(!jsonObject.isNull("isCoerceUpdate")){
//                                isCoerceUpdate = jsonObject.getBoolean("isCoerceUpdate");
//                            }
//                            if(!jsonObject.isNull("minVersion")){
//                                int minVersion = jsonObject.getInt("minVersion");
//                                if(minVersion > appVersionCode){
//                                    isLessMinVersion = true;
//                                }
//                            }
//                            if(!jsonObject.isNull("isMaintenance")){
//                                isMaintenance = jsonObject.getBoolean("isMaintenance");
//                            }
//                            StorageTool.put("isMaintenance", isMaintenance);
//
//                            boolean isVersionOvertop = (versionCode - appVersionCode) < -5; //不能超出5个版本
//                            boolean keyVerify = true;
//                            if(StorageTool.contains("appSecretKey")){
//                                String appSecretKey = StorageTool.get("appSecretKey");
//                                if(appSecretKey.equalsIgnoreCase(StartActivity.secretKey)){
//                                    keyVerify = true;
//                                    ConsoleUtils.logErr("key验证成功");
//                                }
//                            }
//                            boolean versionVerify = false;
//                            if(keyVerify){ //key验证成功才能进行版本验证
//                                if(versionCode > appVersionCode || isVersionOvertop){ //检测是否需要更新
//                                    //需要更新
//                                    if(!isMaintenance && !isCoerceUpdate && !isLessMinVersion){ //需要不存在以下情况：维护/强制更新/版本过低
//                                        versionVerify = true;
//                                        ConsoleUtils.logErr("更新版本验证成功");
//                                    }
//                                }else {
//                                    //无需更新
//                                    if(!isMaintenance){ //需要不存在以下情况：维护
//                                        versionVerify = true;
//                                        ConsoleUtils.logErr("版本验证成功");
//                                    }
//                                }
//                            }else {
//                                ConsoleUtils.logErr("key验证失败");
//                            }
//
//                            boolean allow_visit = false;
//                            if(!jsonObject.isNull("allow_visit")){
//                                allow_visit = jsonObject.getBoolean("allow_visit");
//                            }
//                            if(keyVerify && versionVerify && allow_visit){ //验证key、验证版本
//                                //验证成功，重新获取付费接口
//                                if(!jsonObject.isNull("closeFeePort")){
//                                    StartActivity.closeFeePort = jsonObject.getBoolean("closeFeePort");
//                                }
//                                //云音乐cookie（版本更新加载）
//                                if(!jsonObject.isNull("wyyCookie")){
//                                    String wyyCookie = jsonObject.getString("wyyCookie");
//                                    if(!TextTool.isEmpty(wyyCookie)){
//                                        WYYMusicHttpTool.vipCookie = wyyCookie;
//                                    }
//                                }
//                                verify = true;
//                            }
//                        }catch (Exception e){
//                            e.printStackTrace();
//                        }
//                        if(verify){
//                            //允许访问
//                            MainActivity.isTestVersion = true;
//                            builder.url(url);
//                        }else{
//                            MainActivity.isTestVersion = false;
//                            builder.url(""); //访问空链接
//                        }
//                    }
//                }
//                return chain.proceed(builder.build());
//            })
//            .proxy(Proxy.NO_PROXY) //禁止使用代理
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS).build();

    public static OkHttpClient testClient = new OkHttpClient()
            .newBuilder()
            .proxy(Proxy.NO_PROXY) //禁止使用代理
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(6, TimeUnit.SECONDS).build();


    /**
     * 测试url是否可用
     */
    public static boolean testUrlConnection(String url){
        boolean isOk = false;
        Response response = null;
        try {
            Request request = new Request.Builder().url(url).build();
            response = testClient.newCall(request).execute();//发送请求
            int code = response.code();
            if (200 == code) {
                // 200是请求地址顺利连通。。
                isOk = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(response!=null){
                response.close();
            }
        }
        return isOk;
    }

    public static String randomUA(String type){
        Random random = new Random();
        int r1 = random.nextInt(9) + 1;
        int r2 = random.nextInt(9) + 1;
        int r3 = random.nextInt(9) + 1;
        int r4 = random.nextInt(9) + 1;
        if(type.equalsIgnoreCase("android")){
            return "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/"+r1+"5."+r4+".4"+r2+"32.2"+r3+"2 Mobile Safari/5"+r4+"5.06";
        }else if(type.equalsIgnoreCase("win2")){
            return "Mozilla/5.0 (Windows; U; Windows NT 5.2;. en-US) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/"+r1+"5."+r4+".2"+r2+"32.2"+r3+"2 Mobile Safari/5"+r4+"3.06";
        }else {
            return "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/"+r1+"5."+r4+".2"+r2+"32.2"+r3+"2 Mobile Safari/5"+r4+"3.06";
        }
    }
}
