package com.example.wusthelper.ui.activity;

import android.content.Context;
import android.content.Intent;
import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.databinding.ActivityCampusPartnerBinding;

public class CampusPartnerActivity extends BaseActivity<ActivityCampusPartnerBinding> {

    public static Intent newInstance(Context context) {
        return new Intent(context, CampusPartnerActivity.class);
    }

    @Override
    public void initView() {
        getBinding().tbTitle.tvTitleTitle.setText("校园搭子");
        getBinding().tbTitle.ivTitleBack.setOnClickListener(v -> finish());
    }
}