package com.example.wusthelper.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.example.wusthelper.R;
import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.bean.javabean.data.CompetitionPost;
import com.example.wusthelper.bean.javabean.data.CompetitionPostPageData;
import com.example.wusthelper.databinding.ActivityCompetitionBinding;
import com.example.wusthelper.request.NewApiHelper;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CompetitionActivity extends BaseActivity<ActivityCompetitionBinding> {

    private static final int REQUEST_PUBLISH = 1002;
    private static final int REQUEST_DETAIL = 1003;
    private static final int REQUEST_MY_POSTS = 1004;

    private CompetitionAdapter adapter;
    private String searchText = "";

    public static Intent newInstance(Context context) {
        return new Intent(context, CompetitionActivity.class);
    }

    @Override
    public void initView() {
        getBinding().tbTitle.tvTitleTitle.setText("竞赛组队");
        getBinding().tbTitle.ivTitleBack.setOnClickListener(v -> finish());

        getBinding().rvCompetition.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CompetitionAdapter(new ArrayList<>());
        adapter.setOnItemClickListener((baseQuickAdapter, view, position) -> {
            CompetitionPost item = adapter.getItem(position);
            if (item != null) {
                startActivityForResult(CompetitionDetailActivity.newInstance(this, item), REQUEST_DETAIL);
            }
        });
        getBinding().rvCompetition.setAdapter(adapter);

        getBinding().fabAdd.setOnClickListener(v -> startActivityForResult(CompetitionPublishActivity.newInstance(this), REQUEST_PUBLISH));
        getBinding().tvMyPosts.setOnClickListener(v -> startActivityForResult(CompetitionMyPostsActivity.newInstance(this), REQUEST_MY_POSTS));

        initFilters();
        loadData();
    }

    private void initFilters() {
        getBinding().ivSearch.setOnClickListener(v -> {
            searchText = getBinding().etSearch.getText().toString().trim();
            loadData();
        });

        getBinding().etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                searchText = getBinding().etSearch.getText().toString().trim();
                loadData();
                return true;
            }
            return false;
        });
    }

    private void loadData() {
        NewApiHelper.getCompetitionPosts("", 0, searchText, 1, 20, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                CompetitionPostPageData data = (CompetitionPostPageData) responseObj;
                if (("1".equals(data.getCode()) || data.isSuccess()) && data.getData() != null) {
                    List<CompetitionPost> records = data.getData().records == null ? new ArrayList<>() : data.getData().records;
                    runOnUiThread(() -> adapter.setNewData(records));
                } else {
                    runOnUiThread(() -> Toast.makeText(CompetitionActivity.this, "获取失败: " + data.getMsg(), Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onFailure(Object reasonObj) {
                runOnUiThread(() -> Toast.makeText(CompetitionActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == REQUEST_PUBLISH || requestCode == REQUEST_DETAIL || requestCode == REQUEST_MY_POSTS) && resultCode == RESULT_OK) {
            loadData();
        }
    }

    static class CompetitionAdapter extends BaseQuickAdapter<CompetitionPost, BaseViewHolder> {
        public CompetitionAdapter(List<CompetitionPost> data) {
            super(R.layout.item_competition, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, CompetitionPost item) {
            helper.setText(R.id.tv_title, item.competitionName);
            helper.setText(R.id.tv_intro, item.competitionIntroduction == null ? "暂无简介" : item.competitionIntroduction);
            helper.setText(R.id.tv_student_id, item.studentId == null || item.studentId.trim().isEmpty() ? "发起人：未知" : "发起人：" + item.studentId);
            helper.setText(R.id.tv_status, item.status == 0 ? "招募中" : "已结束");

            String timeStr = item.endUpdateTime;
            try {
                SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date date = parser.parse(timeStr);
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                if (date != null) {
                    timeStr = formatter.format(date);
                }
            } catch (Exception ignore) {
            }
            helper.setText(R.id.tv_time, timeStr);
        }
    }
}
