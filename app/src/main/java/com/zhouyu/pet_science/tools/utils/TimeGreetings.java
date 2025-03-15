package com.zhouyu.pet_science.tools.utils;

import android.annotation.SuppressLint;
import android.widget.Toast;

import com.zhouyu.pet_science.tools.CustomSys.MyToast;

import java.util.Calendar;

public class TimeGreetings {
    Calendar calendar = Calendar.getInstance();

    @SuppressLint("ShowToast")
    public void showGreetings(){
        String startStr = "";
//        if(!Application.loginInformation.isNotLogin()){
//            startStr = "@"+Application.loginInformation.getNickname()+"，";
//        }
        String endStr = getEndStr();
        int h = calendar.get(Calendar.HOUR_OF_DAY);
        if(h < 5){
            MyToast.show(startStr+"现在是休息时间，记得早点休息哦~",Toast.LENGTH_LONG);
        }else if(h < 9){
            MyToast.show(startStr+"早上好"+endStr+"~",Toast.LENGTH_LONG);
        }else if(h < 11){
            MyToast.show(startStr+"上午好"+endStr+"~",Toast.LENGTH_LONG);
        }else if(h < 13){
            MyToast.show(startStr+"中午好"+endStr+"~",Toast.LENGTH_LONG);
        }else if(h < 17){
            MyToast.show(startStr+"下午好"+endStr+"~",Toast.LENGTH_LONG);
        }else if(h < 20){
            MyToast.show(startStr+"Hi"+endStr+"~",Toast.LENGTH_LONG);
        }else if(h < 22){
            MyToast.show(startStr+"晚上好"+endStr+"~",Toast.LENGTH_LONG);
        }else {
            MyToast.show(startStr+"夜深了，记得早点休息哦~",Toast.LENGTH_LONG);
        }
    }

    private String getEndStr() {
        String endStr = "";
        try {
            int month = calendar.get(Calendar.MONTH) + 1;
            int day_Month = calendar.get(Calendar.DAY_OF_MONTH);

//            if(!Application.loginInformation.isNotLogin() || Application.loginInformation.isVisitorsLogin()){
//                long birthday = Application.loginInformation.getBirthday();
//                if(birthday != 0){
//                    Calendar bCalendar = Calendar.getInstance();
//                    bCalendar.setTime(new Date(birthday));
//                    int bMonth = bCalendar.get(Calendar.MONTH) + 1;
//                    int bDay = bCalendar.get(Calendar.DAY_OF_MONTH);
//                    if(month == bMonth && day_Month == bDay){
//                        return "，祝您生日快乐";
//                    }
//                }
//            }

            switch (month){
                case 1:{
                    if(day_Month == 1){
                        endStr = "，祝您元旦节快乐";
                    }
                    break;
                }
                case 5:{
                    if(day_Month == 1){
                        endStr = "，祝您五一劳动节快乐";
                    }
                    break;
                }
                case 6:{
                    if(day_Month == 1){
                        endStr = "，祝您六一快乐";
                    }
                    break;
                }
                case 9:{
                    if(day_Month == 10){
                        endStr = "，祝您教师节快乐";
                    }
                    break;
                }
                case 10:{
                    if (day_Month == 1){
                        endStr = "，祝愿祖国繁荣昌盛，国庆节快乐";
                    }
                }
            }

            LunarUtil lunarUtil = new LunarUtil(calendar);
            String lunarDay = lunarUtil.getLunarDay();
            switch (lunarUtil.getLunarMonth()){
                case "十二":{
                    if(lunarDay.equals("廿三") || lunarDay.equals("廿四")){
                        endStr = "，祝您小年快乐";
                    }
                    break;
                }
                case  "一":{
                    if(lunarDay.equals("初一")){
                        endStr = "，祝您新年快乐";
                    }else if(lunarDay.equals("十五")){
                        endStr = "，祝您元宵节快乐";
                    }
                    break;
                }
                case "五":{
                    if(lunarDay.equals("初五")){
                        endStr = "，祝您端午安康";
                    }
                    break;
                }
//                case "七":{
//                    if(lunarDay.equals("初七")){
//                        endStr = "，祝你七夕节快乐";
//                    }
//                    break;
//                }
                case "八":{
                    if(lunarDay.equals("十五")){
                        endStr = "，祝您中秋节快乐";
                    }
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return endStr;
    }

    /**
     * 获取节日
     */
    public static String getFestival(Calendar calendar){
        String endStr = "";
        try {
            int month = calendar.get(Calendar.MONTH) + 1;
            int day_Month = calendar.get(Calendar.DAY_OF_MONTH);

            switch (month){
                case 1:{
                    if(day_Month == 1){
                        endStr = "元旦节";
                    }
                    break;
                }
                case 5:{
                    if(day_Month == 1){
                        endStr = "劳动节";
                    }
                    break;
                }
                case 9:{
                    if(day_Month == 10){
                        endStr = "教师节";
                    }
                    break;
                }
                case 10:{
                    if (day_Month == 1){
                        endStr = "国庆节";
                    }
                }
            }

            LunarUtil lunarUtil = new LunarUtil(calendar);
            String lunarDay = lunarUtil.getLunarDay();
            switch (lunarUtil.getLunarMonth()){
                case "十二":{
                    if(lunarDay.equals("廿三") || lunarDay.equals("廿四")){
                        endStr = "小年";
                    }
                    break;
                }
                case  "一":{
                    if(lunarDay.equals("初一")){
                        endStr = "新年";
                    }else if(lunarDay.equals("十五")){
                        endStr = "元宵节";
                    }
                    break;
                }
                case "五":{
                    if(lunarDay.equals("初五")){
                        endStr = "端午节";
                    }
                    break;
                }
                case "七":{
                    if(lunarDay.equals("初七")){
                        endStr = "七夕节";
                    }
                    break;
                }
                case "八":{
                    if(lunarDay.equals("十五")){
                        endStr = "中秋节";
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return endStr;
    }

}
