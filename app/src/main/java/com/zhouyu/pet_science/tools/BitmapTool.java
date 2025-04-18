package com.zhouyu.pet_science.tools;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Base64;

import com.zhouyu.pet_science.application.Application;
import com.zhouyu.pet_science.network.HttpUtils;
import com.zhouyu.pet_science.tools.utils.GlideEngine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import androidx.palette.graphics.Palette;

import okhttp3.Request;
import okhttp3.Response;

public class BitmapTool {

    /**
     * * 生成圆角Bitmap
     * @param bitmap 原bitmap
     * @param radius 圆角大小
     * @return 圆角后的bitmap
     */
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int radius) {
        if(bitmap == null){
            return null;
        }
        try {
            Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);

            final int color = 0xff424242;
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            final RectF rectF = new RectF(rect);

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);

            canvas.drawRoundRect(rectF, radius, radius, paint);

            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);
            return output;
        }catch (Exception e){
//            e.printStackTrace();
            return bitmap;
        }
    }

    /**
     * url获取bitmap
     */
    public static Bitmap urlToBitmap(String url){
        try {
            return GlideEngine.getInstance().getCacheBitmap(Application.context,url,1000,1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 得到Bitmap的大小
     */
    @SuppressLint("ObsoleteSdkInt")
    public static long getBitmapSize(Bitmap bitmap) {
        if(bitmap == null){
            return 0;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {    //API 19
            return bitmap.getAllocationByteCount();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {//API 12
            return bitmap.getByteCount();
        }

        // 在低版本中用一行的字节x高度
        return bitmap.getRowBytes() * bitmap.getHeight();                //earlier version
    }

    //获取bitmap颜色
    public static Palette.Swatch getBitmapColor(Bitmap bitmap, int type) {
        // Palette的部分
        Palette palette = Palette.from(bitmap).generate();
        Palette.Swatch swatch = null;
        if (palette != null) {
            switch (type) {
                case 1:
                    //获取充满活力的亮
                    swatch = palette.getLightVibrantSwatch();
                    break;
                case 2:
                    //获取充满活力的黑
                    swatch = palette.getDarkVibrantSwatch();
                    break;
                case 3:
                    //获取柔和的色调
                    swatch = palette.getMutedSwatch();
                    break;
                case 4:
                    //获取柔和的亮
                    swatch = palette.getLightMutedSwatch();
                    break;
                case 5:
                    //获取柔和的黑
                    swatch = palette.getDarkMutedSwatch();
                    break;
                case 6:
                    //获取充满活力的色调
                    swatch = palette.getVibrantSwatch();
                    break;
            }
            if(swatch == null){
                swatch = palette.getVibrantSwatch();
            }
        }
        return swatch;
    }

    /**
     * * 将bitmap裁剪为正方形
     */
    public static Bitmap cropToSquare(Bitmap source) {
        int size = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;

        return Bitmap.createBitmap(source, x, y, size, size);
    }

    /**
     * 图片模糊处理
     */
    public static class BlurBitmap {
        /**
         * 模糊图片
         * @param radius 模糊半径
         * @param simple 缩小比例
         */
        public static Bitmap blur(Context context, Bitmap bitmap, int radius, int simple) {
            if (simple > 1) {
                bitmap = compressBitmap(bitmap, simple);
            }
            return blurByRenderScript(context, bitmap, radius);
        }

        /**
         * 模糊
         */
        public static Bitmap blurByRenderScript(Context context, Bitmap bitmap, int radius) {
            try {
                RenderScript rs = RenderScript.create(context);
                Allocation allocation = Allocation.createFromBitmap(rs, bitmap);
                ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs, allocation.getElement());
                blur.setInput(allocation);
                blur.setRadius(radius);
                blur.forEach(allocation);
                allocation.copyTo(bitmap);

                rs.destroy();
            }catch (Exception e){
               e.printStackTrace();
            }
            return bitmap;
        }
        /**
         * 缩放
         */
        private static Bitmap compressBitmap(Bitmap bitmap, int simple) {
            if (simple > 1) {
                if(bitmap.getWidth() < 200){
                    return bitmap;
                }
                bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / simple,
                        bitmap.getHeight() / simple, true);
            }
            return bitmap;
        }
    }


    /**
     * 将字符串转换成Bitmap
     * @param string Bitmap字符串
     * @return Bitmap位图
     */
    public static Bitmap stringToBitmap(String string) {
        Bitmap bitmap = null;
        try {
            byte[] bitmapArray;
            bitmapArray = Base64.decode(string, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0,bitmapArray.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 将Bitmap转为String
     * @param bitmap Bitmap位图
     * @return Bitmap字符串
     */
    public static String bitmapToString(Bitmap bitmap) {
        String string;
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bStream);
        byte[] bytes = bStream.toByteArray();
        string = Base64.encodeToString(bytes, Base64.DEFAULT);
        return string;
    }

    /**
     * 文件转bitmap
     */
    public static Bitmap bitmapFromFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            // 使用BitmapFactory的decodeFile方法从文件路径创建Bitmap
            return BitmapFactory.decodeFile(filePath);
        }
        return null;
    }



    /**
     * bitmap转文件
     */
    public static File bitmapToFile(Bitmap bitmap,String fileName) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos); //质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 2048) {  //循环判断如果压缩后图片是否大于2M,大于继续压缩
            baos.reset();  //重置baos即清空baos
            options -= 5; //每次都减少5
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
        }
        File dirFile = new File(Application.appFilesPath + "/bitmapFile");
        if(!dirFile.exists()){
            boolean mkdirs = dirFile.mkdirs();
            if(!mkdirs){
                return null;
            }
        }
        File file = new File(dirFile,fileName);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            try {
                fos.write(baos.toByteArray());
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return file;
    }

    /**
     * 根据传入url获取到图片的Bitmap对象
     */
    public static Bitmap getBitmap(final String imgUrl) {
        Bitmap bm;
        Request request = new Request.Builder().url(imgUrl).build();
        try (Response response = HttpUtils.client.newCall(request).execute()) {
            //发送请求
            InputStream bis = Objects.requireNonNull(response.body()).byteStream();
            bm = BitmapFactory.decodeStream(bis);
        } catch (Exception e) {
            return null;
        }
        long bitmapSize = BitmapTool.getBitmapSize(bm);  //图片过大，先压缩大小
        if(bitmapSize > 32000000){
            bm = Bitmap.createScaledBitmap(bm, 500, 500, true);
        }
        return bm;
    }

    // Drawable转换成Bitmap
    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap
                .createBitmap(
                        drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(),
                        drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    // 将Bitmap转换成InputStream
    public static InputStream bitmapToInputStream(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return new ByteArrayInputStream(baos.toByteArray());
    }


}
