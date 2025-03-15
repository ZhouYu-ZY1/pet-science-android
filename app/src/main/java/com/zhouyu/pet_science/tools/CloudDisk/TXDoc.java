package com.zhouyu.pet_science.tools.CloudDisk;

import com.zhouyu.pet_science.tools.HttpTool.HttpTool;

import org.json.JSONObject;

import java.util.Objects;

import okhttp3.Request;
import okhttp3.Response;

public class TXDoc {

    public static String getDoc(String id){
        try {
            String url = "https://docs.qq.com/dop-api/opendoc?&u=&id="+id+"&normal=1&preview_token=&t="+System.currentTimeMillis();
            Request request = new Request.Builder().url(url)
                    .addHeader("referer","https://docs.qq.com/doc/"+id)
                    .addHeader("cookie","traceid=3afe0c97cc; TOK=3afe0c97cca6b39f; hashkey=3afe0c97")
                    .addHeader("user-agent", HttpTool.randomUA("windows"))
                    .build();
            Response response = HttpTool.testClient.newCall(request).execute();//发送请求
            String result = Objects.requireNonNull(response.body()).string();
            String[] split = result.split("\n");
            String content = "";
            for (String s : split) {
                if(s.startsWith("{\"commands\":")){
                    JSONObject jsonObject = new JSONObject(s);
                    JSONObject commands = jsonObject.getJSONArray("commands").getJSONObject(0);
                    JSONObject mutations = commands.getJSONArray("mutations").getJSONObject(0);
                    content = mutations.getString("s");
                    content = content.substring(content.indexOf("[[") + 2,content.indexOf("]]"));
                    break;
                }
            }
            return content;
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }

    //
    public static String getYouDaoDoc(String id){
        try {
            String url = "https://note.youdao.com/yws/api/note/"+id+"?sev=j1";
            Request request = new Request.Builder().url(url)
                    .addHeader("user-agent", HttpTool.randomUA("windows"))
                    .build();
            Response response = HttpTool.testClient.newCall(request).execute();//发送请求
            String result = Objects.requireNonNull(response.body()).string();
            JSONObject jsonObject = new JSONObject(result);
            String content = jsonObject.getString("content");
            content = content.substring(content.indexOf("[[") + 2,content.indexOf("]]"));
            content = content.replace("</span>","").replace("&nbsp;"," ");
            return content;
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }


    public void removeHtmlLabel(String label){
        String first = "<"+label;
        String end = "</"+label+">";

    }
}
