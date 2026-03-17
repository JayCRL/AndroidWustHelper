package com.example.wusthelper.ui.activity;

import android.content.Context;
import android.content.Intent;
import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.databinding.ActivitySecondHandBinding;

public class SecondHandActivity extends BaseActivity<ActivitySecondHandBinding> {

    public static Intent newInstance(Context context) {
        return new Intent(context, SecondHandActivity.class);
    }

    @Override
    public void initView() {
        getBinding().tbTitle.tvTitleTitle.setText("二手平台");
        getBinding().tbTitle.ivTitleBack.setOnClickListener(v -> finish());
    }
}