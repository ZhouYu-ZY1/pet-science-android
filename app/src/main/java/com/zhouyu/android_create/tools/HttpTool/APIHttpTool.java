package com.zhouyu.android_create.tools.HttpTool;

import com.zhouyu.android_create.tools.utils.ConsoleUtils;

import org.json.JSONObject;

import java.util.Objects;

import okhttp3.Request;
import okhttp3.Response;

public class APIHttpTool {
    /**
     * 网络获取当前时间戳
     * @return currTime
     */
    public static long getCurrTime(){
        long time = 0;
        //淘宝API
        try {
            String url = "https://api.m.taobao.com/rest/api3.do?api=mtop.common.getTimestamp";
            Request request = new Request.Builder()
                    .url(url).build();
            Response response = HttpTool.testClient.newCall(request).execute();//发送请求
            String result = Objects.requireNonNull(response.body()).string();
            JSONObject jsonObject = new JSONObject(result);
            time = Long.parseLong(jsonObject.getJSONObject("data").getString("t"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //苏宁API(1)
        if(time == 0){
            ConsoleUtils.logErr("tbTimeApi失效");
            try {
                String url = "https://f.m.suning.com/api/ct.do";
                Request request = new Request.Builder()
                        .url(url).build();
                Response response = HttpTool.testClient.newCall(request).execute();//发送请求
                String result = Objects.requireNonNull(response.body()).string();
                JSONObject jsonObject = new JSONObject(result);
                time = jsonObject.getLong("currentTime");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //获取不到就用本地时间
        if(time == 0){
            ConsoleUtils.logErr("ygTimeApi失效");
            time = System.currentTimeMillis();

        }
        return time;
    }

}
