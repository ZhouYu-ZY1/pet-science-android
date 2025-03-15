package com.zhouyu.pet_science.tools.HttpTool;

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

    public static OkHttpClient client = new OkHttpClient()
            .newBuilder()
            .sslSocketFactory(getSSLSocketFactory(), getX509TrustManager())
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
