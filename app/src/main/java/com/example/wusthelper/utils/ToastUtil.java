package com.example.wusthelper.utils;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.example.wusthelper.R;
import com.example.wusthelper.MyApplication;

public class ToastUtil {
    private static final String TAG = "ToastUtil";
    private static final int  NORMAL = 0 ;
    private static final int  CENTER = 1 ;
    private static int mGravity = 0 ;
    private static Toast    toast;
    private static TextView sTextView;

    public static void deleteToast(){
        toast = null ;
    }
    private static void createToast(){
        View toastRoot = LayoutInflater.from(MyApplication.getContext()).inflate(R.layout.my_toast, null);
        sTextView = (TextView) toastRoot.findViewById(R.id.tv_util_show);
        toast = new Toast(MyApplication.getContext());
        toast.setView(toastRoot);
    }
    /**
     * 显示一个Toast
     * 需要在主线程里面显示*/
    public static void show(String text) {
        try{
            if (toast == null || mGravity != NORMAL) {
                mGravity = NORMAL ;
                createToast();
            }
            sTextView.setText(text);
            toast.show();
        }catch (Exception e){
            Log.d(TAG, "show: "+e.getMessage());
            Log.e(TAG, "show: ToastUtil调用出错，可能是因为在子线程调用了ToastUtil.show()");
        }

    }
    /**
     * 短时间显示Toast【居中】
     * @param msg 显示的内容-字符串*/
    public static void showShortToastCenter(String msg){
        try{
            if(MyApplication.getContext() != null) {
                if (toast == null || mGravity != CENTER ) {
                    mGravity = CENTER ;
                    createToast();
                }
                toast.setGravity(Gravity.CENTER, 0, 0);
                sTextView.setText(msg);
                toast.show();
            }
        }catch (Exception e){
            Log.e(TAG, "show: ToastUtil调用出错，可能是因为在子线程调用了ToastUtil.show()");
        }
    }
    /**
     * 长时间显示Toast【居下】
     * @param msg 显示的内容-字符串*/
    public static void showLongToast(String msg) {
        try{
            if(MyApplication.getContext() != null) {
                if (toast == null || mGravity != NORMAL ) {
                    mGravity = NORMAL ;
                    createToast();
                }
                sTextView.setText(msg);
                toast.show();
            }
        }catch (Exception e){
            Log.e(TAG, "show: ToastUtil调用出错，可能是因为在子线程调用了ToastUtil.show()");
        }
    }
    /**
     * 长时间显示Toast【居中】
     * @param msg 显示的内容-字符串*/
    public static void showLongToastCenter(String msg){
        try{
            if(MyApplication.getContext() != null) {
                if (toast == null || mGravity != CENTER) {
                    mGravity = CENTER ;
                    createToast();
                }
                toast.setGravity(Gravity.CENTER, 0, 0);
                sTextView.setText(msg);
                toast.show();
            }
        }catch (Exception e){
            Log.e(TAG, "show: ToastUtil调用出错，可能是因为在子线程调用了ToastUtil.show()");
        }
    }
}
