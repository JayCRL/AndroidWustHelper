package com.example.wusthelper.ui.activity;

import static com.example.wusthelper.MyApplication.getContext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.wusthelper.R;
import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.databinding.ActivityUserSettingBinding;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.utils.ResourcesUtils;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import java.util.List;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class UserSettingActivity extends BaseActivity<ActivityUserSettingBinding> implements View.OnClickListener, EasyPermissions.PermissionCallbacks {

    private static final int OPEN_ALBUM = 1;
    private static final String TAG = "UserSettingActivity";

    public static Intent newInstance(Context context) {
        return new Intent(context, UserSettingActivity.class);
    }

    @Override
    public void initView() {
        getWindow().setStatusBarColor(ResourcesUtils.getRealColor(R.color.white));
        changeStatusBarTextColor(true);
        setListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        init();
    }

    private void setListener() {
        getBinding().flHeadSetting.setOnClickListener(this);
        getBinding().flUserName.setOnClickListener(this);
        getBinding().flMajor.setOnClickListener(this);
        getBinding().flQrCode.setOnClickListener(this);
        getBinding().tbUserDataSetting.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void onClick(View v) {
        if(v.equals(getBinding().flHeadSetting)){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openAlbum();
            } else {
                EasyPermissions.requestPermissions(this, "选取本地图片作为头像，首先需要获取您的相册权限", OPEN_ALBUM, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }else if(v.equals(getBinding().flUserName)){
            startActivity(ModifyUserDataActivity.newInstance(getContext(),ModifyUserDataActivity.USER_NAME));
        }else if(v.equals(getBinding().flMajor)){
            startActivity(ModifyUserDataActivity.newInstance(getContext(),ModifyUserDataActivity.MAJOR));
        }else if(v.equals(getBinding().flQrCode)){
            startActivity(QRCodeShareActivity.newInstance(getContext()));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: ");
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case OPEN_ALBUM:
                if (resultCode == RESULT_OK) {
                    List<Uri> headPath = Matisse.obtainResult(data);
                    Log.d(TAG, headPath.get(0).getPath());
                    SharePreferenceLab.getInstance().setHeadPath(this, "content://media" + headPath.get(0).getPath());
                    Glide.with(this)
                            .load("content://media" + headPath.get(0).getPath())
                            .apply(RequestOptions.centerCropTransform())
                            .into(getBinding().ivHead);
                }
                break;
            default:
                break;
        }
    }

    private void openAlbum() {

        Matisse.from(UserSettingActivity.this)
                .choose(MimeType.ofAll())//图片类型
                .countable(false)//true:选中后显示数字;false:选中后显示对号
                .maxSelectable(1)//可选的最大数
                .capture(false)//选择照片时，是否显示拍照
                //参数1 true表示拍照存储在共有目录，false表示存储在私有目录；参数2与 AndroidManifest中authorities值相同，用于适配7.0系统 必须设置
                .theme(R.style.Matisse_Dracula)
                .imageEngine(new GlideEngine())//图片加载引擎
                .forResult(OPEN_ALBUM);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        switch (requestCode) {

            case OPEN_ALBUM:
                break;
            default:
                break;

        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    private void init() {
        String userName = SharePreferenceLab.getDisplayName();
        String headPath = SharePreferenceLab.getInstance().getHeadPath(this);
        String major = SharePreferenceLab.getInstance().getMajor(this);
        String college = SharePreferenceLab.getInstance().getCollege(this);

        Glide.with(this)
                .load(headPath)
                .apply(RequestOptions.placeholderOf(R.drawable.user_img_holder))
                .apply(RequestOptions.centerCropTransform())
                .into(getBinding().ivHead);
        getBinding().tvUserName.setText(userName);
        getBinding().tvMajor.setText(major);
        getBinding().tvAcademy.setText(college);

        if(SharePreferenceLab.getIsGraduate()) {
            getBinding().flTutorName.setVisibility(View.VISIBLE);
            getBinding().flDegree.setVisibility(View.VISIBLE);
            getBinding().flGrade.setVisibility(View.VISIBLE);
            getBinding().tvDegree.setText(SharePreferenceLab.getDegree());
            getBinding().tvGrade.setText(SharePreferenceLab.getGrade());
            getBinding().tvTutorName.setText(SharePreferenceLab.getTutorName());
        }

    }
}