/*
 * @author yuandalai
 * @date 2018/11/17
 * @email yuanlai0611@gmail.com
 * @github https://github.com/yuanlai0611
 * @blog https://yuanlai0611.github.io/
 */

package com.example.wusthelper.helper;

import static com.xuexiang.xutil.XUtil.getContentResolver;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ImgTool {

    public static void saveImageToGallery(Context context, Bitmap bitmap) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
//            //获取要保存的图片的位图
//            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image1);
                    //创建一个保存的Uri
                    Uri saveUri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
                    if (TextUtils.isEmpty(saveUri.toString())) {
                        Looper.prepare();
                        Toast.makeText(context, "保存失败！", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                        return;
                    }
                    OutputStream outputStream = getContentResolver().openOutputStream(saveUri);
                    //将位图写出到指定的位置
                    //第一个参数：格式JPEG 是可以压缩的一个格式 PNG 是一个无损的格式
                    //第二个参数：保留原图像90%的品质，压缩10% 这里压缩的是存储大小
                    //第三个参数：具体的输出流
                    if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
                        Looper.prepare();
                        Toast.makeText(context, "保存成功！", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    } else {
                        Looper.prepare();
                        Toast.makeText(context, "保存失败！", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

//    public static Uri saveImageToGallery(Context context, Bitmap bmp) {
//        // 首先保存图片
//        String storePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "wust_campus";
//        File appDir = new File(storePath);
//        if (!appDir.exists()) {
//            appDir.mkdir();
//        }
//
//        String fileName = "share_schedule.jpg";
//        File file = new File(appDir, fileName);
//
//        if (file.exists()) {
//            file.delete();
//        }
//        try {
//            FileOutputStream fos = new FileOutputStream(file);
//            //通过io流的方式来压缩保存图片
//            boolean isSuccess = bmp.compress( Bitmap.CompressFormat.JPEG, 80, fos);
//            fos.flush();
//            fos.close();
//
//            //把文件插入到系统图库
//            //MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);
//
//            //保存图片后发送广播通知更新数据库
//            Uri uri = Uri.fromFile(file);
//            context.sendBroadcast(new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
//            if (isSuccess) {
//                return uri;
//            } else {
//                return null;
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
}
