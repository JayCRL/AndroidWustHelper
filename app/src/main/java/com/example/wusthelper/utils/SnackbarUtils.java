package com.example.wusthelper.utils;

import android.view.View;

import com.example.wusthelper.MyApplication;
import com.google.android.material.snackbar.Snackbar;

/**
 * @author: Gong Yunhao
 * @version: V1.0
 * @date: 2018/10/20
 * @github https://github.com/Roman-Gong
 * @blog https://www.jianshu.com/u/52a8fa1f29fb
 */
public class SnackbarUtils {

    public static void showDefaultSnackBar(View view, int textID) {
        showDefaultSnackBar(view, MyApplication.getContext().getResources().getString(textID));
    }

    public static void showColorSnackBar(View view, int textID, int color) {
        showColorSnackBar(view, MyApplication.getContext().getResources().getString(textID), color);
    }

    public static void showColorSnackBar(View view, String text, int color) {
        Snackbar sSnackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG );
        View snackView = sSnackbar.getView( );
        snackView.setBackgroundColor( MyApplication.getContext( ).getResources( ).getColor(color) );
        sSnackbar.show();
    }

    public static void showDefaultSnackBar(View view, String text) {
        Snackbar sSnackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG );
        sSnackbar.show();
    }
}
