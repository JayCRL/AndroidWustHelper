package com.example.wusthelper.ui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.wusthelper.R;
import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.databinding.ActivityNaviBarSettingBinding;
import com.example.wusthelper.helper.SharePreferenceLab;

/**
 * 自定义底部导航栏
 */

public class NaviBarSettingActivity extends BaseActivity<ActivityNaviBarSettingBinding> implements View.OnClickListener{

    public static Intent newInstance(Context context) {
        return new Intent(context, NaviBarSettingActivity.class);
    }

    @Override
    public void initView() {
        getWindow().setStatusBarColor(Color.WHITE);
        if(SharePreferenceLab.getIsGraduate()) {
            getBinding().volunteer.setVisibility(View.GONE);
        }
        initBarState();
        setOnclickListener();
    }

    private void initBarState() {
        if(SharePreferenceLab.getBarState() == SharePreferenceLab.BARSTATEDEFAULT) {
            getBinding().viewAll.setText("显示");
            getBinding().recover.setVisibility(View.GONE);
            getBinding().consultDelete.setVisibility((View.GONE));
            getBinding().consult.setVisibility(View.VISIBLE);
            if(!SharePreferenceLab.getIsGraduate()) {
                getBinding().volunteerDelete.setVisibility((View.GONE));
                getBinding().volunteer.setVisibility(View.VISIBLE);
            }

        }else if(SharePreferenceLab.getBarState() == SharePreferenceLab.BARSTATEDELETEVOL){
            getBinding().viewAll.setText("显示");
            getBinding().recover.setVisibility(View.VISIBLE);
            getBinding().consultDelete.setVisibility((View.GONE));
            getBinding().consult.setVisibility(View.VISIBLE);
            if(!SharePreferenceLab.getIsGraduate()) {
                getBinding().volunteerDelete.setVisibility((View.VISIBLE));
                getBinding().volunteer.setVisibility(View.GONE);
            }
        }else if (SharePreferenceLab.getBarState() == SharePreferenceLab.BARSTATEDELETECON) {
            getBinding().viewAll.setText("显示");
            getBinding().recover.setVisibility(View.VISIBLE);
            getBinding().consultDelete.setVisibility((View.VISIBLE));
            getBinding().consult.setVisibility(View.GONE);
            if(!SharePreferenceLab.getIsGraduate()) {
                getBinding().volunteerDelete.setVisibility((View.GONE));
                getBinding().volunteer.setVisibility(View.VISIBLE);
            }
        }else if (SharePreferenceLab.getBarState() == SharePreferenceLab.BARSTATEDELETETOW) {
            getBinding().viewAll.setText("显示");
            getBinding().recover.setVisibility(View.VISIBLE);
            getBinding().consultDelete.setVisibility((View.VISIBLE));
            getBinding().consult.setVisibility(View.GONE);
            getBinding().volunteerDelete.setVisibility((View.VISIBLE));
            getBinding().volunteer.setVisibility(View.GONE);
        }else if (SharePreferenceLab.getBarState() == SharePreferenceLab.BARSTATEDELETEALL) {
            getBinding().viewAll.setText("显示（已隐藏全部）");
            getBinding().recover.setVisibility(View.VISIBLE);
            getBinding().consultDelete.setVisibility((View.GONE));
            getBinding().consult.setVisibility(View.VISIBLE);
            if(SharePreferenceLab.getIsGraduate()) {
                getBinding().volunteerDelete.setVisibility((View.GONE));
                getBinding().volunteer.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setOnclickListener() {
        getBinding().imageVolunteer.setOnClickListener(this);
        getBinding().imageVolunteerDelete.setOnClickListener(this);
        getBinding().imageConsult.setOnClickListener(this);
        getBinding().imageConsultDelete.setOnClickListener(this);
        getBinding().deleteAll.setOnClickListener(this);
        getBinding().recover.setOnClickListener(this);
        getBinding().ivBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_volunteer:
                //如果没有隐藏所有
                if(SharePreferenceLab.getBarState() != SharePreferenceLab.BARSTATEDELETEALL) {
                    //如果已经隐藏校园
                    if(SharePreferenceLab.getBarState() == SharePreferenceLab.BARSTATEDELETECON) {
                        //隐藏两个
                        SharePreferenceLab.setBarState(SharePreferenceLab.BARSTATEDELETETOW);
                    } else {
                        //否则隐藏志愿者
                        SharePreferenceLab.setBarState(SharePreferenceLab.BARSTATEDELETEVOL);
                    }
                    getBinding().volunteer.setVisibility(View.GONE);
                    getBinding().volunteerDelete.setVisibility(View.VISIBLE);
                    getBinding().recover.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.image_volunteer_delete:
                //如果已经隐藏两个
                if(SharePreferenceLab.getBarState() == SharePreferenceLab.BARSTATEDELETETOW) {
                    //状态为隐藏校园
                    SharePreferenceLab.setBarState(SharePreferenceLab.BARSTATEDELETECON);
                } else {
                    //否则状态切换成默认
                    SharePreferenceLab.setBarState(SharePreferenceLab.BARSTATEDEFAULT);
                    getBinding().recover.setVisibility(View.GONE);
                }
                getBinding().volunteerDelete.setVisibility((View.GONE));
                getBinding().volunteer.setVisibility(View.VISIBLE);
                break;
            case R.id.image_consult:
                //如果没有隐藏所有
                if(SharePreferenceLab.getBarState() != SharePreferenceLab.BARSTATEDELETEALL) {
                    //如果是研究生（研究生没有志愿者）
                    if(SharePreferenceLab.getIsGraduate()) {
                        SharePreferenceLab.setBarState(SharePreferenceLab.BARSTATEDELETECON);
                    } else {
                        //不是研究生，已经隐藏志愿者
                        if(SharePreferenceLab.getBarState() == SharePreferenceLab.BARSTATEDELETEVOL) {
                            //状态切换为隐藏两个
                            SharePreferenceLab.setBarState(SharePreferenceLab.BARSTATEDELETETOW);
                        } else {
                            //否则隐藏校园
                            SharePreferenceLab.setBarState(SharePreferenceLab.BARSTATEDELETECON);
                        }

                    }
                    getBinding().recover.setVisibility(View.VISIBLE);
                    getBinding().consult.setVisibility(View.GONE);
                    getBinding().consultDelete.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.image_consult_delete:
                if(SharePreferenceLab.getIsGraduate()) {
                    SharePreferenceLab.setBarState(SharePreferenceLab.BARSTATEDEFAULT);
                    getBinding().recover.setVisibility(View.GONE);
                } else {
                    //如果已经隐藏两个
                    if(SharePreferenceLab.getBarState() == SharePreferenceLab.BARSTATEDELETETOW) {
                        //状态为隐藏志愿者
                        SharePreferenceLab.setBarState(SharePreferenceLab.BARSTATEDELETEVOL);
                    } else {
                        //否则状态切换成默认
                        SharePreferenceLab.setBarState(SharePreferenceLab.BARSTATEDEFAULT);
                        getBinding().recover.setVisibility(View.GONE);
                    }
                }
                getBinding().consultDelete.setVisibility((View.GONE));
                getBinding().consult.setVisibility(View.VISIBLE);
                break;
            case R.id.delete_all:
                if(SharePreferenceLab.getBarState() != SharePreferenceLab.BARSTATEDELETEALL) {
                    SharePreferenceLab.setBarState(SharePreferenceLab.BARSTATEDELETEALL);
                    getBinding().viewAll.setText("显示（已隐藏全部）");
                    getBinding().recover.setVisibility(View.VISIBLE);
                    getBinding().consultDelete.setVisibility((View.GONE));
                    getBinding().consult.setVisibility(View.VISIBLE);
                    if(!SharePreferenceLab.getIsGraduate()) {
                        getBinding().volunteerDelete.setVisibility((View.GONE));
                        getBinding().volunteer.setVisibility(View.VISIBLE);
                    }

                }
                break;
            case R.id.recover:
                if(SharePreferenceLab.getBarState() != SharePreferenceLab.BARSTATEDEFAULT) {
                    SharePreferenceLab.setBarState(SharePreferenceLab.BARSTATEDEFAULT);
                    getBinding().viewAll.setText("显示");
                    getBinding().recover.setVisibility(View.GONE);
                    getBinding().consultDelete.setVisibility((View.GONE));
                    getBinding().consult.setVisibility(View.VISIBLE);
                    if(!SharePreferenceLab.getIsGraduate()) {
                        getBinding().volunteerDelete.setVisibility((View.GONE));
                        getBinding().volunteer.setVisibility(View.VISIBLE);
                    }

                }
                break;
            case R.id.iv_back:
                finish();
            default:
                break;
        }
    }
}