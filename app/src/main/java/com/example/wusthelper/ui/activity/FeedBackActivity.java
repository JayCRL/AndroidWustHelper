package com.example.wusthelper.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.example.wusthelper.BuildConfig;
import com.example.wusthelper.R;
import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.databinding.ActivityFeedBackBinding;
import com.example.wusthelper.helper.SharePreferenceLab;

import java.io.File;
import java.util.List;
import java.util.UUID;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class FeedBackActivity extends BaseActivity<ActivityFeedBackBinding>
        implements EasyPermissions.PermissionCallbacks, View.OnClickListener {
    //    此反馈连接已不再使用
//    private static final String FEEDBACK_URL = "https://www.wjx.top/jq/29301837.aspx";
    //    此处为新连接
    public static final String FEEDBACK_URL = "https://support.qq.com/product/275699?d-wx-push=1";
    private static final String HEAD_IMG_URL = "https://oukarsblog.oss-cn-hangzhou.aliyuncs.com/wusthelper/img/android_avatar.png?x-oss-process=style/blog_img";
    private static final String TAG = "FeedBackActivity";

    private ValueCallback<Uri[]> mUploadMessageAboveL;
    private final static int REQUEST_CAMERA = 1;
    private final static int REQUEST_ALBUM = 2;
    private String mCurrentPhotoUri;
    private String mLastPhotoUri;
    private String Post_Data;

    public static Intent newInstance(Context context) {
        return new Intent(context, FeedBackActivity.class);
    }

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void initView() {
        initStatusBarColor();
        getBinding().ivBack.setOnClickListener(this);
        LoadUserData();

        getBinding().wvFeedback.loadUrl(FEEDBACK_URL);
        getBinding().wvFeedback.getSettings().setDomStorageEnabled(true);
        getBinding().wvFeedback.postUrl(FEEDBACK_URL, Post_Data.getBytes());
        getBinding().wvFeedback.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress >= 95)
                    getBinding().progressBar.setVisibility(View.GONE);
                else if (getBinding().progressBar.getVisibility() == View.GONE)
                    getBinding().progressBar.setVisibility(View.VISIBLE);
                getBinding().progressBar.setProgress(newProgress);
                super.onProgressChanged(view, newProgress);
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                mUploadMessageAboveL = filePathCallback;
                uploadPicture();
                return true;
            }
        });

        getBinding().wvFeedback.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                super.shouldOverrideUrlLoading(view, url);
                Log.d(TAG, "shouldOverrideUrlLoading: " + url);
                if (url == null) {
                    return false;
                }
                try {
                    if (url.startsWith("weixin://")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        view.getContext().startActivity(intent);
                        return true;
                    }
                } catch (Exception e) {
                    return false;
                }
                view.loadUrl(url);
                return true;
            }
        });

        getBinding().wvFeedback.getSettings().setJavaScriptEnabled(true);
        getBinding().wvFeedback.getSettings().setLoadsImagesAutomatically(true);
        getBinding().wvFeedback.getSettings().setUseWideViewPort(true);
        getBinding().wvFeedback.getSettings().setLoadWithOverviewMode(true);
        getBinding().wvFeedback.getSettings().setAllowFileAccess(true);
        getBinding().wvFeedback.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_BACK && getBinding().wvFeedback.canGoBack()) {
                    getBinding().wvFeedback.goBack();
                    return true;
                }
                return false;
            }
        });
    }

    private void initStatusBarColor() {
        getWindow().setStatusBarColor(getResources().getColor(R.color.white));
        changeStatusBarTextColor(true);
    }

    private void LoadUserData() {
        String id;
        StringBuilder name = new StringBuilder();
        id = SharePreferenceLab.getInstance().getStudentId(this);
        String displayName = SharePreferenceLab.getDisplayName();
        if (displayName != null && !displayName.isEmpty() && !"木有设置".equals(displayName)) {
            name.append(displayName.charAt(0));
        }
        name.append("同学");
            Post_Data = "nickname=" + name.toString() + "&avatar=" + HEAD_IMG_URL + "&openid=" + id;
    }

    @Override
    public void onBackPressed() {
        if (getBinding().wvFeedback.canGoBack()) {
            getBinding().wvFeedback.goBack();
        } else {
            finish();
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

        AlertDialog.Builder builder = new AlertDialog.Builder(FeedBackActivity.this);
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

                    if (EasyPermissions.hasPermissions(FeedBackActivity.this, perms)) {
                        takePhoto();
                    } else {
                        EasyPermissions.requestPermissions(FeedBackActivity.this, "请求相机权限", REQUEST_CAMERA, perms);
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

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File tempFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), UUID.randomUUID() + "_upload.png");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri uri = FileProvider.getUriForFile(FeedBackActivity.this, BuildConfig.APPLICATION_ID + ".fileProvider", tempFile);
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

        if (requestCode == REQUEST_CAMERA || requestCode == REQUEST_ALBUM) {
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
                mUploadMessageAboveL.onReceiveValue(new Uri[]{imageUri});
            } else {
                mUploadMessageAboveL.onReceiveValue(null);
            }
            mUploadMessageAboveL = null;
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
    public void onClick(View v) {
        if (v.equals(getBinding().ivBack)) {
            finish();
        }
    }
}
