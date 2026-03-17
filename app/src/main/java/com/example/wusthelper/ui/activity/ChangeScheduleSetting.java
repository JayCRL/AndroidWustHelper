package com.example.wusthelper.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;

import androidx.appcompat.app.AlertDialog;

import com.example.wusthelper.R;
import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.databinding.ActivityChangeScheduleSettingBinding;
import com.example.wusthelper.helper.SharePreferenceLab;

public class ChangeScheduleSetting extends BaseActivity<ActivityChangeScheduleSettingBinding> implements CompoundButton.OnCheckedChangeListener {


    public static Intent newInstance(Context context) {
        return new Intent(context, ChangeScheduleSetting.class);
    }

    @Override
    public void initView() {
        getBinding().changeHomepageSwitchStatus.setChecked(SharePreferenceLab.getHomepageSettings());
        getBinding().chooseSundaySwitchStatus.setChecked(SharePreferenceLab.getIsChooseSundayFirst());
        getBinding().changeHomepageSwitchStatus.setOnCheckedChangeListener(this);
        getBinding().chooseSundaySwitchStatus.setOnCheckedChangeListener(this);

        getBinding().ivIntroduce.setOnClickListener(v -> {

            View dialog_explain = LayoutInflater.from(ChangeScheduleSetting.this)
                    .inflate(R.layout.dialog_schedule_settings_explain, null);
            AlertDialog.Builder builder_explain = new AlertDialog.Builder(ChangeScheduleSetting.this);
            builder_explain.setView(dialog_explain);
            builder_explain.setPositiveButton("我知道了", null);
            builder_explain.show();
        });

        getBinding().ivBack.setOnClickListener(v -> finish());
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(buttonView.equals(getBinding().changeHomepageSwitchStatus)){
            SharePreferenceLab.setHomepageSettings(isChecked);
        }else if(buttonView.equals(getBinding().chooseSundaySwitchStatus)){
            SharePreferenceLab.setIsChooseSundayFirst(isChecked);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
