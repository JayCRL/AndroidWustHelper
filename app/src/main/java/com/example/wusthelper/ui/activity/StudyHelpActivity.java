package com.example.wusthelper.ui.activity;

import android.content.Context;
import android.content.Intent;
import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.databinding.ActivityStudyHelpBinding;

public class StudyHelpActivity extends BaseActivity<ActivityStudyHelpBinding> {

    public static Intent newInstance(Context context) {
        return new Intent(context, StudyHelpActivity.class);
    }

    @Override
    public void initView() {
        getBinding().tbTitle.tvTitleTitle.setText("学习互助");
        getBinding().tbTitle.ivTitleBack.setOnClickListener(v -> finish());
    }
}