package com.zhouyu.android_create.tools;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

import androidx.annotation.ColorInt;

import com.zhouyu.android_create.application.Application;

import net.sourceforge.pinyin4j.PinyinHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextTool {

    /**
     * 设置文字样式
     * @return
     */
    public static CharSequence setTextStyle(String text,int dp_size,boolean isBold,boolean isUnderline){
        SpannableString spannableString = new SpannableString(text);

        //设置字体大小
        spannableString.setSpan(new AbsoluteSizeSpan(dp_size, true), 0, spannableString.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        //设置加粗
        if(isBold){
            StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
            spannableString.setSpan(boldSpan, 0, spannableString.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }

        //设置下划线
        if(isUnderline){
            spannableString.setSpan(new UnderlineSpan(),0,spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return spannableString;
    }


    // 将中文字符串转换为拼音字符串
    public static String convertToPinyin(String chinese) {
        StringBuilder pinyin = new StringBuilder();
        for (int i = 0; i < chinese.length(); i++) {
            String[] pyArray = PinyinHelper.toHanyuPinyinStringArray(chinese.charAt(i));
            if (pyArray != null) {
                pinyin.append(Arrays.toString(pyArray));
                pinyin.append(" "); //使用空格分割每个拼音
            }
        }
        if (pinyin.length() > 0) {
            pinyin.deleteCharAt(pinyin.length() - 1); // 移除最后一个空格
        }
        return pinyin.toString();
    }


    /**
     * 改变特定文字的颜色
     */
    public static SpannableString changeFindTextColor(String text, String keyword,@ColorInt int color) {
        SpannableString ss = new SpannableString(text);
        try {
            Pattern pattern = Pattern.compile(keyword);
            Matcher matcher = pattern.matcher(ss);
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                ss.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
//        if (!TextUtils.isEmpty(text) && !TextUtils.isEmpty(text) && text.contains(keyword)) {
//            int start = text.indexOf(keyword);
//            int end = start + keyword.length();
//            ss.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        }
        return ss;
    }


    /**
     * 解析出url参数中的键值对
     */
    public static Map<String, String> urlParse(String URL) {
        Map<String, String> mapRequest = new HashMap<>();
        String[] arrSplit;
        String strUrlParam = TruncateUrlPage(URL);
        if(strUrlParam == null){
            return mapRequest;
        }
        arrSplit = strUrlParam.split("[&]");
        for(String strSplit : arrSplit){
            String[] arrSplitEqual;
            arrSplitEqual = strSplit.split("[=]");
            //解析出键值
            if(arrSplitEqual.length > 1){
                //正确解析
                mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);
            }else{
                if(!arrSplitEqual[0].equals("")){
                    mapRequest.put(arrSplitEqual[0], "");
                }
            }
        }
        return mapRequest;
    }
    /**
     * 去掉url中的路径，留下请求参数部分
     */
    private static String TruncateUrlPage(String strURL){
        String strAllParam=null;
        String[] arrSplit;
        strURL = strURL.trim();
        arrSplit = strURL.split("[?]");
        if(strURL.length() > 1){
            if(arrSplit.length > 1){
                for (int i = 1; i < arrSplit.length; i++){
                    strAllParam = arrSplit[i];
                }
            }
        }
        return strAllParam;
    }


    /**
     * 从文本中提取url
     */
    public static List<String> findUrls(String url) {
        List<String> result = new ArrayList<>();
        Pattern pattern = Pattern.compile(
                "\\b(((ht|f)tp(s?)://|~/|/)|www.)" +

                        "(\\w+:\\w+@)?(([-\\w]+\\.)+(com|org|net|gov" +

                        "|mil|biz|info|mobi|name|aero|jobs|museum" +

                        "|travel|[a-z]{2}))(:[\\d]{1,5})?" +

                        "(((/([-\\w~!$+|.,=]|%[a-f\\d]{2})+)+|/)+|\\?|#)?" +

                        "((\\?([-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?" +

                        "([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)" +

                        "(&(?:[-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?" +

                        "([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)*)*" +

                        "(#([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)?\\b");
        Matcher matcher = pattern.matcher(url);
        while (matcher.find()) {
            result.add(matcher.group());
        }
        return result;
    }

    /**
     * 获取cookie的值
     * 类似这样的字符串：
     * 	c=1;b=12;a=123
     */
    public static String getCookieValue(String cookies, String key) {
        Pattern pattern = Pattern.compile(key + "[\\s]*=[\\s]*([^;\\s]{1,})");
        Matcher matcher = pattern.matcher(cookies);
        if(matcher.find()){
            if(matcher.groupCount() == 1){
                return matcher.group(1);
            }
        }
        return null;
    }
    public static String setCookieValue(String cookies,String key,String value){
        return cookies.replaceFirst(key + "[\\s]*=[\\s]*([^;\\s]{1,})", key + " = " + value);
    }

    //判断文本是否为空
    public static boolean isEmpty(String str){
        return str == null || str.trim().equals("") || str.trim().equals("null");
    }


    /**
     * 获取当前日期是星期几
     */
    public static String getWeekOfCalendar(Calendar calendar) {
        String[] weekDays = {"星期天", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        int w = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0){
            w = 0;
        }
        return weekDays[w];
    }

    /**
     * 获取当天剩余的时间，单位：秒
     * @return
     */
    public static long getTodayRemainSecondNum() {
        long current = System.currentTimeMillis();    //当前时间毫秒数
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(current));
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long tomorrowZero = calendar.getTimeInMillis();
        return tomorrowZero - current;
    }

    /**
     * 生成随机数字和字母组合
     */
    public static String getRandomNickname(int length) {
        StringBuilder val = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            // 输出字母还是数字
            String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num";
            // 字符串
            if ("char".equalsIgnoreCase(charOrNum)) {
                // 取得大写字母还是小写字母
                int choice = random.nextInt(2) % 2 == 0 ? 65 : 97;
                val.append((char) (choice + random.nextInt(26)));
            } else { // 数字
                val.append(random.nextInt(10));
            }
        }
        return val.toString();
    }

    /**
     * 获取复制内容
     */
    public static String getCopyContent(){
        String content = "";
        ClipboardManager clipboardManager =(ClipboardManager) Application.context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (null != clipboardManager) {
            // 获取剪贴板的剪贴数据集
            ClipData clipData = clipboardManager.getPrimaryClip();
            if (null != clipData && clipData.getItemCount() > 0) {
                // 从数据集中获取第一条文本数据
                ClipData.Item item = clipData.getItemAt(0);
                if (null != item) {
                    content = item.getText().toString();
                }
            }
        }
        return content;
    }

    /*生成numSize位16进制的数*/
    public static String getRandomValue(int numSize) {
        String str = "";
        for (int i = 0; i < numSize; i++) {
            char temp = 0;
            int key = (int) (Math.random() * 2);
            switch (key) {
                case 0:
                    temp = (char) (Math.random() * 10 + 48);//产生随机数字
                    break;
                case 1:
                    temp = (char) (Math.random() * 6 + 'a');//产生a-f
                    break;
                default:
                    break;
            }
            str = str + temp;
        }
        return str;
    }


    public static String replaceQQMusicListIntroduce(String introduce){
        if(isEmpty(introduce)){
            return "";
        }
        return introduce.replace("<br>","\n")
                .replace("</br>","\n").replace("&ensp;"," ").replace("&emsp;"," ")
                .replace("&nbsp"," ").replace("&#160;"," ")
                .replace("&#128156;","").replace("&#8206;","").replace("&#128536;","")
                .replace("&#32;"," ")
                .replace("&#124;","|")
                .replace("&#58;",":")
                .replace("&#46;",".")
                .replace("&#45;","-")
                .replace("&#9996;","✌")
                .replace("&#10084;","♥")
                .replace("&#9825;","♡")
                .replace("&#33;","!")
                .replace("&#65039;","")
                .replace("\\n","\n");
    }
}
