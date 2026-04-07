package com.example.wusthelper.ui.dialog;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.example.wusthelper.R;
import com.example.wusthelper.bean.javabean.ConfigBean;
import com.example.wusthelper.databinding.DialogUpdateBinding;
import com.example.wusthelper.helper.ConfigHelper;
import com.example.wusthelper.utils.ToastUtil;
import com.xuexiang.xupdate.XUpdate;
import com.xuexiang.xupdate._XUpdate;
import com.xuexiang.xupdate.service.OnFileDownloadListener;
import com.xuexiang.xutil.app.PathUtils;

import java.io.File;

public class UpdateDialog extends BaseDialogFragment<DialogUpdateBinding>
        implements View.OnClickListener {

    /**
     * 进度条
     */

    private static ConfigBean sRootBean;

    private static Context mContext;


    @Override
    public void onActivityCreated(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        //设置监听器，用于调用接口
        initListener();
        initView();
        super.onActivityCreated(savedInstanceState);
    }

//    public static UpdateDialog newInstance(@NonNull Context context,ConfigBean bean) {
//        mContext = context;
//        sRootBean = bean;
//        UpdateDialog dialog = new UpdateDialog();
//        return dialog;
//    }

    public void initView() {
        sRootBean = ConfigHelper.getConfigBean().getData();
        setCancelable(sRootBean.getIsUpdate().equals("1"));
        // 更新内容
        getBinding().tvUpdateDialogTitle.setText(getUpdateTitle());
//        ToastUtil.showShortToastCenter("测试");
        Log.d("TAG", "initView: " + sRootBean);
        getBinding().tvUpdateDialogContent.setText(sRootBean.getUpdateContent());
    }

    public void initListener() {
        getBinding().btnDialogCancel.setOnClickListener(this);
        getBinding().btnDialogOpenTheLink.setOnClickListener(this);
        getBinding().btnDialogUpdate.setOnClickListener(this);
    }

    //====================生命周期============================//

//    @Override
//    public void show() {
//        _XUpdate.setIsShowUpdatePrompter(true);
//        super.show();
//    }
//
//    @Override
//    public void dismiss() {
//        _XUpdate.setIsShowUpdatePrompter(false);
//        super.dismiss();
//    }


    //====================更新功能============================//

    @Override
    public void onClick(View view) {
        int i = view.getId();
        //点击版本升级按钮【下载apk】
        if (i == R.id.btn_dialog_update) {
            //权限判断是否有访问外部存储空间权限
            tryUpdate();
        } else if (i == R.id.btn_dialog_open_the_link) {
            //点击官网链接
            Uri uri = Uri.parse("https://wusthelper.wustlinghang.cn/android/wusthelper_android.json");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } else if (i == R.id.btn_dialog_cancel) {
            //点击忽略按钮
            dismiss();
        }
    }

    private void tryUpdate() {

        if(getActivity()==null){
            ToastUtil.showShortToastCenter("安装失败");
            setCancelable(true);
            return;
        }
        XUpdate.newBuild(getActivity())
                .apkCacheDir(PathUtils.getAppExtCachePath()) //设置下载缓存的根目录
                .build()
                .download(ConfigHelper.get_apkUrl(), new OnFileDownloadListener() {   //设置下载的地址和下载的监听
                    @Override
                    public void onStart() {
                        doStart();
//                            HProgressDialogUtils.showHorizontalProgressDialog(getContext(), "下载进度", false);
                    }

                    @Override
                    public void onProgress(float progress, long total) {
                        handleProgress(progress);
//                            HProgressDialogUtils.setProgress(Math.round(progress * 100));
                    }

                    @Override
                    public boolean onCompleted(File file) {
//                            HProgressDialogUtils.cancel();

                        _XUpdate.startInstallApk(getActivity(), file); //填写文件所在的路径
                        getBinding().btnDialogMakeInstalled.setVisibility(View.VISIBLE);

                        //显示立即安装的按钮
                        getBinding().btnDialogMakeInstalled.setOnClickListener(v -> {
                            _XUpdate.startInstallApk(getActivity(), file); //填写文件所在的路径
                        });

                        return false;
                    }

                    @Override
                    public void onError(Throwable throwable) {
//                            HProgressDialogUtils.cancel();
                        ToastUtil.showShortToastCenter("安装包下载失败!");
                        getBinding().llUpdateDialogContent.setVisibility(View.VISIBLE);
                        getBinding().npbProgress.setVisibility(View.GONE);
                    }
                });
    }


    private void doStart() {
        getBinding().npbProgress.setVisibility(View.VISIBLE);
        getBinding().npbProgress.setProgress(0);
        getBinding().llUpdateDialogContent.setVisibility(View.GONE);
    }


    public void handleProgress(float progress) {

        if (getBinding().npbProgress.getVisibility() == View.GONE) {
            doStart();
        }
        getBinding().npbProgress.setProgress(Math.round(progress * 100));
        getBinding().npbProgress.setMax(100);
    }

    private String getUpdateTitle() {
        String newVersion = sRootBean.getVersion();
        String contentTemplate = getString(R.string.update_dialog_title);
        @SuppressLint("StringFormatMatches") String content = String
                .format(contentTemplate, newVersion);
        return content;
    }

}
