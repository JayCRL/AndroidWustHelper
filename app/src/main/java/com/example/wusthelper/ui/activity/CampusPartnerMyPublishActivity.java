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
import com.example.wusthelper.bean.javabean.data.CampusMateActivity;
import com.example.wusthelper.bean.javabean.data.CampusMateActivityListData;
import com.example.wusthelper.databinding.ActivityCampusPartnerMyPublishBinding;
import com.example.wusthelper.request.NewApiHelper;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;

import java.util.ArrayList;
import java.util.List;

public class CampusPartnerMyPublishActivity extends BaseActivity<ActivityCampusPartnerMyPublishBinding> {

    private PartnerAdapter adapter;

    public static Intent newInstance(Context context) {
        return new Intent(context, CampusPartnerMyPublishActivity.class);
    }

    @Override
    public void initView() {
        getBinding().tbTitle.tvTitleTitle.setText("我的活动");
        getBinding().tbTitle.ivTitleBack.setOnClickListener(v -> finish());
        getBinding().rvList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PartnerAdapter(new ArrayList<>());
        adapter.setOnItemClickListener((a, view, position) -> {
            CampusMateActivity item = adapter.getItem(position);
            if (item != null) {
                startActivity(CampusPartnerDetailActivity.newInstance(this, item.id));
            }
        });
        getBinding().rvList.setAdapter(adapter);
        loadData();
    }

    private void loadData() {
        NewApiHelper.getCampusMateMyCreated(new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                CampusMateActivityListData data = (CampusMateActivityListData) responseObj;
                List<CampusMateActivity> list = data.getData() == null ? new ArrayList<>() : data.getData();
                runOnUiThread(() -> {
                    adapter.setNewData(list);
                    getBinding().tvEmpty.setVisibility(list.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
                });
            }

            @Override
            public void onFailure(Object reasonObj) {
                runOnUiThread(() -> Toast.makeText(CampusPartnerMyPublishActivity.this, "加载失败", Toast.LENGTH_SHORT).show());
            }
        });
    }

    static class PartnerAdapter extends BaseQuickAdapter<CampusMateActivity, BaseViewHolder> {
        PartnerAdapter(List<CampusMateActivity> data) {
            super(R.layout.item_campus_partner, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, CampusMateActivity item) {
            helper.setText(R.id.tv_title, item.title);
            helper.setText(R.id.tv_desc, item.description);
            helper.setText(R.id.tv_location, item.location != null ? item.location : item.campus);
            helper.setText(R.id.tv_people, item.minPeople + "-" + item.maxPeople + "人");
            helper.setText(R.id.tv_time, item.createdAt == null ? "" : (item.createdAt.length() > 10 ? item.createdAt.substring(0, 10) : item.createdAt));
            TextView tvType = helper.getView(R.id.tv_type);
            tvType.setText(item.type);
            tvType.getBackground().setTint(Color.parseColor("#03A9F4"));
        }
    }
}
