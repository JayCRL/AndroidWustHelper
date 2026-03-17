package com.example.wusthelper.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.example.wusthelper.R;
import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.bean.javabean.data.EmptyClassroomSimpleData;
import com.example.wusthelper.databinding.ActivitySearchRoomResultBinding;
import com.example.wusthelper.request.NewApiHelper;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;
import com.example.wusthelper.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

public class SearchRoomResultActivity extends BaseActivity<ActivitySearchRoomResultBinding> {

    private static final String EXTRA_WEEK = "extra_week";
    private static final String EXTRA_WEEKDAY = "extra_weekday";
    private static final String EXTRA_SECTION = "extra_section";

    private final List<String> rooms = new ArrayList<>();
    private RoomAdapter adapter;

    public static Intent newInstance(Context context, String week, String weekDay, String section) {
        Intent intent = new Intent(context, SearchRoomResultActivity.class);
        intent.putExtra(EXTRA_WEEK, week);
        intent.putExtra(EXTRA_WEEKDAY, weekDay);
        intent.putExtra(EXTRA_SECTION, section);
        return intent;
    }

    @Override
    public void initView() {
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        getBinding().ivBack.setOnClickListener(v -> finish());
        String week = getIntent().getStringExtra(EXTRA_WEEK);
        String weekDay = getIntent().getStringExtra(EXTRA_WEEKDAY);
        String section = getIntent().getStringExtra(EXTRA_SECTION);
        getBinding().toolbar.setText("第" + week + "周 · 星期" + weekDay + " · 第" + section + "节");
        getBinding().recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RoomAdapter(rooms);
        getBinding().recyclerView.setAdapter(adapter);
        loadData(week, weekDay, section);
    }

    private void loadData(String week, String weekDay, String section) {
        getBinding().progressBar.setVisibility(View.VISIBLE);
        getBinding().tvEmpty.setVisibility(View.GONE);
        NewApiHelper.getEmptyClassrooms(week, weekDay, section, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                getBinding().progressBar.setVisibility(View.GONE);
                EmptyClassroomSimpleData data = (EmptyClassroomSimpleData) responseObj;
                if ("401".equals(data.getCode())) {
                    handleUnauthorized(data.getMsg());
                    return;
                }
                if (data.isSuccess()) {
                    rooms.clear();
                    if (data.getData() != null) {
                        rooms.addAll(data.getData());
                    }
                    adapter.notifyDataSetChanged();
                    getBinding().tvEmpty.setVisibility(rooms.isEmpty() ? View.VISIBLE : View.GONE);
                    getBinding().tvEmpty.setText("当前时段暂无可用教室");
                } else {
                    getBinding().tvEmpty.setVisibility(View.VISIBLE);
                    getBinding().tvEmpty.setText("空教室查询失败，请稍后重试");
                    ToastUtil.show(getMessage(data.getMsg(), "空教室查询失败"));
                }
            }

            @Override
            public void onFailure(Object reasonObj) {
                getBinding().progressBar.setVisibility(View.GONE);
                getBinding().tvEmpty.setVisibility(View.VISIBLE);
                getBinding().tvEmpty.setText("请求失败，请检查网络后重试");
                ToastUtil.show("请求失败，可能是网络未链接或请求超时");
            }
        });
    }

    private void handleUnauthorized(String msg) {
        NewApiHelper.clearLoginState();
        ToastUtil.show(getMessage(msg, "登录已失效，请重新登录"));
        startActivity(LoginMvpActivity.newInstance(this));
        finish();
    }

    private String getMessage(String msg, String fallback) {
        return msg == null || msg.trim().isEmpty() ? fallback : msg;
    }

    static class RoomAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
        RoomAdapter(List<String> data) {
            super(android.R.layout.simple_list_item_1, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, String item) {
            helper.setText(android.R.id.text1, item);
        }
    }
}
