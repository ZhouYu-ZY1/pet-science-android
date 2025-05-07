package com.zhouyu.pet_science.utils;

import com.orhanobut.hawk.Hawk;

public class StorageUtils {
    /**
     * 添加
     */
    public static boolean put(String key,Object value){
        try {
            return Hawk.put(key,value);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除
     */
    public static boolean delete(String key){
        try {
            return Hawk.delete(key);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 获取
     */
    public static <T> T get(String key){
        return Hawk.get(key);
    }

    /**
     * 判断指定Key是否存在
     */
    public static boolean contains(String key){
        try {
            return Hawk.contains(key);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

}
