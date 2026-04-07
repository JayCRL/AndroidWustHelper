package com.example.wusthelper.ui.activity;

import android.content.Context;
import android.content.Intent;

import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.bean.javabean.data.CampusMateUserInfo;
import com.example.wusthelper.bean.javabean.data.CampusMateUserInfoData;
import com.example.wusthelper.databinding.ActivityCampusPartnerProfileBinding;
import com.example.wusthelper.request.NewApiHelper;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;

public class CampusPartnerProfileActivity extends BaseActivity<ActivityCampusPartnerProfileBinding> {

    public static Intent newInstance(Context context) {
        return new Intent(context, CampusPartnerProfileActivity.class);
    }

    @Override
    public void initView() {
        getBinding().tbTitle.tvTitleTitle.setText("个人资料");
        getBinding().tbTitle.ivTitleBack.setOnClickListener(v -> finish());
        loadData();
    }

    private void loadData() {
        NewApiHelper.getCampusMateUserInfo(new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                CampusMateUserInfoData data = (CampusMateUserInfoData) responseObj;
                CampusMateUserInfo info = data.getData();
                runOnUiThread(() -> bind(info));
            }

            @Override
            public void onFailure(Object reasonObj) {
            }
        });
    }

    private void bind(CampusMateUserInfo info) {
        getBinding().tvStudentId.setText("学号：" + safe(info == null ? null : info.studentId, "未填写"));
        getBinding().tvCampus.setText("校区：" + safe(info == null ? null : info.campus, "未填写"));
        getBinding().tvCollege.setText("学院：" + safe(info == null ? null : info.college, "未填写"));
        getBinding().tvMajor.setText("专业：" + safe(info == null ? null : info.major, "未填写"));
        getBinding().tvGrade.setText("年级：" + safe(info == null ? null : info.grade, "未填写"));
        getBinding().tvGender.setText("性别：" + safe(info == null ? null : info.gender, "未填写"));
        getBinding().tvSignature.setText("签名：" + safe(info == null ? null : info.signature, "这个人很低调"));
        getBinding().tvInterests.setText("兴趣：" + safe(info == null ? null : info.interests, "未填写"));
        getBinding().tvSkills.setText("技能：" + safe(info == null ? null : info.skills, "未填写"));
        String contact = safe(info == null ? null : info.phone, null);
        if (contact == null) {
            contact = safe(info == null ? null : info.wechat, null);
        }
        if (contact == null) {
            contact = safe(info == null ? null : info.qq, "未填写");
        }
        getBinding().tvContact.setText("联系方式：" + contact);
    }

    private String safe(String text, String fallback) {
        if (text == null || text.trim().isEmpty()) {
            return fallback;
        }
        return text.trim();
    }
}
