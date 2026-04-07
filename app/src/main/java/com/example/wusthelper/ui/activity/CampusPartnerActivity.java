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
import com.example.wusthelper.bean.javabean.data.CampusMateActivity;
import com.example.wusthelper.bean.javabean.data.CampusMateActivityListData;
import com.example.wusthelper.databinding.ActivityCampusPartnerBinding;
import com.example.wusthelper.request.NewApiHelper;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;

import java.util.ArrayList;
import java.util.List;

public class CampusPartnerActivity extends BaseActivity<ActivityCampusPartnerBinding> {

    private static final int REQUEST_PUBLISH = 1201;

    private PartnerAdapter adapter;
    private List<CampusMateActivity> allActivities = new ArrayList<>();

    private String[] campuses = {"全部校区", "黄家湖校区", "青山校区"};
    private String[] colleges = {"全部学院", "材料学部", "城市建设学院", "管理学院", "国际学院", "化学与化工学院", "机械自动化学院", "计算机科学与技术学院", "理学院", "临床学院", "马克思主义学院", "汽车与交通工程学院", "生命科学与健康学院", "体育学院", "外国语学院", "法学与经济学院", "信息科学与工程学院(人工智能学院)", "艺术与设计学院", "资源与环境工程学院", "冶金与能源学院"};
    private String[] types = {"全部类型", "休闲娱乐", "运动健身", "学习互助"};

    private String selectedCampus = null;
    private String selectedCollege = null;
    private String selectedType = null;
    private String searchText = "";

    public static Intent newInstance(Context context) {
        return new Intent(context, CampusPartnerActivity.class);
    }

    @Override
    public void initView() {
        getBinding().tbTitle.tvTitleTitle.setText("校园搭子");
        getBinding().tbTitle.ivTitleBack.setOnClickListener(v -> finish());

        getBinding().rvPartner.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PartnerAdapter(new ArrayList<>());
        adapter.setOnItemClickListener((baseQuickAdapter, view, position) -> {
            CampusMateActivity item = adapter.getItem(position);
            if (item != null) {
                startActivity(CampusPartnerDetailActivity.newInstance(this, item.id));
            }
        });
        getBinding().rvPartner.setAdapter(adapter);

        getBinding().fabAdd.setOnClickListener(v -> startActivityForResult(CampusPartnerPublishActivity.newInstance(this), REQUEST_PUBLISH));
        getBinding().tvMyPublish.setOnClickListener(v -> startActivity(CampusPartnerMyPublishActivity.newInstance(this)));
        getBinding().tvProfile.setOnClickListener(v -> startActivity(CampusPartnerProfileActivity.newInstance(this)));
        getBinding().tvNotification.setOnClickListener(v -> startActivity(CampusPartnerNotificationActivity.newInstance(this)));

        initFilters();
        loadData();
    }

    private void initFilters() {
        ArrayAdapter<String> campusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, campuses);
        campusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        getBinding().spinnerCampus.setAdapter(campusAdapter);
        getBinding().spinnerCampus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCampus = position == 0 ? null : campuses[position];
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ArrayAdapter<String> collegeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, colleges);
        collegeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        getBinding().spinnerCollege.setAdapter(collegeAdapter);
        getBinding().spinnerCollege.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCollege = position == 0 ? null : colleges[position];
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        getBinding().spinnerType.setAdapter(typeAdapter);
        getBinding().spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedType = position == 0 ? null : types[position];
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        getBinding().ivSearch.setOnClickListener(v -> {
            searchText = getBinding().etSearch.getText().toString().trim();
            applyFilters();
        });

        getBinding().etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                searchText = getBinding().etSearch.getText().toString().trim();
                applyFilters();
                return true;
            }
            return false;
        });
    }

    private void applyFilters() {
        List<CampusMateActivity> filtered = new ArrayList<>();
        String searchLower = searchText == null ? "" : searchText.toLowerCase();

        for (CampusMateActivity act : allActivities) {
            boolean matchCampus = selectedCampus == null || (act.campus != null && act.campus.contains(selectedCampus));
            boolean matchCollege = selectedCollege == null || (act.college != null && act.college.equals(selectedCollege));
            boolean matchType = selectedType == null || (act.type != null && act.type.equals(selectedType));
            boolean matchSearch = searchText.isEmpty()
                    || (act.title != null && act.title.toLowerCase().contains(searchLower))
                    || (act.description != null && act.description.toLowerCase().contains(searchLower))
                    || (act.location != null && act.location.toLowerCase().contains(searchLower))
                    || (act.type != null && act.type.toLowerCase().contains(searchLower));

            if (matchCampus && matchCollege && matchType && matchSearch) {
                filtered.add(act);
            }
        }
        adapter.setNewData(filtered);
    }

    private void loadData() {
        NewApiHelper.getCampusMateActivities(null, null, null, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                CampusMateActivityListData data = (CampusMateActivityListData) responseObj;
                if (data.isSuccess()) {
                    allActivities = data.getData() == null ? new ArrayList<>() : data.getData();
                    runOnUiThread(() -> applyFilters());
                } else {
                    runOnUiThread(() -> Toast.makeText(CampusPartnerActivity.this, "获取失败: " + data.getMsg(), Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onFailure(Object reasonObj) {
                runOnUiThread(() -> Toast.makeText(CampusPartnerActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PUBLISH && resultCode == RESULT_OK) {
            loadData();
        }
    }

    static class PartnerAdapter extends BaseQuickAdapter<CampusMateActivity, BaseViewHolder> {
        public PartnerAdapter(List<CampusMateActivity> data) {
            super(R.layout.item_campus_partner, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, CampusMateActivity item) {
            helper.setText(R.id.tv_title, item.title);
            helper.setText(R.id.tv_desc, item.description);
            helper.setText(R.id.tv_location, item.location != null ? item.location : item.campus);
            helper.setText(R.id.tv_people, item.minPeople + "-" + item.maxPeople + "人");

            String timeStr = item.createdAt;
            if (timeStr != null && timeStr.length() > 10) {
                timeStr = timeStr.substring(0, 10);
            }
            helper.setText(R.id.tv_time, timeStr);

            TextView tvType = helper.getView(R.id.tv_type);
            tvType.setText(item.type);
            tvType.getBackground().setTint(Color.parseColor("#03A9F4"));
        }
    }
}
