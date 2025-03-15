package com.zhouyu.pet_science.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.zhouyu.pet_science.R;
import com.zhouyu.pet_science.tools.utils.PhoneMessage;

/**
 * 圆角图片（可设置圆角度）
 */
@SuppressLint("AppCompatCustomView")
public class RoundedImageView extends ImageView {
    private static Paint paint;
    private float radius; //圆角度数

    public void setRadius(int radius){
        this.radius = PhoneMessage.dpToPxFloat(radius);
    }

    public RoundedImageView(Context context) {
        this(context,null);
    }

    public RoundedImageView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public RoundedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if(paint == null){
            paint = new Paint();
        }

        @SuppressLint("Recycle")
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundedImageView);
        radius = typedArray.getDimension(R.styleable.RoundedImageView_radius, PhoneMessage.dpToPxFloat(50));
    }

    public static Rect rect;
    /**
     * 绘制圆角矩形图片
     * @author caizhiming
     */
    @Override
    @SuppressLint("DrawAllocation")
    protected void onDraw(Canvas canvas) {
        try {
            Drawable drawable = getDrawable();
            if (drawable instanceof BitmapDrawable) {
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                Bitmap b = getRoundBitmapByShader(bitmap,getWidth(),getHeight(), radius,0);

                if(b == null){
                    super.onDraw(canvas);
                    return;
                }

                if(rect == null){
                    rect = new Rect();
                }

                rect.set(0, 0, b.getWidth(), b.getHeight());
                final Rect rectSrc = rect;
                rect.set(0,0,getWidth(),getHeight());
                final Rect rectDest = rect;

                paint.reset();
                canvas.drawBitmap(b, rectSrc, rectDest, paint);
            } else {
                super.onDraw(canvas);
            }
        }catch (Exception e){
            e.printStackTrace();
            super.onDraw(canvas);
        }
    }

    public static Matrix matrix;
    public static Canvas canvas;
    public static Paint bitmapPaint;
    public static RectF rectf;
    public static Paint boarderPaint;
    public static Bitmap getRoundBitmapByShader(Bitmap bitmap, int outWidth, int outHeight, float radius, int boarder) {
        try {
            if (bitmap == null) {
                return null;
            }
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            if(width <= 0 || height <= 0){
                return null;
            }
            float widthScale = outWidth * 1f / width;
            float heightScale = outHeight * 1f / height;

            if(matrix == null){
                matrix = new Matrix();
            }
            matrix.setScale(widthScale, heightScale);
            //创建输出的bitmap
            Bitmap desBitmap = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888);
            //创建canvas并传入desBitmap，这样绘制的内容都会在desBitmap上
//        Canvas canvas = new Canvas(desBitmap);
            if(canvas == null){
                canvas = new Canvas();
            }
            canvas.setBitmap(desBitmap);

            if(bitmapPaint == null){
                bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            }

            //创建着色器
            BitmapShader bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

            //给着色器配置matrix
            bitmapShader.setLocalMatrix(matrix);

            bitmapPaint.setShader(bitmapShader);
            //创建矩形区域并且预留出border
//        RectF rect = new RectF(boarder, boarder, outWidth - boarder, outHeight - boarder);

            if(rectf == null){
                rectf = new RectF();
            }
            rectf.set(boarder, boarder, outWidth - boarder, outHeight - boarder);

            //把传入的bitmap绘制到圆角矩形区域内
            canvas.drawRoundRect(rectf, radius, radius, bitmapPaint);

            if (boarder > 0) {
                //绘制boarder
                if(boarderPaint == null){
                    boarderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                }
                boarderPaint.setColor(Color.GREEN);
                boarderPaint.setStyle(Paint.Style.STROKE);
                boarderPaint.setStrokeWidth(boarder);
                canvas.drawRoundRect(rectf, radius, radius, boarderPaint);
            }
            return desBitmap;
        }catch (Exception e){
            e.printStackTrace();
            return bitmap;
        }
    }

}




