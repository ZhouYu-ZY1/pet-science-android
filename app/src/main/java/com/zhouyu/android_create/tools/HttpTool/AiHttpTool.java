package com.zhouyu.android_create.tools.HttpTool;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AiHttpTool {
    public static String xunFeiLite(String message){
        try {
            // 构建请求参数
            JSONObject requestData = new JSONObject();
            try {
                requestData.put("model", "generalv1.1");

                JSONArray messages = new JSONArray();
                JSONObject userMessage = new JSONObject();
                userMessage.put("role", "user");
                userMessage.put("content", message);
                messages.put(userMessage);

                requestData.put("messages", messages);
                requestData.put("stream", true);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //https://spark-api-open.xf-yun.com/v1/chat/completions
            String url = "https://spark-api-open.xf-yun.com/v1/chat/completions";
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"),requestData.toString());
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .addHeader("Content-Type","application/json")
                    .addHeader("Authorization","Bearer 0acfe194")
                    .build();
            Response response = HttpTool.client.newCall(request).execute();//发送请求
            return Objects.requireNonNull(response.body()).string();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
