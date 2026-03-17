/*
 * @author yuandalai
 * @date 2018/11/17
 * @email yuanlai0611@gmail.com
 * @github https://github.com/yuanlai0611
 * @blog https://yuanlai0611.github.io/
 */

package com.example.wusthelper.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.wusthelper.R;
import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.databinding.ActivityScanBinding;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.utils.Base64Util;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.filter.Filter;
import com.zhihu.matisse.internal.entity.CaptureStrategy;
import com.zhihu.matisse.internal.entity.IncapableCause;
import com.zhihu.matisse.internal.entity.Item;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.QRCodeDecoder;
import cn.bingoogolapple.qrcode.zxing.ZXingView;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class ScanActivity extends BaseActivity<ActivityScanBinding> implements QRCodeView.Delegate, EasyPermissions.PermissionCallbacks, View.OnClickListener {

    private static final String TAG = "ScanActivity";
    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_ALBUM = 2;
    public static final String STUDENT_NAME = "studentName";
    public static final String STUDENT_ID = "studentId";
    public static final String TOKEN = "token";
    public static final String SEMESTER = "semester";

    private int kind;
    public static final int COUNTDOWN = 9;
    public static final int COURSE = 10;

    public static Intent newInstance(Context context) {
        return new Intent(context, ScanActivity.class);
    }

    @Override
    public void initView() {
        View view = getWindow().getDecorView();
        int options = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        view.setSystemUiVisibility(options);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        kind = getIntent().getIntExtra("kind",COURSE);
        setListener();
        if (ContextCompat.checkSelfPermission(ScanActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            EasyPermissions.requestPermissions(this, "请求相机的权限", REQUEST_CAMERA, Manifest.permission.CAMERA);
        } else {
            getBinding().zvScan.setDelegate(this);
        }
    }


    private void setListener() {
        getBinding().ivScanOpenAlbum.setOnClickListener(this);
        getBinding().ivScanBack.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getBinding().zvScan.startSpotAndShowRect();
    }

    @Override
    protected void onStop() {
        getBinding().zvScan.stopCamera();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        getBinding().zvScan.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onScanQRCodeSuccess(String result) {

//        String content = Base64Util.decode(result);
        String content=result.substring(3,10)+result.substring(13);
        content=new String( Base64.decode(content, Base64.DEFAULT));
        Log.d(TAG, "onScanQRCodeSuccess: "+content);

        if (content != null) {
            if(kind  == COURSE){
                if (content.startsWith("kjbk")){
                    String[] strs = content.split("\\?\\+/");
                    if (strs.length == 5) {
                        String studentName = strs[1];
                        String studentId = strs[2];
                        String token = strs[3];
                        String semester = strs[4];

                        String myStudentId = SharePreferenceLab.getInstance().getStudentId(this);
                        if (myStudentId.equals(studentId)) {

                            Toast.makeText(this, "干嘛添加自己的课程表？", Toast.LENGTH_SHORT).show();

                        } else {
                            Intent intent = new Intent();
                            intent.putExtra(STUDENT_NAME, studentName);
                            intent.putExtra(STUDENT_ID, studentId);
                            intent.putExtra(TOKEN, token);
                            intent.putExtra(SEMESTER, semester);
                            setResult(RESULT_OK, intent);
                        }
                    } else {
                        Toast.makeText(this, "这不是合理的二维码", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                } else {
                    Toast.makeText(this, "这不是合理的二维码", Toast.LENGTH_SHORT).show();
                }
            }else if(kind == COUNTDOWN){
                Intent intent = new Intent();
                intent.putExtra("onlyId",content);
                Log.e(TAG, "onScanQRCodeSuccess: "+content);
                setResult(RESULT_OK,intent);
                finish();
            }
            //成功后设置震动
            vibrate();

        } else {

            Toast.makeText(this, "这不是合理的二维码", Toast.LENGTH_SHORT).show();

        }

//        Log.d(TAG, content);

    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        Toast.makeText(ScanActivity.this, "摄像头调用失败", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

        if (requestCode == REQUEST_CAMERA) {
            getBinding().zvScan.setDelegate(this);
            getBinding().zvScan.startSpotAndShowRect();
        } else if (requestCode == REQUEST_ALBUM){
            openAlbum();
        }

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

        if (requestCode == REQUEST_CAMERA) {
            Toast.makeText(ScanActivity.this, "拒绝了相机权限", Toast.LENGTH_SHORT).show();
        } else if (requestCode == REQUEST_ALBUM) {
            Toast.makeText(ScanActivity.this, "拒绝了相册权限", Toast.LENGTH_SHORT).show();
        }

        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }

    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(100);
        }

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.iv_scan_back:
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.iv_scan_open_album:
                if (ContextCompat.checkSelfPermission(ScanActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    EasyPermissions.requestPermissions(this, "请求相册的权限", REQUEST_ALBUM, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
                break;
            default:
                break;
        }

    }

    private void openAlbum() {

        Matisse.from(ScanActivity.this)
                .choose(MimeType.ofAll())//图片类型
                .countable(false)//true:选中后显示数字;false:选中后显示对号
                .maxSelectable(1)//可选的最大数
                .capture(false)//选择照片时，是否显示拍照
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                //参数1 true表示拍照存储在共有目录，false表示存储在私有目录；参数2与 AndroidManifest中authorities值相同，用于适配7.0系统 必须设置
                .imageEngine(new GlideEngine())//图片加载引擎
                .forResult(REQUEST_ALBUM);//

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getBinding().zvScan.startSpotAndShowRect(); // 显示扫描框，并且延迟0.5秒后开始识别

        if (resultCode == RESULT_OK && requestCode == REQUEST_ALBUM) {
//            String path = Matisse.obtainPathResult(data).get(0);
            List<Uri> backgroundUris = Matisse.obtainResult(data);
            Uri uri = backgroundUris.get(0);
            String[] filePathColumn = {MediaStore.Audio.Media.DATA};
            Cursor cursor = getContentResolver().query(getFileUri(uri), null, null, null, null);
            assert cursor != null;
            cursor.moveToFirst();
            //获取到的图片路径
            String photoPath = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
            cursor.close();
            Log.e(TAG, "onActivityResult: photoPath =="+photoPath );
            Log.v(TAG,QRCodeDecoder.syncDecodeQRCode(photoPath));
            String result = QRCodeDecoder.syncDecodeQRCode(photoPath);
            Log.e(TAG, "onActivityResult: result =="+result );
            if (result == null) {
                Toast.makeText(this, "请确认后再次扫描", Toast.LENGTH_SHORT).show();
            } else {
                onScanQRCodeSuccess(result);
            }

        }

    }

    private Uri getFileUri(Uri uri) {

        try {
            if (uri.getScheme().equals("file")) {
                String path = uri.getEncodedPath();
                if (path != null) {
                    path = Uri.decode(path);
                    ContentResolver cr = this.getContentResolver();
                    StringBuffer buff = new StringBuffer();
                    buff.append("(")
                            .append(MediaStore.Images.ImageColumns.DATA)
                            .append("=")
                            .append("'")
                            .append(path)
                            .append("'")
                            .append(")");
                    Cursor cur = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Images.ImageColumns._ID}, buff.toString(), null, null);
                    int index = 0;
                    for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                        index = cur.getColumnIndex(MediaStore.Images.ImageColumns._ID);
                        index = cur.getInt(index);
                    }
                    if (index == 0) {

                    } else {
                        Uri uri_temp = Uri.parse("content://media/external/images/media/" + index);
                        if (uri_temp != null) {
                            uri = uri_temp;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return uri;
    }

}
