package com.example.wusthelper.ui.activity;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;


import com.example.wusthelper.R;
import com.example.wusthelper.base.activity.BaseMvpActivity;
import com.example.wusthelper.databinding.ActivityCreditsStatisticsBinding;
import com.example.wusthelper.helper.MyDialogHelper;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.helper.SnackBarHelper;
import com.example.wusthelper.mvp.presenter.CreditsStatisticsPresenter;
import com.example.wusthelper.mvp.view.CreditsStatisticsView;

public class CreditsStatisticsActivity extends BaseMvpActivity<CreditsStatisticsView, CreditsStatisticsPresenter,
        ActivityCreditsStatisticsBinding> implements CreditsStatisticsView{

    private WebView mWebViewCredits;
    private WebView mWebViewScheme;
    private AlertDialog loadingView;
    private static final String TAG = "WebCreditsStatistics";

    public static Intent newInstance(Context context) {
        return new Intent(context, CreditsStatisticsActivity.class);
    }

    @Override
    public CreditsStatisticsPresenter createPresenter() {
        return new CreditsStatisticsPresenter();
    }

    @Override
    public CreditsStatisticsView createView() {
        return this;
    }

    @Override
    public void initView() {

        Log.e(TAG,"已进入");
        initStatusBarColor();
        setListener();
        initLoadingView();
        initWebView();

        getPresenter().initPresenterData();

    }



    private void setListener() {
        getBinding().ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void initWebView() {
        mWebViewCredits = new WebView(getApplicationContext());
        mWebViewCredits.setWebChromeClient(new WebChromeClient());
        mWebViewCredits.getSettings().setJavaScriptEnabled(true);
        getBinding().llWebCredits.addView(mWebViewCredits);
        mWebViewScheme = new WebView(getApplicationContext());
//        if(!SharePreferenceLab.getIsGraduate()) {
//            mWebViewScheme.setWebChromeClient(new WebChromeClient());
//            mWebViewScheme.getSettings().setJavaScriptEnabled(true);
//            getBinding().llWebScheme.addView(mWebViewScheme);
//        }
    }

    private void initLoadingView() {
        loadingView = MyDialogHelper.createLoadingDialog(this,"加载中...",false);
        loadingView.show();
    }

    private void initStatusBarColor() {
        getWindow().setStatusBarColor(getResources().getColor(R.color.white));
        changeStatusBarTextColor(true);
    }

    @Override
    protected void onDestroy() {
        mWebViewCredits.removeAllViews();
        mWebViewCredits.destroy();
        mWebViewScheme.removeAllViews();
        mWebViewScheme.destroy();
        super.onDestroy();
        if(loadingView != null){
            loadingView.dismiss();
        }
    }

    @Override
    public void showCreditsHtml(String htmlText) {
        mWebViewCredits.loadDataWithBaseURL(null,htmlText,"text/html","charset=UTF-8",null);
        mWebViewCredits.getSettings().setJavaScriptEnabled(true);
        mWebViewCredits.getSettings().setBuiltInZoomControls(true);
        mWebViewCredits.getSettings().setDisplayZoomControls(false);
        mWebViewCredits.getSettings().setUseWideViewPort(true);
        mWebViewCredits.getSettings().setLoadWithOverviewMode(true);
    }

    @Override
    public void showSchemeHtml(String htmlText) {
        mWebViewScheme.loadDataWithBaseURL(null,htmlText,"text/html","charset=UTF-8",null);
        mWebViewScheme.getSettings().setJavaScriptEnabled(true);
        mWebViewScheme.getSettings().setBuiltInZoomControls(true);
        mWebViewScheme.getSettings().setDisplayZoomControls(false);
        mWebViewScheme.getSettings().setUseWideViewPort(true);
        mWebViewScheme.getSettings().setLoadWithOverviewMode(true);
    }

    @Override
    public void showSnackBar(String msg) {
        SnackBarHelper.showColorSnackBar(getBinding().llWebCredits, msg,
                R.color.color_snack_bar_no_internet);
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void cancelDialog() {
        loadingView.cancel();
    }

}