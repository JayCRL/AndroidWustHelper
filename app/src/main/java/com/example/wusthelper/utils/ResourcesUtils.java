package com.example.wusthelper.utils;

import android.graphics.drawable.Drawable;

import com.example.wusthelper.MyApplication;

/**
 * @author: Gong Yunhao
 * @version: V1.0
 * @date: 2018/10/22
 * @github https://github.com/Roman-Gong
 * @blog https://www.jianshu.com/u/52a8fa1f29fb
 */
public class ResourcesUtils {
    public static String getRealString(int textID) {
        return MyApplication.getContext().getResources().getString(textID);
    }

    public static int getRealColor(int colorID) {
        return MyApplication.getContext().getResources().getColor(colorID);
    }

    public static Drawable getRealDrawable(int drawableID) {
        return MyApplication.getContext().getResources().getDrawable(drawableID);
    }

}
