package com.example.wusthelper.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.example.wusthelper.R;
import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.bean.javabean.data.CatPost;
import com.example.wusthelper.bean.javabean.data.CatTag;
import com.example.wusthelper.databinding.ActivityCampusCatBinding;
import com.example.wusthelper.request.CampusCatDataProvider;

import java.util.ArrayList;
import java.util.List;

public class CampusCatActivity extends BaseActivity<ActivityCampusCatBinding> {

    private CatAdapter adapter;
    private List<CatPost> allCats;
    private String searchText = "";
    private String selectedTag = CatTag.ALL;

    private String[] tags = {CatTag.ALL, CatTag.STRAY, CatTag.ADOPT, CatTag.DAILY, CatTag.HELP, CatTag.SHOW_OFF};

    public static Intent newInstance(Context context) {
        return new Intent(context, CampusCatActivity.class);
    }

    @Override
    public void initView() {
        getBinding().tbTitle.tvTitleTitle.setText("校园猫");
        getBinding().tbTitle.ivTitleBack.setOnClickListener(v -> finish());

        getBinding().rvCat.setLayoutManager(new GridLayoutManager(this, 2));
        allCats = CampusCatDataProvider.getPosts();
        adapter = new CatAdapter(new ArrayList<>());
        getBinding().rvCat.setAdapter(adapter);

        getBinding().fabAdd.setOnClickListener(v -> Toast.makeText(this, "发布功能开发中...", Toast.LENGTH_SHORT).show());

        initFilters();
        applyFilters();
    }

    private void initFilters() {
        for (String tag : tags) {
            TextView tagView = new TextView(this);
            tagView.setText(tag);
            tagView.setPadding(30, 10, 30, 10);
            tagView.setBackgroundResource(R.drawable.shape_search_bg);
            tagView.setTextColor(Color.GRAY);
            tagView.setTextSize(14f);

            android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 20, 0);
            tagView.setLayoutParams(params);

            tagView.setOnClickListener(v -> {
                selectedTag = tag;
                for (int i = 0; i < getBinding().llTags.getChildCount(); i++) {
                    TextView child = (TextView) getBinding().llTags.getChildAt(i);
                    if (child.getText().toString().equals(tag)) {
                        child.setTextColor(getResources().getColor(R.color.colorPrimary));
                        child.setBackgroundResource(R.drawable.shape_tag_bg);
                    } else {
                        child.setTextColor(Color.GRAY);
                        child.setBackgroundResource(R.drawable.shape_search_bg);
                    }
                }
                applyFilters();
            });

            getBinding().llTags.addView(tagView);
            if (tag.equals(selectedTag)) {
                tagView.setTextColor(getResources().getColor(R.color.colorPrimary));
                tagView.setBackgroundResource(R.drawable.shape_tag_bg);
            }
        }

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
        List<CatPost> filtered = new ArrayList<>();
        String searchLower = searchText.toLowerCase();

        for (CatPost cat : allCats) {
            boolean matchTag = selectedTag.equals(CatTag.ALL) || (cat.tags != null && cat.tags.contains(selectedTag));
            boolean matchSearch = searchText.isEmpty()
                    || (cat.title != null && cat.title.toLowerCase().contains(searchLower))
                    || (cat.content != null && cat.content.toLowerCase().contains(searchLower));

            if (matchTag && matchSearch) {
                filtered.add(cat);
            }
        }
        adapter.setNewData(filtered);
    }

    static class CatAdapter extends BaseQuickAdapter<CatPost, BaseViewHolder> {
        public CatAdapter(List<CatPost> data) {
            super(R.layout.item_cat, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, CatPost item) {
            helper.setText(R.id.tv_cat_name, item.title);
            helper.setText(R.id.tv_cat_desc, item.content);
        }
    }
}
