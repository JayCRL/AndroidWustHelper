package com.example.wusthelper.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.RequiresApi;

import com.example.wusthelper.R;
import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.databinding.ActivityOtherwebBinding;
import com.example.wusthelper.helper.MyDialogHelper;
import com.example.wusthelper.utils.NetWorkUtils;
import com.example.wusthelper.utils.ToastUtil;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class OtherWebActivity extends BaseActivity<ActivityOtherwebBinding> {

    private static final String TAG = "OtherWebMvpActivity";

    private static String url ;
    private static String name;

    //用于显示的Dialog
    private SweetAlertDialog dialog;

    public static Intent getInstance(Context context){
        return new Intent(context, OtherWebActivity.class);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void initView() {

        getWindow().setStatusBarColor(getResources().getColor(R.color.white));
        changeStatusBarTextColor(true);
        isNetWorkConnect();

        getBinding().ivOtherwebBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        getBinding().ivOtherwebMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmDialog();
            }
        });


        getBinding().wvOtherweb.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if(newProgress==100){
                    getBinding().progressOtherwb.setVisibility(View.GONE);
                }else{
                    getBinding().progressOtherwb.setVisibility(View.VISIBLE);
                    getBinding().progressOtherwb.setProgress(newProgress);
                }
            }
        });

        getBinding().wvOtherweb.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
                // 处理下载事件
                Uri uri = Uri.parse(url); // url为你要链接的地址
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        getBinding().tvOtherwebTitle.setText(name);
    }

    private void showConfirmDialog() {
        dialog = MyDialogHelper.getCommonDialog(this, SweetAlertDialog.BUTTON_CONFIRM,
                "提醒：使用浏览器打开", "","确认",null, sweetAlertDialog -> {
                    dialog.cancel();
                    try{
                        Uri uri = Uri.parse(url);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }catch (Exception e){
                        ToastUtil.showShortToastCenter("打开浏览器失败");
                    }
                });
        dialog.show();
    }


    private void isNetWorkConnect() {
        if (!NetWorkUtils.isConnected(this)) {
            getBinding().flOtherwebNoContent.setVisibility(View.VISIBLE);
            getBinding().wvOtherweb.setVisibility(View.GONE);
        }else{
            startWebView();
        }
    }

    private void startWebView() {
        getBinding().wvOtherweb.getSettings().setJavaScriptEnabled(true);
        getBinding().wvOtherweb.getSettings().setUseWideViewPort(true);
        getBinding().wvOtherweb.getSettings().setLoadWithOverviewMode(true);
        getBinding().wvOtherweb.loadUrl(url);
        getBinding().wvOtherweb.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK &&  getBinding().wvOtherweb.canGoBack()) {
            // 返回上一页面
            getBinding().wvOtherweb.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public static void setUrl(String Url){
        url = Url;
    }
    public static void setName(String Name){
        name = Name ;
    }

}