package com.example.wusthelper.helper;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import com.example.wusthelper.R;
import com.example.wusthelper.databinding.ToastLoadingBinding;
import com.wang.avi.AVLoadingIndicatorView;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MyDialogHelper {

    /**
     * 创建一个普通的加载Dialog
     * */
    public static AlertDialog createLoadingDialog(Context context, String text, boolean cancelable) {
        View view = LayoutInflater.from(context).inflate(R.layout.toast_loading, null);
        AVLoadingIndicatorView avl = view.findViewById(R.id.avl);
        avl.show();
        TextView tv = view.findViewById(R.id.tv);
        tv.setText(text);
        AlertDialog dialog = new AlertDialog.Builder(context, R.style.LoadingDialog)
                .setView(view)
                .setCancelable(cancelable)
                .create();
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
            }
        });
        return dialog;
    }

    /**
     * @param activity
     * @param text
     * @param cancelable
     * 创建登录界面的加载Dialog
     * */
    public static AlertDialog createLoginLoadingDialog(Activity activity, String text, boolean cancelable) {
        ToastLoadingBinding toastLoadingBinding = ToastLoadingBinding.inflate(activity.getLayoutInflater());
        toastLoadingBinding.avl.show();
        toastLoadingBinding.tv.setText(text);
        return new AlertDialog.Builder(activity, R.style.LoadingDialog)
                .setView(toastLoadingBinding.getRoot())
                .setCancelable(cancelable)
                .create();
    }

    /**
     * 创建加载课表界面的Dialog
     * */
    public static SweetAlertDialog getCommonDialog(Context context, int dialogType, String contentText,
                                                   String confirmText) {
        return getSweetAlertDialog(context,dialogType,null,contentText,confirmText,null,null);
    }

    public static SweetAlertDialog getCommonDialog(Context context, int dialogType, String title,
                                                   String contentText, String confirmText) {
        return getSweetAlertDialog(context,dialogType,title,contentText,confirmText,null,null);
    }

    public static SweetAlertDialog getCommonDialog(Context context, int dialogType, String title,
                                                   String confirmText, SweetAlertDialog.OnSweetClickListener listener) {
        return getSweetAlertDialog(context,dialogType,title,null,confirmText,null,listener);
    }

    public static SweetAlertDialog getCommonDialog(Context context, int dialogType, String title,
                                                   String contentText, String confirmText,
                                                   SweetAlertDialog.OnSweetClickListener listener) {
        return getSweetAlertDialog(context,dialogType,title,contentText,confirmText,null,listener);
    }

    public static SweetAlertDialog getCommonDialog(Context context, int dialogType, String title,
                                                   String contentText, String confirmText,String cancelText,
                                                   SweetAlertDialog.OnSweetClickListener listener) {
        return getSweetAlertDialog(context,dialogType,title,contentText,confirmText,cancelText,listener);
    }


    /**
     * 功能还没有全部加满，可以自己添加
     * */
    public static SweetAlertDialog getSweetAlertDialog(Context context, int dialogType, String title,
                                                   String contentText, String confirmText,String cancelText,
                                                   SweetAlertDialog.OnSweetClickListener listener) {
        SweetAlertDialog dialog = new SweetAlertDialog(context, dialogType);
        if(title!=null)
            dialog.setTitleText(title);
        if(contentText!=null)
            dialog.setContentText(contentText);
        if(confirmText!=null)
            dialog.setConfirmText(confirmText);
        if(cancelText!=null)
            dialog.setCancelText(cancelText);
        if(listener!=null)
            dialog.setConfirmClickListener(listener);
        return dialog;
    }


}
