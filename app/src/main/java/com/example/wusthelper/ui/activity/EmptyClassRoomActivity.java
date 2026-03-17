package com.example.wusthelper.ui.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.databinding.ActivityEmptyClassRoomBinding;
import com.example.wusthelper.helper.TimeTools;
import com.example.wusthelper.request.WustApi;
import com.example.wusthelper.utils.NetWorkUtils;

public class EmptyClassRoomActivity extends BaseActivity<ActivityEmptyClassRoomBinding> {
    
    private int thisweek;

    public static Intent newInstance(Context context){
        return new Intent(context, EmptyClassRoomActivity.class);
    }

    @Override
    public void initView() {
        getWindow().setStatusBarColor(Color.WHITE);
        thisweek = TimeTools.getCurrentWeek();
        setListener();
        initWebView();
        isNetWorkConnect();
    }
    
    private void setListener() {
        getBinding().ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getBinding().wvEmptyclassroom.canGoBack())
                    getBinding().wvEmptyclassroom.goBack();
                else{
                    finish();
                }
            }
        });
    }

    private void initWebView() {
        getBinding().wvEmptyclassroom.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if(newProgress==100){
                    getBinding().emptyClassroomProgress.setVisibility(View.GONE);
                }else{
                    getBinding().emptyClassroomProgress.setVisibility(View.VISIBLE);
                    getBinding().emptyClassroomProgress.setProgress(newProgress);
                }
            }
        });
    }

    private void isNetWorkConnect() {
        if (!NetWorkUtils.isConnected(this)) {
            getBinding().flEmptyclassroomNoContent.setVisibility(View.VISIBLE);
            getBinding().wvEmptyclassroom.setVisibility(View.GONE);
        }else{
            startWebView();
        }
    }

    private void startWebView() {
        getBinding().wvEmptyclassroom.getSettings().setJavaScriptEnabled(true);
        getBinding().wvEmptyclassroom.getSettings().setUseWideViewPort(true);
        getBinding().wvEmptyclassroom.getSettings().setLoadWithOverviewMode(true);
        String url = String.format("%s?weekNum=%s", WustApi.EMPTYCLASSROOM_URL, thisweek);
        getBinding().wvEmptyclassroom.loadUrl(url);
        getBinding().wvEmptyclassroom.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && getBinding().wvEmptyclassroom.canGoBack()) {
            // 返回上一页面
            getBinding().wvEmptyclassroom.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}