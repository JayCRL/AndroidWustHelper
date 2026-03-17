package com.example.wusthelper.ui.activity;

import android.content.Context;
import android.content.Intent;
import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.databinding.ActivityCompetitionBinding;

public class CompetitionActivity extends BaseActivity<ActivityCompetitionBinding> {

    public static Intent newInstance(Context context) {
        return new Intent(context, CompetitionActivity.class);
    }

    @Override
    public void initView() {
        getBinding().tbTitle.tvTitleTitle.setText("竞赛组队");
        getBinding().tbTitle.ivTitleBack.setOnClickListener(v -> finish());
    }
}