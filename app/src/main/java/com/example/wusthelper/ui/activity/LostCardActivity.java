package com.example.wusthelper.ui.activity;

import static com.example.wusthelper.MyApplication.getContext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.wusthelper.BuildConfig;
import com.example.wusthelper.MyApplication;
import com.example.wusthelper.R;
import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.databinding.ActivityLostCardBinding;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.utils.NetWorkUtils;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class LostCardActivity extends BaseActivity<ActivityLostCardBinding>
        implements EasyPermissions.PermissionCallbacks{
    private int param = 1;
    private static final String TAG = "LostCardActivity";

    private static String url ;
    /**
     * 不同方式的请求码
     */

    private final static int REQUEST_CAMERA = 1;
    private final static int REQUEST_ALBUM = 2;

    /**
     * 接收安卓5.0以上的
     */
    private ValueCallback<Uri[]> mUploadMessageAboveL;

    private String mCurrentPhotoUri;
    private String mLastPhotoUri;

    private AlertDialog loadingView;

    public static Intent newInstance(Context context){
        return new Intent(context, LostCardActivity.class);
    }

    @Override
    public void initView() {

//        getBinding().ivBack.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (getBinding().lostCardWebView.canGoBack()) {
//                    // 返回上一页面
//                    getBinding().lostCardWebView.goBack();
//                }else{
//                    finish();
//                }
//            }
//        });
        getWindow().setStatusBarColor(Color.parseColor("#f7f7f7"));
        Log.d(TAG, "initView: ");
        try {
            isNetWorkConnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");

    }

    private void isNetWorkConnect() throws IOException {
        if (!NetWorkUtils.isConnected(this)) {
            getBinding().flEmptyclassroomNoContent.setVisibility(View.VISIBLE);
            getBinding().lostCardWebView.setVisibility(View.GONE);
        }else{
            startWebView();
        }
    }

    private void startWebView() throws IOException {

        String cacheDirPath = LostCardActivity.this.getFilesDir().getAbsolutePath()+"cache/";
        getBinding().lostCardWebView.getSettings().setAppCachePath(cacheDirPath);
        // 1. 设置缓存路径

        getBinding().lostCardWebView.getSettings().setAppCacheMaxSize(20*1024*1024);
        // 2. 设置缓存大小

        getBinding().lostCardWebView.getSettings().setAppCacheEnabled(true);
        // 3. 开启Application Cache存储机制


        getBinding().lostCardWebView.getSettings().setUseWideViewPort(true);
        getBinding().lostCardWebView.getSettings().setLoadWithOverviewMode(true);
        getBinding().lostCardWebView.getSettings().setDomStorageEnabled(true);
        getBinding().lostCardWebView.getSettings().setDefaultTextEncodingName("UTF-8");
        getBinding().lostCardWebView.getSettings().setAllowContentAccess(true); // 是否可访问Content Provider的资源，默认值 true
        getBinding().lostCardWebView.getSettings().setAllowFileAccess(true);    // 是否可访问本地文件，默认值 true
        // 是否允许通过file url加载的Javascript读取本地文件，默认值 false
        getBinding().lostCardWebView.getSettings().setAllowFileAccessFromFileURLs(false);
        // 是否允许通过file url加载的Javascript读取全部资源(包括文件,http,https)，默认值 false
        getBinding().lostCardWebView.getSettings().setAllowUniversalAccessFromFileURLs(false);
        //开启JavaScript支持
        getBinding().lostCardWebView.getSettings().setJavaScriptEnabled(true);
        // 支持缩放
        getBinding().lostCardWebView.getSettings().setSupportZoom(true);

        getBinding().lostCardWebView.clearCache(true);
        getBinding().lostCardWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        getBinding().lostCardWebView.getSettings().setAllowFileAccess(true);
        getBinding().lostCardWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
        getBinding().lostCardWebView.getSettings().setDomStorageEnabled(true);
        getBinding().lostCardWebView.getSettings().setAllowFileAccess(true);
        //如果访问的页面中有Javascript，则webview必须设置支持Javascript
        getBinding().lostCardWebView.getSettings().setAllowFileAccess(true);
        getBinding().lostCardWebView.getSettings().setAppCacheEnabled(true);
        getBinding().lostCardWebView.getSettings().setDomStorageEnabled(true);
        getBinding().lostCardWebView.getSettings().setDatabaseEnabled(true);
        getBinding().lostCardWebView.getSettings().setUseWideViewPort(true);
        getBinding().lostCardWebView.getSettings().setLoadWithOverviewMode(true);

        Log.d(TAG, "startWebView: ");
        syncCookie(LostCardActivity.this, url);
        getBinding().lostCardWebView.loadUrl(url);
        getBinding().lostCardWebView.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                //openFileChooseImpleForAndroid(filePathCallback);
                mUploadMessageAboveL = filePathCallback;
                uploadPicture();
                return true;
            }

        });


        showLoadView();

        getBinding().lostCardWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();    //表示等待证书响应
                // handler.cancel();      //表示挂起连接，为默认方式
                // handler.handleMessage(null);    //可做其他处理
            }

            public void onPageFinished(WebView view, String url) {
                Log.i(TAG, "Finished loading URL: " +url);
                if(loadingView!=null)
                    loadingView.cancel();
            }

        });


        // 特别注意：5.1以上默认禁止了https和http混用，以下方式是开启
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getBinding().lostCardWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

    }

    public void syncCookie(Context context, String url){
        try{
            CookieSyncManager.createInstance(context);
            CookieManager cookieManager = CookieManager.getInstance();
            Log.e(TAG, "syncCookie: "+cookieManager );
            cookieManager.setAcceptCookie(true);
            cookieManager.removeSessionCookie();// 移除
//            cookieManager.removeAllCookie();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cookieManager.setAcceptThirdPartyCookies(getBinding().lostCardWebView, true);//TODO 跨域cookie读取
            }
            URL aURL = new URL(url);
            StringBuilder sbCookie = new StringBuilder();
            sbCookie.append(String.format("token" + "=%s", SharePreferenceLab.getInstance().getToken(getContext())));
            //webview在使用cookie前会前判断保存cookie的domain和当前要请求的domain是否相同，相同才会发送cookie
            sbCookie.append(String.format(";domain=wustlinghang.cn")); //注意，是getHost()，不是getAuthority(),
            sbCookie.append(String.format(";path=%s","/"));

            String cookieValue = sbCookie.toString();
            cookieManager.setCookie(url, cookieValue);

            Log.e(TAG, "syncCookie: "+ cookieManager.getCookie(url));
            CookieSyncManager.getInstance().sync();

            String newCookie = cookieManager.getCookie(url);
        }catch(Exception e){
        }
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            takePhoto();
        }
    };


    private void uploadPicture() {

        AlertDialog.Builder builder = new AlertDialog.Builder(LostCardActivity.this);
        builder.setTitle("请选择图片上传的方式");

        // 这里需要注意的点是一定要返回null
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if (mUploadMessageAboveL != null) {
                    mUploadMessageAboveL.onReceiveValue(null);
                    mUploadMessageAboveL = null;
                }
            }
        });

        builder.setPositiveButton("相机", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (mCurrentPhotoUri != null && !mLastPhotoUri.isEmpty()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            File file = new File(mLastPhotoUri);
                            if (file != null) {
                                file.delete();
                            }
                            mHandler.sendEmptyMessage(1);

                        }
                    }).start();
                } else {

                    String[] perms = new String[]{Manifest.permission.CAMERA};

                    if (EasyPermissions.hasPermissions(LostCardActivity.this, perms)) {
                        takePhoto();
                    } else {
                        EasyPermissions.requestPermissions(LostCardActivity.this, "请求相机权限", REQUEST_CAMERA, perms);
                    }

                }

            }
        });

        builder.setNegativeButton("相册", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                chooseAlbum();
            }
        });

        builder.create().show();
    }

    private void takePhoto() {

        StringBuilder fileName = new StringBuilder();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileName.append(UUID.randomUUID()).append("_upload.png");
        File tempFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName.toString());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri uri = FileProvider.getUriForFile(LostCardActivity.this, BuildConfig.APPLICATION_ID + ".fileProvider", tempFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        } else {
            Uri uri = Uri.fromFile(tempFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }

        mCurrentPhotoUri = tempFile.getAbsolutePath();
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void chooseAlbum() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, ""), REQUEST_ALBUM);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CAMERA||requestCode == REQUEST_ALBUM) {
            if (mUploadMessageAboveL == null)
                return;
            if (resultCode == RESULT_OK) {

                Uri imageUri = null;
                switch (requestCode) {
                    case REQUEST_CAMERA:
                        if (mCurrentPhotoUri != null && !mCurrentPhotoUri.isEmpty()) {
                            File file = new File(mCurrentPhotoUri);
                            Uri localUri = Uri.fromFile(file);
                            Intent localIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri);
                            sendBroadcast(localIntent);
                            imageUri = Uri.fromFile(file);
                            mLastPhotoUri = mCurrentPhotoUri;
                        }
                        break;
                    case REQUEST_ALBUM:
                        if (data != null) {
                            imageUri = data.getData();
                        }
                        break;
                    default:
                        break;
                }

                if (mUploadMessageAboveL != null) {
                    mUploadMessageAboveL.onReceiveValue(new Uri[]{imageUri});
                    mUploadMessageAboveL = null;

                }

            } else {
                if (mUploadMessageAboveL != null) {
                    mUploadMessageAboveL.onReceiveValue(null);
                    mUploadMessageAboveL = null;
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == REQUEST_CAMERA) {
            takePhoto();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && getBinding().lostCardWebView.canGoBack()) {
            // 返回上一页面
            getBinding().lostCardWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode,event);
//        else{
//            startActivity(MainMvpActivity.newInstance(getContext()));
//            LostCardActivity.this.finish();
//            return false;
//        }
    }


    public static void setUrl(String Url){
        url = Url;
    }

    private void showLoadView() {
        try{
            loadingView = loadingDialog("加载中...", false);
            loadingView.show();

            Thread t = new Thread(new Runnable() {

                @Override

                public void run() {

                    try {

                        Thread.sleep(5000);//让他显示10秒后，取消ProgressDialog

                    } catch (InterruptedException e) {

// TODO Auto-generated catch block

                        e.printStackTrace();

                    }

                    if(loadingView != null){
                        loadingView.dismiss();
                    }

                }

            });

            t.start();
        }catch (Exception e){
            Log.d(TAG, "startWebView: "+e.toString());
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
}