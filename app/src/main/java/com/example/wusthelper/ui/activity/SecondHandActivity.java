package com.example.wusthelper.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.example.wusthelper.databinding.ActivitySecondHandBinding;
import com.example.wusthelper.request.NewApiHelper;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;

import java.util.ArrayList;
import java.util.List;

public class SecondHandActivity extends BaseActivity<ActivitySecondHandBinding> {

    private SecondHandAdapter adapter;

    private final String[] categories = {"全部分类", "电子商品", "生活用品", "虚拟商品", "学习用品", "跑腿服务"};
    private final String[] statuses = {"全部状态", "出售", "求购", "其它"};

    private int selectedCategory = -1;
    private int selectedStatus = -1;
    private String searchText = "";

    private static final int REQUEST_PUBLISH = 1001;
    private static final int REQUEST_DETAIL = 1002;
    private static final int REQUEST_MY_PUBLISH = 1003;
    private static final int REQUEST_FAVORITES = 1004;

    public static Intent newInstance(Context context) {
        return new Intent(context, SecondHandActivity.class);
    }

    @Override
    public void initView() {
        getBinding().tbTitle.tvTitleTitle.setText("二手平台");
        getBinding().tbTitle.ivTitleBack.setOnClickListener(v -> finish());

        getBinding().rvSecondHand.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SecondHandAdapter(new ArrayList<>());
        adapter.setOnItemClickListener((baseQuickAdapter, view, position) -> {
            Commodity item = adapter.getItem(position);
            if (item != null) {
                startActivityForResult(SecondHandDetailActivity.newInstance(this, item.pid), REQUEST_DETAIL);
            }
        });
        getBinding().rvSecondHand.setAdapter(adapter);

        getBinding().fabAdd.setOnClickListener(v -> startActivityForResult(SecondHandPublishActivity.newInstance(this), REQUEST_PUBLISH));
        getBinding().tvMyPublish.setOnClickListener(v -> startActivityForResult(SecondHandMyPublishActivity.newInstance(this), REQUEST_MY_PUBLISH));
        getBinding().tvMyFavorites.setOnClickListener(v -> startActivityForResult(SecondHandFavoritesActivity.newInstance(this), REQUEST_FAVORITES));

        initFilters();
        loadData();
    }

    private void initFilters() {
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        getBinding().spinnerCategory.setAdapter(catAdapter);
        getBinding().spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = position - 1;
                loadData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statuses);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        getBinding().spinnerStatus.setAdapter(statusAdapter);
        getBinding().spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedStatus = position - 1;
                loadData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

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
        DisposeDataListener listener = new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                SecondHandPageData response = (SecondHandPageData) responseObj;
                PageCommodity pageData = response.getData();
                List<Commodity> records = pageData == null || pageData.records == null ? new ArrayList<>() : pageData.records;
                runOnUiThread(() -> adapter.setNewData(records));
            }

            @Override
            public void onFailure(Object reasonObj) {
                runOnUiThread(() -> Toast.makeText(SecondHandActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show());
            }
        };

        if (!searchText.isEmpty()) {
            NewApiHelper.searchSecondHand(searchText, 1, 20, selectedCategory, selectedStatus, listener);
        } else if (selectedCategory != -1 || selectedStatus != -1) {
            NewApiHelper.getSecondHandByTypeOrStatus(1, 20, selectedCategory, selectedStatus, listener);
        } else {
            NewApiHelper.getSecondHandAll(1, 20, listener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == REQUEST_PUBLISH || requestCode == REQUEST_DETAIL || requestCode == REQUEST_MY_PUBLISH || requestCode == REQUEST_FAVORITES)
                && resultCode == RESULT_OK) {
            loadData();
        }
    }

    static class SecondHandAdapter extends BaseQuickAdapter<Commodity, BaseViewHolder> {
        public SecondHandAdapter(List<Commodity> data) {
            super(R.layout.item_second_hand, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, Commodity item) {
            helper.setText(R.id.tv_title, item.name);
            helper.setText(R.id.tv_price, "¥ " + String.format("%.2f", item.price));
            helper.setText(R.id.tv_desc, item.introduce);
            helper.setGone(R.id.tv_edit, false);
            helper.setGone(R.id.tv_delete, false);

            String timeStr = item.date;
            if (timeStr != null && timeStr.length() > 10) {
                timeStr = timeStr.substring(0, 10);
            }
            helper.setText(R.id.tv_time, timeStr);

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
