package com.example.wusthelper.ui.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.example.wusthelper.R;
import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.bean.javabean.data.BaseData;
import com.example.wusthelper.bean.javabean.data.Commodity;
import com.example.wusthelper.bean.javabean.data.PageCommodity;
import com.example.wusthelper.bean.javabean.data.SecondHandPageData;
import com.example.wusthelper.databinding.ActivitySecondHandMyPublishBinding;
import com.example.wusthelper.request.NewApiHelper;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;

import java.util.ArrayList;
import java.util.List;

public class SecondHandMyPublishActivity extends BaseActivity<ActivitySecondHandMyPublishBinding> {

    private static final int REQUEST_DETAIL = 2001;
    private static final int REQUEST_EDIT = 2002;

    private SecondHandSimpleAdapter adapter;

    public static Intent newInstance(Context context) {
        return new Intent(context, SecondHandMyPublishActivity.class);
    }

    @Override
    public void initView() {
        getBinding().tbTitle.tvTitleTitle.setText("我的发布");
        getBinding().tbTitle.ivTitleBack.setOnClickListener(v -> finish());
        getBinding().rvList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SecondHandSimpleAdapter(new ArrayList<>());
        adapter.setOnItemClickListener((a, view, position) -> {
            Commodity item = adapter.getItem(position);
            if (item != null) {
                startActivityForResult(SecondHandDetailActivity.newOwnerInstance(this, item.pid), REQUEST_DETAIL);
            }
        });
        adapter.addChildClickViewIds(R.id.tv_edit, R.id.tv_delete);
        adapter.setOnItemChildClickListener((a, view, position) -> {
            Commodity item = adapter.getItem(position);
            if (item == null) {
                return;
            }
            int viewId = view.getId();
            if (viewId == R.id.tv_edit) {
                startActivityForResult(SecondHandPublishActivity.newEditInstance(this, item), REQUEST_EDIT);
            } else if (viewId == R.id.tv_delete) {
                confirmDelete(item);
            }
        });
        getBinding().rvList.setAdapter(adapter);
        loadData();
    }

    private void loadData() {
        NewApiHelper.getSecondHandMyPublish(1, 50, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                SecondHandPageData response = (SecondHandPageData) responseObj;
                PageCommodity pageData = response.getData();
                List<Commodity> records = pageData == null || pageData.records == null ? new ArrayList<>() : pageData.records;
                runOnUiThread(() -> {
                    adapter.setNewData(records);
                    getBinding().tvEmpty.setVisibility(records.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
                });
            }

            @Override
            public void onFailure(Object reasonObj) {
                runOnUiThread(() -> Toast.makeText(SecondHandMyPublishActivity.this, "加载失败", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void confirmDelete(Commodity item) {
        new AlertDialog.Builder(this)
                .setTitle("删除商品")
                .setMessage("确认删除“" + safeText(item.name, "该商品") + "”吗？")
                .setPositiveButton("删除", (dialog, which) -> deleteCommodity(item.pid))
                .setNegativeButton("取消", null)
                .show();
    }

    private void deleteCommodity(int pid) {
        NewApiHelper.deleteSecondHand(pid, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                BaseData data = (BaseData) responseObj;
                runOnUiThread(() -> {
                    Toast.makeText(SecondHandMyPublishActivity.this,
                            data.isSuccess() ? "删除成功" : safeText(data.getMsg(), "删除失败"), Toast.LENGTH_SHORT).show();
                    if (data.isSuccess()) {
                        loadData();
                    }
                });
            }

            @Override
            public void onFailure(Object reasonObj) {
                runOnUiThread(() -> Toast.makeText(SecondHandMyPublishActivity.this, "删除失败", Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == REQUEST_DETAIL || requestCode == REQUEST_EDIT) && resultCode == RESULT_OK) {
            loadData();
        }
    }

    private String safeText(String text, String fallback) {
        return text == null || text.trim().isEmpty() ? fallback : text.trim();
    }

    static class SecondHandSimpleAdapter extends BaseQuickAdapter<Commodity, BaseViewHolder> {
        public SecondHandSimpleAdapter(List<Commodity> data) {
            super(R.layout.item_second_hand, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, Commodity item) {
            helper.setText(R.id.tv_title, item.name);
            helper.setText(R.id.tv_price, "¥ " + String.format("%.2f", item.price));
            helper.setText(R.id.tv_desc, item.introduce == null ? "暂无描述" : item.introduce);
            helper.setText(R.id.tv_time, item.date == null ? "" : (item.date.length() > 10 ? item.date.substring(0, 10) : item.date));
            TextView tvStatus = helper.getView(R.id.tv_status);
            if (item.status == 0) {
                tvStatus.setText("出售");
                tvStatus.getBackground().setTint(Color.parseColor("#4CAF50"));
            } else if (item.status == 1) {
                tvStatus.setText("求购");
                tvStatus.getBackground().setTint(Color.parseColor("#FF9800"));
            } else {
                tvStatus.setText("其他");
                tvStatus.getBackground().setTint(Color.parseColor("#9E9E9E"));
            }
            helper.setText(R.id.tv_edit, "编辑");
            helper.setText(R.id.tv_delete, "删除");
            helper.setGone(R.id.tv_edit, true);
            helper.setGone(R.id.tv_delete, true);
        }
    }
}
