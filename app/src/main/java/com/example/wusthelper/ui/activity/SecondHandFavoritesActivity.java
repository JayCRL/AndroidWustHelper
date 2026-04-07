package com.example.wusthelper.ui.activity;

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
import com.example.wusthelper.bean.javabean.data.Commodity;
import com.example.wusthelper.bean.javabean.data.PageCommodity;
import com.example.wusthelper.bean.javabean.data.SecondHandPageData;
import com.example.wusthelper.databinding.ActivitySecondHandFavoritesBinding;
import com.example.wusthelper.request.NewApiHelper;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;

import java.util.ArrayList;
import java.util.List;

public class SecondHandFavoritesActivity extends BaseActivity<ActivitySecondHandFavoritesBinding> {

    private static final int REQUEST_DETAIL = 3001;

    private SecondHandFavoriteAdapter adapter;

    public static Intent newInstance(Context context) {
        return new Intent(context, SecondHandFavoritesActivity.class);
    }

    @Override
    public void initView() {
        getBinding().tbTitle.tvTitleTitle.setText("我的收藏");
        getBinding().tbTitle.ivTitleBack.setOnClickListener(v -> finish());
        getBinding().rvList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SecondHandFavoriteAdapter(new ArrayList<>());
        adapter.setOnItemClickListener((a, view, position) -> {
            Commodity item = adapter.getItem(position);
            if (item != null) {
                startActivityForResult(SecondHandDetailActivity.newInstance(this, item.pid), REQUEST_DETAIL);
            }
        });
        getBinding().rvList.setAdapter(adapter);
        loadData();
    }

    private void loadData() {
        NewApiHelper.getSecondHandCollectionList(new DisposeDataListener() {
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
                runOnUiThread(() -> Toast.makeText(SecondHandFavoritesActivity.this, "加载失败", Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_DETAIL && resultCode == RESULT_OK) {
            loadData();
            setResult(RESULT_OK);
        }
    }

    static class SecondHandFavoriteAdapter extends BaseQuickAdapter<Commodity, BaseViewHolder> {
        public SecondHandFavoriteAdapter(List<Commodity> data) {
            super(R.layout.item_second_hand, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, Commodity item) {
            helper.setText(R.id.tv_title, item.name);
            helper.setText(R.id.tv_price, "¥ " + String.format("%.2f", item.price));
            helper.setText(R.id.tv_desc, item.introduce == null ? "暂无描述" : item.introduce);
            helper.setText(R.id.tv_time, item.date == null ? "" : (item.date.length() > 10 ? item.date.substring(0, 10) : item.date));
            helper.setGone(R.id.tv_edit, false);
            helper.setGone(R.id.tv_delete, false);
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
        }
    }
}
