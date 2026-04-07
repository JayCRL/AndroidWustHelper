package com.example.wusthelper.ui.fragment.mainviewpager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.example.wusthelper.base.fragment.BaseBindingFragment;
import com.example.wusthelper.databinding.FragmentConsultBinding;
import com.example.wusthelper.helper.MyDialogHelper;
import com.example.wusthelper.request.WustApi;
import com.example.wusthelper.utils.NetWorkUtils;

public class ConsultFragment extends BaseBindingFragment<FragmentConsultBinding> {

    private static final String TAG = "ConsultFragment";

    // iOS(NewWustHelper_副本4) 的 NewsView.swift 直接加载该地址
    private static final String WEB_VIEW_URL = WustApi.CONSULT_URL;

    // MainActivity 隐藏了状态栏，这里记录高度后手动占位
    private int height;

    private AlertDialog loadingView;

    public static ConsultFragment newInstance() {
        return new ConsultFragment();
    }

    @Override
    public void initView() {
        initStatusBar();
    }

    @Override
    protected void lazyLoad() {
        if (NetWorkUtils.isConnected(getContext())) {
            showLoadView();
            startWebView();
        } else {
            getBinding().flNoContent.setVisibility(View.VISIBLE);
            getBinding().wvConsult.setVisibility(View.GONE);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void startWebView() {
        Log.i(TAG, "startWebView url=" + WEB_VIEW_URL);

        WebView webView = getBinding().wvConsult;

        // 打开 WebView 远程调试（chrome://inspect）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                WebView.setWebContentsDebuggingEnabled(true);
            } catch (Exception ignore) {
            }
        }

        setWebClient();

        WebSettings settings = webView.getSettings();

        Log.i(TAG, "UA(before)=" + settings.getUserAgentString());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        // 修正路径：确保 cache 目录名正确且启用必要存储
        String cacheDirPath = requireContext().getCacheDir().getAbsolutePath() + "/web_cache";
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAppCacheEnabled(true);
        settings.setAppCachePath(cacheDirPath);

        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        // 针对现代移动端 H5 的优化
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        // 对齐 iOS NewWustHelper_副本4 CustomWebView 的移动端 UA
        settings.setUserAgentString(
                "Mozilla/5.0 (iPhone; CPU iPhone OS 13_6_1 like Mac OS X) AppleWebKit/537.36 (KHTML, like Gecko) Version/13.0 Mobile/15E148 Safari/537.36"
        );

        Log.i(TAG, "UA(after)=" + settings.getUserAgentString());

        // 打印当前 cookies（如果有）
        try {
            String cookie = CookieManager.getInstance().getCookie(WEB_VIEW_URL);
            Log.i(TAG, "cookie(beforeLoad)=" + cookie);
        } catch (Exception ignore) {
        }

        try {
            Log.i(TAG, "loadUrl begin");
            webView.loadUrl(WEB_VIEW_URL);
            Log.i(TAG, "loadUrl end");
        } catch (Exception e) {
            Log.e(TAG, "loadUrl exception", e);
            if (loadingView != null) {
                loadingView.cancel();
            }
            getBinding().flNoContent.setVisibility(View.VISIBLE);
            webView.setVisibility(View.GONE);
        }
    }

    private void setWebClient() {
        // iOS 里只做了 navigation 打印；安卓这里保留原逻辑：alert 带链接时外部打开
        getBinding().wvConsult.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                if (consoleMessage != null) {
                    Log.d(TAG, "[console] " + consoleMessage.message() + " (" + consoleMessage.sourceId() + ":" + consoleMessage.lineNumber() + ")");
                }
                return super.onConsoleMessage(consoleMessage);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                Log.d(TAG, "progress=" + newProgress + " url=" + (view != null ? view.getUrl() : "null"));
                super.onProgressChanged(view, newProgress);
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                Log.d(TAG, "onJsAlert: " + message);
                try {
                    Uri uri = Uri.parse(message);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                } catch (Exception ignore) {
                    // message 不是合法 URL 时，直接确认，避免 JS alert 被拦截后卡死导致白屏
                } finally {
                    try {
                        result.confirm();
                    } catch (Exception ignore) {
                    }
                }
                return true;
            }
        });

        // 允许 cookie（部分 H5 依赖 localStorage/cookie 做鉴权/路由）
        try {
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cookieManager.setAcceptThirdPartyCookies(getBinding().wvConsult, true);
            }
        } catch (Exception ignore) {
        }

        getBinding().wvConsult.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.i(TAG, "onPageStarted url=" + url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d(TAG, "shouldOverrideUrlLoading url=" + url);
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                Log.e(TAG, "onReceivedSslError url=" + (view != null ? view.getUrl() : "null") + " error=" + error);
                handler.proceed();
            }

            @Override
            public void onReceivedHttpError(WebView view, android.webkit.WebResourceRequest request, android.webkit.WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
                try {
                    Log.e(TAG, "onReceivedHttpError code=" + (errorResponse != null ? errorResponse.getStatusCode() : -1)
                            + " url=" + (request != null ? request.getUrl() : "null"));
                } catch (Exception ignore) {
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                Log.e(TAG, "onReceivedError: code=" + errorCode + " desc=" + description + " url=" + failingUrl);
                if (loadingView != null) {
                    loadingView.cancel();
                }
                getBinding().flNoContent.setVisibility(View.VISIBLE);
                getBinding().wvConsult.setVisibility(View.GONE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.i(TAG, "onPageFinished url=" + url + " title=" + (view != null ? view.getTitle() : "null"));

                // 对齐 iOS：打印当前 cookie（iOS CustomWebView 会把 cookieStore 里 cookie 打印出来）
                try {
                    String cookieStr = CookieManager.getInstance().getCookie(url);
                    Log.d(TAG, "Cookies for " + url + ": " + cookieStr);
                } catch (Exception ignore) {
                }

                if (loadingView != null) {
                    loadingView.cancel();
                }
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            height = savedInstanceState.getInt("statusBarHeight");
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("statusBarHeight", height);
    }

    public void setHeight(int statusBarHeight) {
        this.height = statusBarHeight;
    }

    public void initStatusBar() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        getBinding().viewStatus.setLayoutParams(lp);
        getBinding().viewStatus.setBackgroundColor(Color.TRANSPARENT);
    }

    public boolean onKeyDownBack() {
        if (getBinding().wvConsult.canGoBack()) {
            getBinding().wvConsult.goBack();
            return true;
        }
        return false;
    }

    private void showLoadView() {
        try {
            Log.i(TAG, "showLoadView");
            loadingView = MyDialogHelper.createLoadingDialog(getContext(), "加载中...", false);
            loadingView.show();

            Thread t = new Thread(() -> {

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (loadingView != null) {
                    Log.w(TAG, "loading dialog timeout dismiss (5s)");
                    loadingView.dismiss();
                }

            });

            t.start();
        } catch (Exception e) {
            Log.e(TAG, "showLoadView exception", e);
        }

    }
}
