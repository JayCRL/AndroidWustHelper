package com.example.wusthelper.ui.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.wusthelper.MyApplication;
import com.example.wusthelper.R;
import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.databinding.ActivityQrcodeShareBinding;
import com.example.wusthelper.helper.ImgTool;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.utils.Base64Util;
import com.example.wusthelper.utils.CourseShareCodec;
import com.example.wusthelper.utils.ScreenUtils;
import com.example.wusthelper.utils.ToastUtil;

import java.io.File;
import java.util.List;

import cn.bingoogolapple.qrcode.zxing.QRCodeEncoder;
import de.hdodenhof.circleimageview.CircleImageView;
import pub.devrel.easypermissions.EasyPermissions;

public class QRCodeShareActivity extends BaseActivity<ActivityQrcodeShareBinding> implements
        View.OnClickListener, EasyPermissions.PermissionCallbacks {

    private final static int REQUEST_SHARE = 1;
    private final static int REQUEST_SAVE = 2;
    private Bitmap savedBitmap;
    private static final String KEY = "WUST_HELPER";
    private static final String TAG = "QRCodeShareActivity";
    private String studentId;
    private String headPath;
    private String studentName;
    private String semester;
    private String password;

    public static Intent newInstance(Context context) {
        return new Intent(context, QRCodeShareActivity.class);
    }

    @Override
    public void initView() {
        getWindow().setStatusBarColor(getResources().getColor(R.color.color_love_background));
        setListener();
        init();
        initData();
    }

    private void initData() {
        String content = CourseShareCodec.encodeV2("android", semester, studentName, studentId, null);
        generateLogoBitmap(content);
        savedBitmap = changeBitmapSize(savedBitmap);
        Glide.with(this).load(savedBitmap).into(getBinding().ivQr);
    }

    public Bitmap changeBitmapSize(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = ScreenUtils.dp2px(320);
        int newHeight = ScreenUtils.dp2px(320);
//        int newWidth = ScreenUtils.dp2px(240);
//        int newHeight = ScreenUtils.dp2px(240);
        float scaleWidth = ((float)newWidth)/width;
        float scaleHeight = ((float)newHeight)/height;
        Matrix matrix = new Matrix();
        matrix.setScale(scaleWidth, scaleHeight);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return bitmap;
    }

    private void setListener() {
        getBinding().rlShare.setOnClickListener(this);
        getBinding().llSaveToLocal.setOnClickListener(this);
        getBinding().llBack.setOnClickListener(this);
    }

    public void generateLogoBitmap(String content) {
        Bitmap logoBitmap = BitmapFactory.decodeResource(QRCodeShareActivity.this.getResources(), R.mipmap.logo);
        savedBitmap = QRCodeEncoder.syncEncodeQRCode(content, ScreenUtils.dp2px(400), Color.parseColor("#000000"), logoBitmap);
    }

    private void shareImg(String dlgTitle, String subject, String content) {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/wust_campus/share_schedule.jpg");
        Uri pictureUri = FileProvider.getUriForFile(QRCodeShareActivity.this, "com.linghang.wusthelper.fileProvider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(pictureUri, "image/*");
        intent.putExtra(Intent.EXTRA_STREAM, pictureUri);
        if (subject != null && !"".equals(subject)) {
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }
        if (content != null && !"".equals(content)) {
            intent.putExtra(Intent.EXTRA_TEXT, content);
        }

        // 设置弹出框标题
        if (dlgTitle != null && !"".equals(dlgTitle)) { // 自定义标题
            startActivity(Intent.createChooser(intent, dlgTitle));
        } else { // 系统默认标题
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_share_my_schedule:
                ImgTool imgTool = new ImgTool();
                if (ContextCompat.checkSelfPermission(QRCodeShareActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    EasyPermissions.requestPermissions(this, "请求读写内存", REQUEST_SHARE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                } else {
                    Bitmap bitmap = createBitmap2(getBinding().rlShare);
                    imgTool.saveImageToGallery(QRCodeShareActivity.this, bitmap);
                    shareImg("分享我的课程表", null, null);
                }
                break;
            case R.id.ll_save_to_local:
                ImgTool imgUtil=new ImgTool();
                if (ContextCompat.checkSelfPermission(QRCodeShareActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE )!= PackageManager.PERMISSION_GRANTED){
                    EasyPermissions.requestPermissions(this, "请求储存到本地", REQUEST_SAVE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }else {
                    Bitmap bitmap = createBitmap2(getBinding().rlShare);
                    imgUtil.saveImageToGallery(QRCodeShareActivity.this, bitmap);
//                    ToastUtil.show("图片保存成功！");
                }
                break;
            case R.id.ll_back:
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        switch (requestCode) {
            case REQUEST_SHARE:
                ImgTool imgTool = new ImgTool();
                Bitmap bitmap = createBitmap2(getBinding().rlShare);
                imgTool.saveImageToGallery(QRCodeShareActivity.this, bitmap);
                shareImg("分享二维码",null,null);
                break;
            case REQUEST_SAVE:
                ImgTool imgTool1 = new ImgTool();
                Bitmap bitmap1 = createBitmap2(getBinding().rlShare);
                imgTool1.saveImageToGallery(QRCodeShareActivity.this, bitmap1);
                break;
            default:
                break;
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    public Bitmap createBitmap2(View v) {
        Bitmap bmp = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        c.drawColor(Color.WHITE);
        v.draw(c);
        return bmp;
    }

    private void init() {
        studentId = SharePreferenceLab.getInstance().getStudentId(this);
        headPath = SharePreferenceLab.getInstance().getHeadPath(this);
        studentName = SharePreferenceLab.getDisplayName();
        semester = SharePreferenceLab.getSemester();
        password = SharePreferenceLab.getInstance().getPassword(this);
        Glide.with(this)
                .load(headPath)
                .apply(RequestOptions.placeholderOf(R.drawable.user_img_holder))
                .apply(RequestOptions.centerCropTransform())
                .into(getBinding().ivUser);
        getBinding().tvUserName.setText(studentName);
        getBinding().tvSemester.setText(semester);
    }
}