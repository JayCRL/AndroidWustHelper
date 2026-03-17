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
import android.webkit.JavascriptInterface;
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
import com.example.wusthelper.databinding.FragmentVolunteerBinding;
import com.example.wusthelper.helper.MyDialogHelper;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.request.WustApi;
import com.example.wusthelper.utils.NetWorkUtils;

import org.json.JSONObject;

public class VolunteerFragment extends BaseBindingFragment<FragmentVolunteerBinding> {

    private static final String TAG = "VolunteerFragment";

    // iOS(NewWustHelper_副本4) 直接加载该地址
    private static final String WEB_VIEW_URL = WustApi.VOLUNTEER_URL;

    // MainActivity 隐藏了状态栏，这里记录高度后手动占位
    private int height;

    private AlertDialog loadingView;

    public static VolunteerFragment newInstance() {
        return new VolunteerFragment();
    }

    @Override
    public void initView() {
        Log.i(TAG, "initView");
        initStatusBar();
    }

    @Override
    protected void lazyLoad() {
        Log.i(TAG, "lazyLoad connected=" + NetWorkUtils.isConnected(getContext()));
        if (NetWorkUtils.isConnected(getContext())) {
            showLoadView();
            startWebView();
        } else {
            getBinding().flNoContent.setVisibility(View.VISIBLE);
            getBinding().wvVolunteer.setVisibility(View.GONE);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void startWebView() {
        Log.i(TAG, "startWebView url=" + WEB_VIEW_URL);

        WebView webView = getBinding().wvVolunteer;

        // 打开 WebView 远程调试（chrome://inspect）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                WebView.setWebContentsDebuggingEnabled(true);
            } catch (Exception ignore) {
            }
        }

        // JS bridge，用于确认页面是否真的执行了 JS
        try {
            webView.addJavascriptInterface(new Object() {
                @JavascriptInterface
                public void log(String msg) {
                    Log.i(TAG, "[js-bridge] " + msg);
                }
            }, "AndroidBridge");
        } catch (Exception e) {
            Log.e(TAG, "addJavascriptInterface failed", e);
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

        // 某些站点会依赖这两个
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            settings.setMediaPlaybackRequiresUserGesture(false);
        }

        // 针对现代移动端 H5 (如 Vant) 的优化
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        // **关键**：志愿者站点首页里引用了 http://res.wx.qq.com/...
        // WebView 在 HTTPS 页面里加载 HTTP 子资源经常会被拦截，改为拦截并自动升级到 https。

        // 统一对齐 iOS NewWustHelper_副本4 的移动端 UA
        settings.setUserAgentString(
                "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148"
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
        // 仅实现 iOS 里用到的跳转处理（alert 里带链接时外部打开）
        getBinding().wvVolunteer.setWebChromeClient(new WebChromeClient() {
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
                cookieManager.setAcceptThirdPartyCookies(getBinding().wvVolunteer, true);
            }
        } catch (Exception ignore) {
        }

        getBinding().wvVolunteer.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.i(TAG, "onPageStarted url=" + url);
                // 验证 JS 是否能执行
                try {
                    view.evaluateJavascript("(function(){try{AndroidBridge.log('pageStarted ' + location.href);}catch(e){} return document.title;})()", null);
                } catch (Exception ignore) {
                }
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
            public android.webkit.WebResourceResponse shouldInterceptRequest(WebView view, android.webkit.WebResourceRequest request) {
                try {
                    if (request != null && request.getUrl() != null) {
                        String reqUrl = request.getUrl().toString();
                        if (reqUrl.startsWith("http://")) {
                            String upgrade = "https://" + reqUrl.substring("http://".length());
                            Log.w(TAG, "intercept upgrade http->https: " + reqUrl + " => " + upgrade);
                            try {
                                java.net.URL url = new java.net.URL(upgrade);
                                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                                conn.setConnectTimeout(8000);
                                conn.setReadTimeout(8000);
                                conn.setInstanceFollowRedirects(true);
                                conn.setRequestProperty("User-Agent", view.getSettings().getUserAgentString());
                                conn.connect();
                                String contentType = conn.getContentType();
                                String mimeType = null;
                                String encoding = "UTF-8";
                                if (contentType != null) {
                                    String[] parts = contentType.split(";");
                                    if (parts.length > 0) {
                                        mimeType = parts[0].trim();
                                    }
                                    for (String p : parts) {
                                        p = p.trim().toLowerCase();
                                        if (p.startsWith("charset=")) {
                                            encoding = p.substring("charset=".length());
                                        }
                                    }
                                }
                                java.io.InputStream is = conn.getInputStream();
                                return new android.webkit.WebResourceResponse(mimeType, encoding, is);
                            } catch (Exception e) {
                                Log.e(TAG, "upgrade fetch failed: " + upgrade, e);
                            }
                        }
                    }
                } catch (Exception ignore) {
                }
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                Log.e(TAG, "onReceivedError: code=" + errorCode + " desc=" + description + " url=" + failingUrl);
                if (loadingView != null) {
                    loadingView.cancel();
                }
                getBinding().flNoContent.setVisibility(View.VISIBLE);
                getBinding().wvVolunteer.setVisibility(View.GONE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.i(TAG, "onPageFinished url=" + url + " title=" + (view != null ? view.getTitle() : "null"));

                // 验证 JS 是否能执行
                try {
                    view.evaluateJavascript("(function(){try{AndroidBridge.log('pageFinished ' + location.href);}catch(e){} return document.title;})()", value -> Log.i(TAG, "evaluate title=" + value));
                } catch (Exception ignore) {
                }

                if (loadingView != null) {
                    loadingView.cancel();
                }

                // 完全对齐 iOS(NewWustHelper_副本4)：加载完成后注入 JS 自动填充学号/密码并点击确认
                injectAutoLoginIfNeeded(view);
            }
        });
    }

    private void injectAutoLoginIfNeeded(WebView webView) {

        String username = SharePreferenceLab.getStudentId();
        String password = SharePreferenceLab.getPassword();
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return;
        }

        // 完全对齐 iOS(NewWustHelper_副本4)：轮询次数、debug log、事件触发、contenteditable 同步、键盘事件模拟。
        // 用 JSONObject.quote 做字符串转义，避免引号/反斜杠导致 JS 语法错误
        String jsCode = "(function(){\n" +
                "let checkCount = 0;\n" +
                "const maxCheck = 15;\n" +
                "const interval = setInterval(() => {\n" +
                "  checkCount++;\n" +
                "  console.log('第' + checkCount + '次检测');\n" +
                "  const usernameInput = document.querySelector(\"input[type=\\\"text\\\"][placeholder=\\\"请输入学号\\\"].van-field__control\");\n" +
                "  const passwordInput = document.querySelector(\"input[type=\\\"password\\\"][placeholder=\\\"请输入密码\\\"].van-field__control\");\n" +
                "  const confirmBtn = document.querySelector('.van-dialog__confirm');\n" +
                "  console.log('学号框:', usernameInput ? '找到' : '未找到');\n" +
                "  console.log('密码框:', passwordInput ? '找到' : '未找到');\n" +
                "  console.log('确认按钮:', confirmBtn ? '找到' : '未找到');\n" +
                "  if (usernameInput && passwordInput) {\n" +
                "    clearInterval(interval);\n" +
                "    console.log('✅ 找到所有元素，开始填充');\n" +
                "    usernameInput.value = " + JSONObject.quote(username) + ";\n" +
                "    triggerEvents(usernameInput);\n" +
                "    passwordInput.value = " + JSONObject.quote(password) + ";\n" +
                "    triggerEvents(passwordInput);\n" +
                "    setTimeout(() => {\n" +
                "      if (confirmBtn) {\n" +
                "        confirmBtn.click();\n" +
                "        console.log('✅ 已点击确认按钮');\n" +
                "      } else {\n" +
                "        console.log('❌ 未找到确认按钮，尝试提交表单');\n" +
                "        submitForm(usernameInput);\n" +
                "      }\n" +
                "    }, 1000);\n" +
                "  }\n" +
                "  if (checkCount >= maxCheck) {\n" +
                "    clearInterval(interval);\n" +
                "    console.log('❌ 超时未找到元素');\n" +
                "  }\n" +
                "}, 500);\n" +
                "\n" +
                "function triggerEvents(element) {\n" +
                "  if (!element) {\n" +
                "    console.log('❌ 元素不存在，跳过事件触发');\n" +
                "    return;\n" +
                "  }\n" +
                "  const events = ['focus', 'input', 'change', 'blur'];\n" +
                "  events.forEach(evt => {\n" +
                "    element.dispatchEvent(new Event(evt, { bubbles: true }));\n" +
                "  });\n" +
                "  let editableEl = element.nextElementSibling;\n" +
                "  if (!editableEl || !editableEl.isContentEditable) {\n" +
                "    try {\n" +
                "      editableEl = element.parentNode && element.parentNode.querySelector ? element.parentNode.querySelector('[contenteditable=\\\"plaintext-only\\\"]') : null;\n" +
                "    } catch (e) {\n" +
                "      editableEl = null;\n" +
                "    }\n" +
                "  }\n" +
                "  if (editableEl && editableEl.isContentEditable) {\n" +
                "    editableEl.textContent = element.value;\n" +
                "    editableEl.dispatchEvent(new Event('input', { bubbles: true }));\n" +
                "    editableEl.dispatchEvent(new Event('change', { bubbles: true }));\n" +
                "    console.log('✅ 已同步contenteditable元素内容');\n" +
                "  } else {\n" +
                "    console.log('ℹ️ 未找到关联的contenteditable元素，跳过同步');\n" +
                "  }\n" +
                "  element.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter' }));\n" +
                "  element.dispatchEvent(new KeyboardEvent('keyup', { key: 'Enter' }));\n" +
                "}\n" +
                "\n" +
                "function submitForm(inputElement) {\n" +
                "  if (!inputElement) return;\n" +
                "  const form = inputElement.closest && inputElement.closest('form');\n" +
                "  if (form) {\n" +
                "    form.submit();\n" +
                "    console.log('✅ 已提交表单');\n" +
                "  } else {\n" +
                "    console.log('❌ 未找到表单元素');\n" +
                "  }\n" +
                "}\n" +
                "})();";

        webView.evaluateJavascript(jsCode, value -> {
            // iOS 代码只在 console 打印；安卓这里不额外处理回调
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
        if (getBinding().wvVolunteer.canGoBack()) {
            getBinding().wvVolunteer.goBack();
            return true;
        }
        return false;
    }

    private void showLoadView() {
        try {
            Log.i(TAG, "showLoadView");
            loadingView = MyDialogHelper.createLoadingDialog(getActivity(), "加载中...", false);
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
