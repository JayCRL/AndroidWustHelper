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
import com.example.wusthelper.bean.javabean.data.CompetitionPostPageData;
import com.example.wusthelper.databinding.ActivityCompetitionMyPostsBinding;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.request.NewApiHelper;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CompetitionMyPostsActivity extends BaseActivity<ActivityCompetitionMyPostsBinding> {

    private static final int REQUEST_DETAIL = 4101;
    private static final int REQUEST_EDIT = 4102;

    private CompetitionMyPostAdapter adapter;

    public static Intent newInstance(Context context) {
        return new Intent(context, CompetitionMyPostsActivity.class);
    }

    @Override
    public void initView() {
        getBinding().tbTitle.tvTitleTitle.setText("我的帖子");
        getBinding().tbTitle.ivTitleBack.setOnClickListener(v -> finish());
        getBinding().rvList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CompetitionMyPostAdapter(new ArrayList<>());
        adapter.setOnItemClickListener((a, view, position) -> {
            CompetitionPost item = adapter.getItem(position);
            if (item != null) {
                startActivityForResult(CompetitionDetailActivity.newOwnerInstance(this, item), REQUEST_DETAIL);
            }
        });
        adapter.addChildClickViewIds(R.id.tv_edit, R.id.tv_delete);
        adapter.setOnItemChildClickListener((a, view, position) -> {
            CompetitionPost item = adapter.getItem(position);
            if (item == null) {
                return;
            }
            if (view.getId() == R.id.tv_edit) {
                startActivityForResult(CompetitionPublishActivity.newEditInstance(this, item), REQUEST_EDIT);
            } else if (view.getId() == R.id.tv_delete) {
                confirmDelete(item);
            }
        });
        getBinding().rvList.setAdapter(adapter);
        loadData();
    }

    private void loadData() {
        NewApiHelper.getCompetitionPosts(SharePreferenceLab.getStudentId(), 0, "", 1, 50, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                CompetitionPostPageData data = (CompetitionPostPageData) responseObj;
                CompetitionPageData<CompetitionPost> pageData = data.getData();
                List<CompetitionPost> records = pageData == null || pageData.records == null ? new ArrayList<>() : pageData.records;
                runOnUiThread(() -> {
                    adapter.setNewData(records);
                    getBinding().tvEmpty.setVisibility(records.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
                });
            }

            @Override
            public void onFailure(Object reasonObj) {
                runOnUiThread(() -> Toast.makeText(CompetitionMyPostsActivity.this, "加载失败", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void confirmDelete(CompetitionPost item) {
        new AlertDialog.Builder(this)
                .setTitle("删除帖子")
                .setMessage("确认删除“" + safeText(item.competitionName, "该帖子") + "”吗？")
                .setPositiveButton("删除", (dialog, which) -> deletePost(item.cid))
                .setNegativeButton("取消", null)
                .show();
    }

    private void deletePost(int cid) {
        NewApiHelper.deleteCompetitionPost(cid, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                BaseData data = (BaseData) responseObj;
                runOnUiThread(() -> {
                    Toast.makeText(CompetitionMyPostsActivity.this,
                            data.isSuccess() ? "删除成功" : safeText(data.getMsg(), "删除失败"), Toast.LENGTH_SHORT).show();
                    if (data.isSuccess()) {
                        loadData();
                        setResult(RESULT_OK);
                    }
                });
            }

            @Override
            public void onFailure(Object reasonObj) {
                runOnUiThread(() -> Toast.makeText(CompetitionMyPostsActivity.this, "删除失败", Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == REQUEST_DETAIL || requestCode == REQUEST_EDIT) && resultCode == RESULT_OK) {
            loadData();
            setResult(RESULT_OK);
        }
    }

    private String formatTime(String source) {
        if (source == null || source.trim().isEmpty()) {
            return "未知";
        }
        try {
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = parser.parse(source);
            if (date != null) {
                return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
            }
        } catch (Exception ignore) {
        }
        return source;
    }

    private String safeText(String text, String fallback) {
        return text == null || text.trim().isEmpty() ? fallback : text.trim();
    }

    static class CompetitionMyPostAdapter extends BaseQuickAdapter<CompetitionPost, BaseViewHolder> {
        CompetitionMyPostAdapter(List<CompetitionPost> data) {
            super(R.layout.item_competition, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, CompetitionPost item) {
            helper.setText(R.id.tv_title, item.competitionName);
            helper.setText(R.id.tv_intro, item.competitionIntroduction == null ? "暂无简介" : item.competitionIntroduction);
            helper.setText(R.id.tv_student_id, item.studentId == null || item.studentId.trim().isEmpty() ? "发起人：未知" : "发起人：" + item.studentId);
            helper.setText(R.id.tv_status, item.status == 0 ? "招募中" : "已结束");
            helper.setText(R.id.tv_time, item.endUpdateTime == null ? "" : item.endUpdateTime.replace("T", " "));
        }
    }
}
