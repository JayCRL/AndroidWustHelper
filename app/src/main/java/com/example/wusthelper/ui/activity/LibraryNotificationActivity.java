package com.example.wusthelper.ui.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.wusthelper.R;
import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.bean.javabean.data.AnnouncementContentData;
import com.example.wusthelper.databinding.ActivityLibraryNotificationBinding;
import com.example.wusthelper.request.NewApiHelper;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;
import com.example.wusthelper.utils.NetWorkUtils;
import com.example.wusthelper.utils.SnackbarUtils;
import com.example.wusthelper.utils.ThreadPoolManager;
import com.example.wusthelper.utils.ToastUtil;
import com.wang.avi.AVLoadingIndicatorView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

public class LibraryNotificationActivity extends BaseActivity<ActivityLibraryNotificationBinding> {

    private WebView webView;
    private WebView mWebView;
    private LinearLayout mLinearLayout;
    private AlertDialog loadingView;
    private Toolbar newsDetailToolbar;
    private static final String NEWS_ID = "newsid";
    private static final String TAG = "NotificationActivity";
    private String new_id;

    public static Intent newInstance(Context context, String new_id) {
        Intent intent = new Intent(context, LibraryNotificationActivity.class);
        intent.putExtra(NEWS_ID, new_id);
        return intent;
    }

    private void getCreditStatistics(String id) {
//         TODO//模仿学分统计界面
        ThreadPoolManager.getInstance().addExecuteTask(new Runnable() {
            @Override
            public void run() {
                try {
                    Document document = Jsoup.connect(id)
                            .ignoreContentType(true)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.131 Safari/537.36 SLBrowser/8.0.0.3161 SLBChan/30")
                            .get();
                    if(!document.getElementsByAttributeValue("width","90%").equals(document.getElementsByAttributeValue("width","10%"))) {
                        Elements elements = document.getElementsByAttributeValue("width","90%");
                        elements.first().select("img").attr("src","http://www.lib.wust.edu.cn"+elements.first().select("img").attr("src").substring(2));
                    }else {
                        try {
                            Elements elements = document.getElementsByAttributeValue("style","clear: both; font-size: 13px; line-height: 28px; padding-right: 70px;");
                            elements.first().select("p").first().select("img").attr("src","http://www.lib.wust.edu.cn"+elements.first().select("img").attr("src").substring(2));
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Elements elementsHtml = document.getElementsByAttributeValue("style","padding: 20px 0 10px 0; height:100%;");
                    String html = elementsHtml.toString();
                    Log.d(TAG,"html" + html);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mWebView.loadDataWithBaseURL(null,html, "text/html","UTF-8", null);
                            mWebView.getSettings().setJavaScriptEnabled(true);
                            mWebView.getSettings().setBuiltInZoomControls(true);
                            mWebView.getSettings().setDisplayZoomControls(false);
                            mWebView.getSettings().setUseWideViewPort(true);
                            mWebView.getSettings().setLoadWithOverviewMode(true);
                            mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
                            mWebView.getSettings().setAllowFileAccess(true);
//                            mWebView.setWebViewClient(new WebViewClient(){
//                                @Override
//                                public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                                    view.loadUrl(url);
//                                    return true;
//                                }
//                            });
//                            mWebView.getSettings().setUseWideViewPort(true);
//                            mWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING);
//                            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
//                                mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
//                            mWebView.getSettings().setBlockNetworkImage(false);
                            loadingView.cancel();
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();

                }

            }
        });
    }

    private void getAnnouncement(String id) {
        NewApiHelper.getLibraryAnnouncementDetail(new_id, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                AnnouncementContentData announcementContentData = (AnnouncementContentData) responseObj;
                if(announcementContentData.getCode().equals("10000")){
                    String html = announcementContentData.getData();
                    mWebView.loadDataWithBaseURL(null,html, "text/html","UTF-8", null);
                    mWebView.getSettings().setJavaScriptEnabled(true);
                    mWebView.getSettings().setBuiltInZoomControls(true);
                    mWebView.getSettings().setDisplayZoomControls(false);
                    mWebView.getSettings().setUseWideViewPort(true);
                    mWebView.getSettings().setLoadWithOverviewMode(true);
                    mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
                    mWebView.getSettings().setAllowFileAccess(true);

                }else {
                    ToastUtil.show(announcementContentData.getMsg());
                }
                loadingView.cancel();
            }

            @Override
            public void onFailure(Object reasonObj) {
                loadingView.cancel();
                ToastUtil.show("请求失败，可能是网络未链接或请求超时");
            }
        });
    }

    private void getNewAnnouncement(String id){
        if(id.substring(0,4).equals("http")){
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.getSettings().setUseWideViewPort(true);
            mWebView.getSettings().setLoadWithOverviewMode(true);
            mWebView.loadUrl(id);
            mWebView.setWebViewClient(new WebViewClient(){
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.loadUrl(url);
                    return true;
                }
            });
        }else {
            mWebView.loadDataWithBaseURL(null,id, "text/html","UTF-8", null);
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.getSettings().setBuiltInZoomControls(true);
            mWebView.getSettings().setDisplayZoomControls(false);
            mWebView.getSettings().setUseWideViewPort(true);
            mWebView.getSettings().setLoadWithOverviewMode(true);
            mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            mWebView.getSettings().setAllowFileAccess(true);
        }

        loadingView.cancel();
    }

    @Override
    public void initView() {
        new_id = getIntent().getStringExtra(NEWS_ID);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        mLinearLayout = findViewById(R.id.ll_web_libraryNotification);
        newsDetailToolbar = (Toolbar)findViewById(R.id.tb_news_detail);
        newsDetailToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        loadingView = loadingDialog("加载中...", false);
        loadingView.show();
        mWebView = new WebView(getApplicationContext());
        mWebView.setWebChromeClient(new WebChromeClient());
//        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient());
        mLinearLayout.addView(mWebView);
        if (NetWorkUtils.isConnected(LibraryNotificationActivity.this)) {
            getAnnouncement(new_id);
        } else {
            loadingView.cancel();
            SnackbarUtils.showColorSnackBar(mLinearLayout, "网络未连接,获取失败！", R.color.color_snack_bar_no_internet);
        }
    }


    private AlertDialog loadingDialog(String text, boolean cancelable) {
        View view = LayoutInflater.from(this).inflate(R.layout.toast_loading, null);
        AVLoadingIndicatorView avl = view.findViewById(R.id.avl);
        avl.show();
        TextView tv = view.findViewById(R.id.tv);
        tv.setText(text);
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.LoadingDialog)
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK &&  mWebView.canGoBack()) {
            // 返回上一页面
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        mWebView.removeAllViews();
        mWebView.destroy();
        super.onDestroy();
    }
}