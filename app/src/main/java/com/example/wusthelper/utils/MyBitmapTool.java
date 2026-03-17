package com.example.wusthelper.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import com.example.wusthelper.helper.SharePreferenceLab;
import com.glidebitmappool.GlideBitmapPool;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MyBitmapTool {
//这个工具类基本上是桌面小插件背景绑定的，特化的,还有课表背景图片的回调识别

    private static final String TAG = "WidgetProvider";
    //从一个图片文件的相对路径获取其对应的Bitmap//经过压缩，给小组件用
    public static Bitmap getRoundedCornerBitmap_from_PathFromString(Context context, String Path,int alpha){
        try{
            Path = MyBitmapTool.getRealPathFromString(context,Path);
            if(Path==null||Path.equals("")){
                return null;
            }
            Bitmap bitmap = BitmapFactory.decodeFile(Path);

            Bitmap frontBitmap = Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(),Bitmap.Config.ARGB_8888); // 建立画布
            frontBitmap.eraseColor(Color.parseColor("#"+String.format("%02X",alpha)+"FFFFFF")); // 填充颜色
            frontBitmap = MyBitmapTool.getRoundedCornerBitmap(frontBitmap);

            Bitmap targetBitmap = MyBitmapTool.mergeBitmap(bitmap,frontBitmap);
            if(targetBitmap == null){
                return bitmap;
            }else{
                return getRoundedCornerBitmap(targetBitmap);

            }
        }catch (Exception e){
            Log.e(TAG, "getRoundedCornerBitmap_from_PathFromString: "+e.getMessage());
            return null;
        }

    }


    public static Bitmap getRoundedCornerBitmap_from_PathFromString(Context context, String Path,int alpha, int w, int h){

        Log.d(TAG, "getRoundedCornerBitmap_from_PathFromString: "+w);
        Log.d(TAG, "getRoundedCornerBitmap_from_PathFromString: "+h);

        try{
            Path = MyBitmapTool.getRealPathFromString(context,Path);
            if(Path==null||Path.equals("")){
                return null;
            }
            Bitmap bitmap = BitmapFactory.decodeFile(Path);

            Bitmap frontBitmap = Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(),Bitmap.Config.ARGB_8888); // 建立画布
            frontBitmap.eraseColor(Color.parseColor("#"+String.format("%02X",alpha)+"FFFFFF")); // 填充颜色
            frontBitmap = MyBitmapTool.getRoundedCornerBitmap(frontBitmap);

            Bitmap targetBitmap = MyBitmapTool.mergeBitmap(bitmap,frontBitmap);
            if(targetBitmap == null){
                return bitmap;
            }else{
                return getRoundedCornerBitmap(ImageCrop(targetBitmap,w,h,true));

            }

            //return getRoundedCornerBitmap(bitmap);
        }catch (Exception e){
            Log.e(TAG, "getRoundedCornerBitmap_from_PathFromString: "+e.getMessage());
            return null;
        }

    }



    public static Bitmap getRoundedCornerBitmap_from_PathFromString_before_press(Context context, String Path){
        Path = MyBitmapTool.getRealPathFromString(context,Path);

        Log.d(TAG, "getRoundedCornerBitmap_from_PathFromString_before_press: "+Path);
        if(Path==null){
            Path=" ";
        }
        Bitmap bitmap = BitmapFactory.decodeFile(Path);

        Log.d(TAG, "getRoundedCornerBitmap_from_PathFromString_before_press: "+(bitmap==null));

        return getRoundedCornerBitmap(bitmap);
    }


    /**
     * 圆角图片
     * @param bitmap 位图
     * @param rx x方向上的圆角半径
     * @param ry y方向上的圆角半径
     * @param bl 是否需要描边
     * @param bl 画笔粗细
     * @param bl 颜色代码
     * @return bitmap
     */
    public static Bitmap createCornerBitmap(Bitmap bitmap,int rx,int ry,boolean bl,int edge,int color) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);//创建画布

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        RectF rectF = new RectF(rect);
        canvas.drawRoundRect(rectF, rx, ry, paint);//绘制圆角矩形
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));//取相交裁剪
        canvas.drawBitmap(bitmap, rect, rect, paint);
        if(bl) {
            if (color == 0) color = 0xFFFEA248;//默认橘黄色
            paint.setColor(color);
            paint.setColor(color);
            paint.setStyle(Paint.Style.STROKE);//描边
            paint.setStrokeWidth(edge);
            canvas.drawRoundRect(rectF, rx, ry, paint);
        }
        return output;
    }

    /**
     * 自定义裁剪，根据第一个像素点(左上角)X和Y轴坐标和需要的宽高来裁剪
     * @param srcBitmap
     * @param firstPixelX
     * @param firstPixelY
     * @param needWidth
     * @param needHeight
     * @param recycleSrc
     * @return
     */
    public static Bitmap cropBitmapCustom(Bitmap srcBitmap, int firstPixelX, int firstPixelY, int needWidth, int needHeight, boolean recycleSrc) {

        Log.d("danxx", "cropBitmapRight before w : "+srcBitmap.getWidth());
        Log.d("danxx", "cropBitmapRight before h : "+srcBitmap.getHeight());

        if(firstPixelX + needWidth > srcBitmap.getWidth()){
            needWidth = srcBitmap.getWidth() - firstPixelX;
        }

        if(firstPixelY + needHeight > srcBitmap.getHeight()){
            needHeight = srcBitmap.getHeight() - firstPixelY;
        }

        /**裁剪关键步骤*/
        Bitmap cropBitmap = Bitmap.createBitmap(srcBitmap, firstPixelX, firstPixelY, needWidth, needHeight);

        Log.d("danxx", "cropBitmapRight after w : "+cropBitmap.getWidth());
        Log.d("danxx", "cropBitmapRight after h : "+cropBitmap.getHeight());


        /**回收之前的Bitmap*/
        if (recycleSrc && srcBitmap != null && !srcBitmap.equals(cropBitmap) && !srcBitmap.isRecycled()) {
            GlideBitmapPool.putBitmap(srcBitmap);
        }

        return cropBitmap;
    }

    /**
     * 把两个位图覆盖合成为一个位图，以底层位图的长宽为基准
     * @param backBitmap 在底部的位图
     * @param frontBitmap 盖在上面的位图
     * @return
     */
    public static Bitmap mergeBitmap(Bitmap backBitmap, Bitmap frontBitmap) {

        if (backBitmap == null || backBitmap.isRecycled()
                || frontBitmap == null || frontBitmap.isRecycled()) {
            Log.e(TAG, "backBitmap=" + backBitmap + ";frontBitmap=" + frontBitmap);
            return null;
        }
        Bitmap bitmap = backBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);
        Rect baseRect  = new Rect(0, 0, backBitmap.getWidth(), backBitmap.getHeight());
        Rect frontRect = new Rect(0, 0, frontBitmap.getWidth(), frontBitmap.getHeight());
        canvas.drawBitmap(frontBitmap, frontRect, baseRect, null);
        return bitmap;
    }


        //获取圆角的bitmap
    public static Bitmap getRoundedCornerBitmap(Bitmap sourceBitmap){

        Log.d(TAG, "getRoundedCornerBitmap: getWidth"+sourceBitmap.getWidth());
        Log.d(TAG, "getRoundedCornerBitmap: getHeight"+sourceBitmap.getHeight());

        try {

            Bitmap targetBitmap = Bitmap.createBitmap(sourceBitmap.getWidth(), sourceBitmap.getHeight(), Bitmap.Config.ARGB_8888);

            // 得到画布
            Canvas canvas = new Canvas(targetBitmap);

            // 创建画笔
            Paint paint = new Paint();
            paint.setAntiAlias(true);

            // 值越大角度越明显
            float roundPx = 50;
            float roundPy = 50;

            Rect rect = new Rect(0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight());
            RectF rectF = new RectF(rect);

            // 绘制
            canvas.drawARGB(0, 0, 0, 0);
            canvas.drawRoundRect(rectF, roundPx, roundPy, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(sourceBitmap, rect, rect, paint);

            return targetBitmap;

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "getRoundedCornerBitmap: "+e.getMessage());
        }

        return null;
    }


    //从相对路径的字符串获取绝对路径的字符串
    public static String getRealPathFromString(Context context, String Path){
        try{
            Uri uri = Uri.parse(Path);
            if(uri!=null){
                return getRealPathFromURI(context,uri);
            }
            return "";
        } catch (Exception e){
            return "";
        }

    }

    public static String getRealPathFromURI(Context context, Uri contentUri){
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

        finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static Bitmap compressBitmap(Bitmap bitmap, long sizeLimit) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int quality = 100;
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);

//        // 循环判断压缩后图片是否超过限制大小
//        while(baos.toByteArray().length / 1024 > sizeLimit) {
//            // 清空baos
//            baos.reset();
//            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
//            quality -= 10;
//        }

        Bitmap newBitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(baos.toByteArray()), null, null);

        return newBitmap;
    }



    public static Boolean compare_Bitmap(Bitmap image,int maxSize){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        if ( baos.toByteArray().length / 1024>maxSize) { //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            return false;
        }
        return true;
    }

    public static Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while ( baos.toByteArray().length / 1024>100) { //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    public static Bitmap comp(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        if( baos.toByteArray().length / 1024>3072) {//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, 50, baos);//这里压缩50%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 800f;//这里设置高度为800f
        float ww = 480f;//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        return compressImage(bitmap);//压缩好比例大小后再进行质量压缩
    }



    public static String bitmapToBase64(Bitmap bitmap) {

        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static boolean isGiFFile_from_path(String path){
        File file = new File(path);
        return isGifFile(file);
    }
    private static boolean isGifFile(File file) {
        try {
            FileInputStream inputStream = new FileInputStream(file);
            int[] flags = new int[5];
            flags[0] = inputStream.read();
            flags[1] = inputStream.read();
            flags[2] = inputStream.read();
            flags[3] = inputStream.read();
            inputStream.skip(inputStream.available() - 1);
            flags[4] = inputStream.read();
            inputStream.close();
            return flags[0] == 71 && flags[1] == 73 && flags[2] == 70 && flags[3] == 56 && flags[4] == 0x3B;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 图片资源文件转bitmap
     * @param resId 图片的绝对路径
     * @return bitmap
     */
    public static Bitmap getBitmapResources(Context context,int resId){
        return BitmapFactory.decodeResource(context.getResources(),resId);
    }


    /**
     * 按照一定的宽高比例裁剪图片
     *
     * @param bitmap
     * @param num1
     *            长边的比例
     * @param num2
     *            短边的比例
     * @return
     */
    public static Bitmap ImageCrop(Bitmap bitmap, int num1, int num2,
                                   boolean isRecycled)
    {
        if (bitmap == null)
        {
            return null;
        }
        int w = bitmap.getWidth(); // 得到图片的宽，高
        int h = bitmap.getHeight();
        int retX, retY;
        int nw, nh;
        if (w > h)
        {
            Log.d(TAG, "ImageCrop: W>H");
            if (h > w * num2 / num1)
            {
                nw = w;
                nh = w * num2 / num1;
                retX = 0;
                retY = (h - nh) / 2;
            } else
            {
                nw = h * num1 / num2;
                nh = h;
                retX = (w - nw) / 2;
                retY = 0;
            }
        } else
        {
            Log.d(TAG, "ImageCrop: W<=H");
            if (h > w * num2 / num1)
            {
                nw = w;
                nh = w * num2 / num1;
                retX = 0;
                retY = (h - nh) / 2;
            } else
            {
                nw = h * num1 / num2;
                nh = h;
                retX = (w - nw) / 2;
                retY = 0;
            }
        }

        Log.d(TAG, "ImageCrop: retX = "+retX);
        Log.d(TAG, "ImageCrop: retY = "+retY);

        Log.d(TAG, "ImageCrop: w"+w);
        Log.d(TAG, "ImageCrop: h"+h);
        Log.d(TAG, "ImageCrop: nw = "+nw);
        Log.d(TAG, "ImageCrop: nh = "+nh);

        Bitmap bmp = Bitmap.createBitmap(bitmap, retX, retY, nw, nh, null,
                false);
        if (isRecycled && bitmap != null && !bitmap.equals(bmp)
                && !bitmap.isRecycled())
        {
            bitmap.recycle();
            bitmap = null;
        }
        return bmp;// Bitmap.createBitmap(bitmap, retX, retY, nw, nh, null,
        // false);
    }

}

