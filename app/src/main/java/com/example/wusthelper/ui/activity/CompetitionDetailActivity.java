package com.example.wusthelper.ui.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.example.wusthelper.R;
import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.bean.javabean.data.BaseData;
import com.example.wusthelper.bean.javabean.data.CompetitionPageData;
import com.example.wusthelper.bean.javabean.data.CompetitionPost;
import com.example.wusthelper.bean.javabean.data.CompetitionResponsePageData;
import com.example.wusthelper.bean.javabean.data.ResponsePost;
import com.example.wusthelper.databinding.ActivityCompetitionDetailBinding;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.request.NewApiHelper;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CompetitionDetailActivity extends BaseActivity<ActivityCompetitionDetailBinding> {

    private static final String EXTRA_POST = "post";
    private static final String EXTRA_CAN_DELETE = "can_delete";

    private ResponseAdapter adapter;
    private CompetitionPost post;
    private String studentId;
    private boolean canDelete;

    public static Intent newInstance(Context context, CompetitionPost item) {
        Intent intent = new Intent(context, CompetitionDetailActivity.class);
        intent.putExtra(EXTRA_POST, item);
        return intent;
    }

    public static Intent newOwnerInstance(Context context, CompetitionPost item) {
        Intent intent = newInstance(context, item);
        intent.putExtra(EXTRA_CAN_DELETE, true);
        return intent;
    }

    @Override
    public void initView() {
        post = (CompetitionPost) getIntent().getSerializableExtra(EXTRA_POST);
        studentId = SharePreferenceLab.getStudentId();
        canDelete = getIntent().getBooleanExtra(EXTRA_CAN_DELETE, false);
        getBinding().tbTitle.tvTitleTitle.setText("组队详情");
        getBinding().tbTitle.ivTitleBack.setOnClickListener(v -> finish());
        getBinding().rvResponses.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ResponseAdapter(new ArrayList<>());
        getBinding().rvResponses.setAdapter(adapter);
        bindBaseInfo();
        getBinding().btnSubmitResponse.setOnClickListener(v -> submitResponse());
        getBinding().tvStatus.setOnClickListener(v -> {
            if (canDelete) {
                confirmDelete();
            }
        });
        loadResponses();
    }

    private void bindBaseInfo() {
        if (post == null) {
            Toast.makeText(this, "帖子信息无效", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        getBinding().tvTitle.setText(safeText(post.competitionName, "未命名比赛"));
        getBinding().tvIntro.setText(safeText(post.competitionIntroduction, "暂无简介"));
        getBinding().tvRequirement.setText(safeText(post.requirement, "暂无要求"));
        getBinding().tvContact.setText("联系方式：" + safeText(post.contactInformation, "未提供"));
        getBinding().tvPublisher.setText("发起人：" + safeText(post.studentId, "未知"));
        getBinding().tvStatus.setText(canDelete ? "招募中 · 点击删除" : getStatusLabel(post.status));
        getBinding().tvTime.setText("更新时间：" + formatTime(post.endUpdateTime));
    }

    private void loadResponses() {
        NewApiHelper.getCompetitionResponses(post == null ? 0 : post.cid, 1, 50, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                CompetitionResponsePageData data = (CompetitionResponsePageData) responseObj;
                CompetitionPageData<ResponsePost> pageData = data.getData();
                List<ResponsePost> records = pageData == null || pageData.records == null ? new ArrayList<>() : pageData.records;
                runOnUiThread(() -> {
                    adapter.setNewData(records);
                    getBinding().tvEmpty.setVisibility(records.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
                });
            }

            @Override
            public void onFailure(Object reasonObj) {
                runOnUiThread(() -> Toast.makeText(CompetitionDetailActivity.this, "响应加载失败", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void submitResponse() {
        String response = getBinding().etResponse.getText().toString().trim();
        if (response.isEmpty()) {
            Toast.makeText(this, "请输入响应内容", Toast.LENGTH_SHORT).show();
            return;
        }
        NewApiHelper.createCompetitionResponse(post == null ? 0 : post.cid, safeText(studentId, ""), response, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                BaseData data = (BaseData) responseObj;
                runOnUiThread(() -> {
                    Toast.makeText(CompetitionDetailActivity.this,
                            data.isSuccess() ? "响应发布成功" : safeText(data.getMsg(), "响应发布失败"), Toast.LENGTH_SHORT).show();
                    if (data.isSuccess()) {
                        getBinding().etResponse.setText("");
                        loadResponses();
                    }
                });
            }

            @Override
            public void onFailure(Object reasonObj) {
                runOnUiThread(() -> Toast.makeText(CompetitionDetailActivity.this, "响应发布失败", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void confirmDelete() {
        if (post == null) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("删除帖子")
                .setMessage("确认删除这条组队帖吗？")
                .setPositiveButton("删除", (dialog, which) -> deletePost())
                .setNegativeButton("取消", null)
                .show();
    }

    private void deletePost() {
        NewApiHelper.deleteCompetitionPost(post.cid, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                BaseData data = (BaseData) responseObj;
                runOnUiThread(() -> {
                    Toast.makeText(CompetitionDetailActivity.this,
                            data.isSuccess() ? "删除成功" : safeText(data.getMsg(), "删除失败"), Toast.LENGTH_SHORT).show();
                    if (data.isSuccess()) {
                        setResult(RESULT_OK);
                        finish();
                    }
                });
            }

            @Override
            public void onFailure(Object reasonObj) {
                runOnUiThread(() -> Toast.makeText(CompetitionDetailActivity.this, "删除失败", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private String getStatusLabel(int status) {
        return status == 0 ? "招募中" : "已结束";
    }

    private String formatTime(String source) {
        if (source == null || source.trim().isEmpty()) {
            return "未知";
        }
        try {
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = parser.parse(source);
            if (date != null) {
                return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(date);
            }
        } catch (Exception ignore) {
        }
        return source;
    }

    private String safeText(String text, String fallback) {
        return text == null || text.trim().isEmpty() ? fallback : text.trim();
    }

    static class ResponseAdapter extends BaseQuickAdapter<ResponsePost, BaseViewHolder> {
        ResponseAdapter(List<ResponsePost> data) {
            super(R.layout.item_competition_response, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, ResponsePost item) {
            helper.setText(R.id.tv_student_id, item.studentId == null || item.studentId.trim().isEmpty() ? "匿名同学" : item.studentId);
            helper.setText(R.id.tv_response, item.response == null || item.response.trim().isEmpty() ? "暂无内容" : item.response);
            helper.setText(R.id.tv_time, item.endUpdateTime == null ? "" : item.endUpdateTime.replace("T", " "));
        }
    }
}
