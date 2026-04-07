package com.example.wusthelper.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class DrawableUtil {

    private static final String TAG = "DrawableUtil";
    public static Bitmap createTextImage(int color) {
        //若使背景为透明，必须设置为Bitmap.Config.ARGB_4444
        Bitmap bm = Bitmap.createBitmap(150, 150, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        Paint paint = new Paint();
        paint.setColor(color);
        int mRadius = 75;
        canvas.drawCircle(mRadius, mRadius, mRadius, paint);
        return bm;
    }

    public static Drawable rotateDrawable(Drawable drawable ,float angle)
    {
        Bitmap bmpOriginal = ((BitmapDrawable) drawable).getBitmap();
        Bitmap bmResult = Bitmap.createBitmap(bmpOriginal.getWidth(), bmpOriginal.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas tempCanvas = new Canvas(bmResult);
        tempCanvas.rotate(angle, bmpOriginal.getWidth()/2, bmpOriginal.getHeight()/2);
        tempCanvas.drawBitmap(bmpOriginal, 0, 0, null);
        Drawable drawable1 = new BitmapDrawable(bmResult);
        return drawable1;
    }

    public static Drawable rotatDrawable(Drawable drawable, float angle){
//创建一个Matrix对象
        Matrix matrix = new Matrix();
//由darwable创建一个bitmap对象
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
//设置旋转角度
        matrix.setRotate(angle);
//以bitmap跟matrix一起创建一个新的旋转以后的bitmap
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
//bitmap转化为drawable对象
        return new BitmapDrawable(bitmap);
    }
}
