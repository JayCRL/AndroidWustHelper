package com.example.wusthelper.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.bean.javabean.data.BaseData;
import com.example.wusthelper.bean.javabean.data.CompetitionPost;
import com.example.wusthelper.databinding.ActivityCompetitionPublishBinding;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.request.NewApiHelper;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;

public class CompetitionPublishActivity extends BaseActivity<ActivityCompetitionPublishBinding> {

    private static final String EXTRA_POST = "post";

    private CompetitionPost editPost;

    public static Intent newInstance(Context context) {
        return new Intent(context, CompetitionPublishActivity.class);
    }

    public static Intent newEditInstance(Context context, CompetitionPost post) {
        Intent intent = new Intent(context, CompetitionPublishActivity.class);
        intent.putExtra(EXTRA_POST, post);
        return intent;
    }

    @Override
    public void initView() {
        editPost = (CompetitionPost) getIntent().getSerializableExtra(EXTRA_POST);
        getBinding().tbTitle.tvTitleTitle.setText(editPost == null ? "发布组队帖" : "编辑组队帖");
        getBinding().tbTitle.ivTitleBack.setOnClickListener(v -> finish());
        bindEditData();
        getBinding().btnSubmit.setText(editPost == null ? "发布组队帖" : "保存并重新发布");
        getBinding().btnSubmit.setOnClickListener(v -> submit());
    }

    private void bindEditData() {
        if (editPost == null) {
            return;
        }
        getBinding().etCompetitionName.setText(safeText(editPost.competitionName, ""));
        getBinding().etIntro.setText(safeText(editPost.competitionIntroduction, ""));
        getBinding().etRequirement.setText(safeText(editPost.requirement, ""));
        getBinding().etContact.setText(safeText(editPost.contactInformation, ""));
    }

    private void submit() {
        String studentId = SharePreferenceLab.getStudentId();
        String competitionName = getBinding().etCompetitionName.getText().toString().trim();
        String intro = getBinding().etIntro.getText().toString().trim();
        String requirement = getBinding().etRequirement.getText().toString().trim();
        String contact = getBinding().etContact.getText().toString().trim();

        if (competitionName.isEmpty()) {
            Toast.makeText(this, "请输入比赛名称", Toast.LENGTH_SHORT).show();
            return;
        }
        if (intro.isEmpty()) {
            Toast.makeText(this, "请输入比赛简介", Toast.LENGTH_SHORT).show();
            return;
        }
        if (requirement.isEmpty()) {
            Toast.makeText(this, "请输入组队要求", Toast.LENGTH_SHORT).show();
            return;
        }
        if (contact.isEmpty()) {
            Toast.makeText(this, "请输入联系方式", Toast.LENGTH_SHORT).show();
            return;
        }

        if (editPost == null) {
            publish(studentId, competitionName, intro, requirement, contact);
        } else {
            republishAfterDelete(studentId, competitionName, intro, requirement, contact);
        }
    }

    private void publish(String studentId, String competitionName, String intro, String requirement, String contact) {
        NewApiHelper.createCompetitionPost(studentId, competitionName, intro, requirement, contact, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                BaseData data = (BaseData) responseObj;
                runOnUiThread(() -> {
                    Toast.makeText(CompetitionPublishActivity.this,
                            data.isSuccess() ? "发布成功" : safeText(data.getMsg(), "发布失败"), Toast.LENGTH_SHORT).show();
                    if (data.isSuccess()) {
                        setResult(RESULT_OK);
                        finish();
                    }
                });
            }

            @Override
            public void onFailure(Object reasonObj) {
                runOnUiThread(() -> Toast.makeText(CompetitionPublishActivity.this, "发布失败", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void republishAfterDelete(String studentId, String competitionName, String intro, String requirement, String contact) {
        NewApiHelper.deleteCompetitionPost(editPost.cid, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                BaseData data = (BaseData) responseObj;
                if (!data.isSuccess()) {
                    runOnUiThread(() -> Toast.makeText(CompetitionPublishActivity.this,
                            safeText(data.getMsg(), "删除旧帖子失败"), Toast.LENGTH_SHORT).show());
                    return;
                }
                publish(studentId, competitionName, intro, requirement, contact);
            }

            @Override
            public void onFailure(Object reasonObj) {
                runOnUiThread(() -> Toast.makeText(CompetitionPublishActivity.this, "删除旧帖子失败", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private String safeText(String text, String fallback) {
        return text == null || text.trim().isEmpty() ? fallback : text.trim();
    }
}
