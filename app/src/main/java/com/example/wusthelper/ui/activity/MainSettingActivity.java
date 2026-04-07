package com.example.wusthelper.ui.activity;

import static com.example.wusthelper.MyApplication.getContext;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.databinding.ActivityMainSettingBinding;
import com.example.wusthelper.dbhelper.CourseDB;
import com.example.wusthelper.helper.MyDialogHelper;
import com.example.wusthelper.helper.SharePreferenceLab;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * 主页设置
 */
public class MainSettingActivity extends BaseActivity<ActivityMainSettingBinding> implements View.OnClickListener{

    public static Intent newInstance(Context context) {
        return new Intent(context, MainSettingActivity.class);
    }

    //用于显示的Dialog
    private SweetAlertDialog dialog;


    @Override
    public void initView() {
        getWindow().setStatusBarColor(Color.WHITE);
        getBinding().settingBar.setOnClickListener(this);
        getBinding().ivBack.setOnClickListener(this);
        getBinding().rlRefreshWidget.setOnClickListener(this);
        getBinding().rlQrCode.setOnClickListener(this);
        getBinding().deleteQrCourse.setOnClickListener(this);
        getBinding().changeQrCourse.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(SharePreferenceLab.getIsGraduate()){
            getBinding().rlQrCode.setVisibility(View.GONE);
            getBinding().deleteQrCourse.setVisibility(View.GONE);
            getBinding().changeQrCourse.setVisibility(View.GONE);
        }

    }

    @Override
    public void onClick(View v) {
        if(v.equals(getBinding().settingBar)) {
            startActivity(NaviBarSettingActivity.newInstance(this));
        } else if(v.equals(getBinding().rlRefreshWidget)){
            startActivity(new Intent(this, WidgetSettingsActivity.class));
        } else if( v.equals((getBinding().ivBack))) {
            finish();
        } else if(v.equals((getBinding().rlQrCode))) {
            startActivity(QRCodeShareActivity.newInstance(getContext()));
        } else if(v.equals(getBinding().deleteQrCourse)) {
            if(CourseDB.queryQrCourse()){
                MyDialogHelper.getSweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE, null,
                        "确认删除情侣课表数据吗？", "删除", "取消", new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
                                CourseDB.deleteQrCourse();
                                SharePreferenceLab.setIsGetQr(false);
                                sweetAlertDialog.cancel();
                                showCommonDialog(SweetAlertDialog.SUCCESS_TYPE, "删除成功 \n",
                                        null,
                                        "确定", null);
                            }
                        }).show();
            }else {
                showCommonDialog(SweetAlertDialog.ERROR_TYPE, "无情侣课表数据",
                        null,
                        "确定", null);
            }

        } else if(v.equals(getBinding().changeQrCourse)){
            if(CourseDB.queryQrCourse() && !SharePreferenceLab.getIsGetQr()){
                MyDialogHelper.getSweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE, null,
                        "确认切换为情侣的课表吗？", "确认", "取消", new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
                                SharePreferenceLab.setIsGetQr(true);
                                sweetAlertDialog.cancel();
                                showCommonDialog(SweetAlertDialog.SUCCESS_TYPE, "切换成功 \n",
                                        null,
                                        "确定", null);
                            }
                        }).show();
            }else if(!CourseDB.queryQrCourse() && !SharePreferenceLab.getIsGetQr()){
                showCommonDialog(SweetAlertDialog.ERROR_TYPE, "无情侣课表数据",
                        null,
                        "确定", null);
            }else if(CourseDB.queryQrCourse() && SharePreferenceLab.getIsGetQr()){
                MyDialogHelper.getSweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE, null,
                        "确认切换为自己的课表吗？", "确认", "取消", new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
                                SharePreferenceLab.setIsGetQr(false);
                                sweetAlertDialog.cancel();
                                showCommonDialog(SweetAlertDialog.SUCCESS_TYPE, "切换成功 \n",
                                        null,
                                        "确定", null);
                            }
                        }).show();
            }
        }




    }


    /**
     * 展示一个Dialog
     */
    public void showCommonDialog(int DialogType, String title, String contentText, String confirmText,
                                 SweetAlertDialog.OnSweetClickListener listener) {
        dialog = MyDialogHelper.getCommonDialog(this, DialogType,
                title, contentText, confirmText);
        dialog.show();
    }

}