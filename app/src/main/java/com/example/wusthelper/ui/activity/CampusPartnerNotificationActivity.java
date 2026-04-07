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
import com.example.wusthelper.bean.javabean.data.CampusMateNotification;
import com.example.wusthelper.bean.javabean.data.CampusMateNotificationListData;
import com.example.wusthelper.databinding.ActivityCampusPartnerNotificationBinding;
import com.example.wusthelper.request.NewApiHelper;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;

import java.util.ArrayList;
import java.util.List;

public class CampusPartnerNotificationActivity extends BaseActivity<ActivityCampusPartnerNotificationBinding> {

    private NotificationAdapter adapter;

    public static Intent newInstance(Context context) {
        return new Intent(context, CampusPartnerNotificationActivity.class);
    }

    @Override
    public void initView() {
        getBinding().tbTitle.tvTitleTitle.setText("通知中心");
        getBinding().tbTitle.ivTitleBack.setOnClickListener(v -> finish());
        getBinding().rvList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(new ArrayList<>());
        getBinding().rvList.setAdapter(adapter);
        loadData();
    }

    private void loadData() {
        NewApiHelper.getCampusMateNotifications(new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                CampusMateNotificationListData data = (CampusMateNotificationListData) responseObj;
                List<CampusMateNotification> list = data.getData() == null ? new ArrayList<>() : data.getData();
                runOnUiThread(() -> {
                    adapter.setNewData(list);
                    getBinding().tvEmpty.setVisibility(list.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
                });
            }

            @Override
            public void onFailure(Object reasonObj) {
                runOnUiThread(() -> Toast.makeText(CampusPartnerNotificationActivity.this, "加载失败", Toast.LENGTH_SHORT).show());
            }
        });
    }

    static class NotificationAdapter extends BaseQuickAdapter<CampusMateNotification, BaseViewHolder> {
        NotificationAdapter(List<CampusMateNotification> data) {
            super(R.layout.item_campus_partner_notification, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, CampusMateNotification item) {
            TextView type = helper.getView(R.id.tv_type);
            type.setText(item.type == null ? "通知" : item.type);
            type.getBackground().setTint(item.isRead ? Color.parseColor("#BDBDBD") : Color.parseColor("#FF9800"));
            helper.setText(R.id.tv_content, item.content == null ? "暂无内容" : item.content);
            helper.setText(R.id.tv_time, item.createdAt == null ? "" : item.createdAt.replace("T", " "));
        }
    }
}
