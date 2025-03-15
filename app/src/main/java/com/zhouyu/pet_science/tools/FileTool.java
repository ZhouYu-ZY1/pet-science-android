package com.zhouyu.pet_science.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.zhouyu.pet_science.application.Application;
import com.zhouyu.pet_science.tools.CustomSys.MyToast;

import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

public class FileTool {
    /**
     * 对象输入输出流
     */
    public static class ObjectStream {
        public static void writeObjectToFile(Object obj, String path) {
            File file = new File(path);
            try {
                File directoryFile = new File(path.substring(0,path.lastIndexOf("/")));
                if(!directoryFile.exists()){
                    boolean mkdirs = directoryFile.mkdirs();
                    if(!mkdirs){
                        return;
                    }
                }

                FileOutputStream outputStream = new FileOutputStream(file);
                ObjectOutputStream objOut = new ObjectOutputStream(outputStream);
                objOut.writeObject(obj);
                objOut.flush();
                objOut.close();
            }catch (IOException e) {
                Log.e("main", "存储失败");
                e.printStackTrace();
            }
        }

        @SuppressWarnings("unchecked")
        public static <T> T readObjectFromFile(String path) {
            T temp = null;
            File file = new File(path);
            try {
                FileInputStream inputStream = new FileInputStream(file);
                ObjectInputStream objIn = new ObjectInputStream(inputStream);
                temp = (T) objIn.readObject();
                objIn.close();
            } catch (EOFException e){
                boolean b = file.delete();
                if(b){
                    return null;
                }
            }catch (IOException e) {
                Log.e("main", "读取失败");
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                Log.e("main", "文件不存在");
                e.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }
            return temp;
        }
    }

    /**
     * 普通文件读写
     */
    public static class commonStream {

        public static String read(String url){
            String encoding = "UTF-8";
            File file = new File(url);
            int file_length = (int) file.length();
            byte[] file_content = new byte[file_length];
            try {
                FileInputStream in = new FileInputStream(file);
                in.read(file_content);
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                return new String(file_content, encoding);
            } catch (UnsupportedEncodingException e) {
                System.err.println("The OS does not support " + encoding);
                e.printStackTrace();
                return null;
            }
        }

        public static void write(String content,String url){
            OutputStreamWriter outputStreamWriter = null;
            BufferedWriter writer = null;
            try {
                File file = new File(url.substring(0,url.lastIndexOf("/")));
                if(!file.exists()){
                    boolean mkdirs = file.mkdirs();
                    if(!mkdirs){
                        MyToast.show("文件创建失败",false);
                        return;
                    }
                }
                outputStreamWriter = new OutputStreamWriter(new FileOutputStream(url), StandardCharsets.UTF_8);
                writer = new BufferedWriter(outputStreamWriter);
                writer.write(content);
                writer.close();
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                try {
                    if(outputStreamWriter!=null){
                        outputStreamWriter.close();
                    }
                    if(writer!=null){
                        writer.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean BitmapToImage(Bitmap bitmap,int quality,String path,boolean isDocument,String type) {
        try {
            File bitmapFile = new File(path);
            File file = new File(path.substring(0,path.lastIndexOf("/")));
            if(!file.exists()){
                boolean mkdirs = file.mkdirs();
                if(!mkdirs){
                    return false;
                }
            }
            OutputStream out;
            String name = bitmapFile.getName();
            if(isDocument){
                DocumentFile documentFile = DocumentFile.fromFile(bitmapFile);
                if(documentFile.exists()){
                    documentFile.delete();
                }
                documentFile.createFile(type, name);
                out = Application.context.getContentResolver().openOutputStream(documentFile.getUri());
            }else {
                out = new FileOutputStream(bitmapFile);
            }


            if(bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)) {
                out.flush();
                out.close();

                if(isDocument){
                    //保存图片后发送广播通知更新数据库
                    Application.context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(bitmapFile)));
                }
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
        /**
         * bitmap保存图片
         * @param bitmap bitmap参数
         * @param quality 压缩率
         * @param path 保存路径
         */
    public static boolean BitmapToImage(Bitmap bitmap,int quality,String path,boolean isDocument) {
        return BitmapToImage(bitmap,quality,path,isDocument,"image/jpeg");
    }





    /**
     * 读取Assets目录下的图片文件
     * @param context 上下文
     * @param fileName 路径
     */
    public static Bitmap readAssetsImageFile(Context context, String fileName){
        Bitmap bitmap = null;
        AssetManager assetManager = context.getAssets();
        try {
            InputStream inputStream = assetManager.open(fileName);
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 清理音乐图片缓存
     */
    public static void clearImageCache(){
        File file = new File(Application.appCachePath +"/image");
        if(file.exists() && file.isDirectory()){
            File[] files = file.listFiles();
            if(files != null){
                for (File f : files) {
                    f.delete();
                }
                file.delete();
            }
        }
        clearLocalMusicImageCache();
    }

    public static void clearLocalMusicImageCache(){
        File file = new File(Application.appCachePath +"/music_image");
        if(file.exists() && file.isDirectory()){
            File[] files = file.listFiles();
            for (File f : files) {
                f.delete();
            }
            file.delete();
        }
    }


    /**
     * 清除音乐缓存
     */
    public static void clearMusicCache(){
        File file = new File(Application.appCachePath +"/music");
        if(file.exists() && file.isDirectory()){
            File[] files = file.listFiles();
            for (File f : files) {
                f.delete();
            }
            file.delete();
        }
        StorageTool.delete("matchMusicMap");
    }

    public static void removeDirectoryAllFile(File file){
        if(file.exists()){
            if(file.isDirectory()){
                File[] files = file.listFiles();
                for (File file_c : files) {
                    if(file_c.isDirectory()){
                        removeDirectoryAllFile(file_c);
                    }else {
                        file_c.delete();
                    }
                }
            }
            file.delete();
        }
    }

    /**
     * 清除视频缓存
     */
    public static void clearLyricCache(){
        File file = new File(Application.appCachePath +"/lyric");
        if(file.exists() && file.isDirectory()){
            File[] files = file.listFiles();
            for (File f : files) {
                f.delete();
            }
            file.delete();
        }
    }

    /**
     * 清除视频缓存
     */
    public static void clearVideoCache(){
        File file = new File(Application.appCachePath +"/video");
        if(file.exists() && file.isDirectory()){
            File[] files = file.listFiles();
            for (File f : files) {
                f.delete();
            }
            file.delete();
        }
    }

    /**
     * 获取目录大小(字节)
     */
    public static long getDirectorySize(File file){
       if(file.exists()){
           if(file.isDirectory()){
               countSize = 0;
               sizeOfDirectory(file);
               return countSize;
           }else {
               return file.length();
           }
       }else {
           return 0;
       }
    }
    private static long countSize;
    private static void sizeOfDirectory(File file){
        File[] file_list = file.listFiles();
        if(file_list != null && file_list.length != 0){
            for (File list_file : file_list) {
                if(list_file.isDirectory()){
                    sizeOfDirectory(list_file);
                }else {
                    countSize += list_file.length();
                }
            }
        }
    }

    @SuppressLint("DefaultLocale")
    public static String longToMBStr(long size){
        return String.format("%.1f", size / 1024f / 1024) + "M";
    }

    public static String getFileNameWithoutSuffix(String fileName){
        try {
            return fileName.substring(0, fileName.lastIndexOf("."));
        }catch (Exception e){
            e.printStackTrace();
            return fileName;
        }
    }

    //复制文件
    public static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
            }
        }
    }

    /**
     * 文件的复制操作方法
     * @param fromFile 准备复制的文件
     * @param toFile 复制路径
     */
    public static void copyFile(File fromFile, File toFile){
        if(fromFile.getPath().equals(toFile.getPath())){
            return;
        }
        if(!fromFile.exists()){
            return;
        }
        if(!fromFile.isFile()){
            return;
        }
        if(!fromFile.canRead()){
            return;
        }
        if(!toFile.getParentFile().exists()){
            toFile.getParentFile().mkdirs();
        }
        if(toFile.exists()){
            toFile.delete();
        }
        try {
            FileInputStream fosfrom = new FileInputStream(fromFile);
            FileOutputStream fosto = new FileOutputStream(toFile);
            byte[] bt = new byte[1024];
            int c;
            while((c=fosfrom.read(bt)) > 0){
                fosto.write(bt,0,c);
            }
            //关闭输入、输出流
            fosfrom.close();
            fosto.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //File转Uri
    public static Uri getUriForFile(Context context, File file) {
        if (context == null || file == null) {
            throw new NullPointerException();
        }
        Uri uri;
        if (Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(context.getApplicationContext(), "com.zhouyu.pet_science.fileprovider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }

}
