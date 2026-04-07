package com.example.wusthelper.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.bean.javabean.data.BaseData;
import com.example.wusthelper.databinding.ActivityCampusPartnerPublishBinding;
import com.example.wusthelper.request.NewApiHelper;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;

public class CampusPartnerPublishActivity extends BaseActivity<ActivityCampusPartnerPublishBinding> {

    public static Intent newInstance(Context context) {
        return new Intent(context, CampusPartnerPublishActivity.class);
    }

    @Override
    public void initView() {
        getBinding().tbTitle.tvTitleTitle.setText("发布活动");
        getBinding().tbTitle.ivTitleBack.setOnClickListener(v -> finish());
        getBinding().btnSubmit.setOnClickListener(v -> submit());
    }

    private void submit() {
        String title = getBinding().etTitle.getText().toString().trim();
        String description = getBinding().etDescription.getText().toString().trim();
        String activityTime = getBinding().etActivityTime.getText().toString().trim();
        String location = getBinding().etLocation.getText().toString().trim();
        String minPeopleText = getBinding().etMinPeople.getText().toString().trim();
        String maxPeopleText = getBinding().etMaxPeople.getText().toString().trim();
        String expireTime = getBinding().etExpireTime.getText().toString().trim();
        String campus = getBinding().etCampus.getText().toString().trim();
        String college = getBinding().etCollege.getText().toString().trim();
        String type = getBinding().etType.getText().toString().trim();
        String tags = getBinding().etTags.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || activityTime.isEmpty() || location.isEmpty() || minPeopleText.isEmpty() || maxPeopleText.isEmpty() || expireTime.isEmpty() || campus.isEmpty() || college.isEmpty() || type.isEmpty()) {
            Toast.makeText(this, "请完整填写活动信息", Toast.LENGTH_SHORT).show();
            return;
        }

        int minPeople;
        int maxPeople;
        try {
            minPeople = Integer.parseInt(minPeopleText);
            maxPeople = Integer.parseInt(maxPeopleText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "人数格式不正确", Toast.LENGTH_SHORT).show();
            return;
        }
        if (minPeople <= 0 || maxPeople < minPeople) {
            Toast.makeText(this, "人数范围不正确", Toast.LENGTH_SHORT).show();
            return;
        }

        NewApiHelper.createCampusMateActivity(title, description, type, activityTime, location, minPeople, maxPeople, expireTime, campus, college, tags, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                BaseData data = (BaseData) responseObj;
                runOnUiThread(() -> {
                    Toast.makeText(CampusPartnerPublishActivity.this, data.isSuccess() ? safe(data.getMsg(), "发布成功") : safe(data.getMsg(), "发布失败"), Toast.LENGTH_SHORT).show();
                    if (data.isSuccess()) {
                        setResult(RESULT_OK);
                        finish();
                    }
                });
            }

            @Override
            public void onFailure(Object reasonObj) {
                runOnUiThread(() -> Toast.makeText(CampusPartnerPublishActivity.this, "发布失败", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private String safe(String text, String fallback) {
        return text == null || text.trim().isEmpty() ? fallback : text.trim();
    }
}
