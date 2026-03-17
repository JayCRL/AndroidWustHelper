package com.example.wusthelper.ui.activity;

import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.GridLayoutManager;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.example.wusthelper.R;
import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.databinding.ActivityCampusCatBinding;
import java.util.ArrayList;
import java.util.List;

public class CampusCatActivity extends BaseActivity<ActivityCampusCatBinding> {

    public static Intent newInstance(Context context) {
        return new Intent(context, CampusCatActivity.class);
    }

    @Override
    public void initView() {
        getBinding().tbTitle.tvTitleTitle.setText("校园猫");
        getBinding().tbTitle.ivTitleBack.setOnClickListener(v -> finish());

        getBinding().rvCat.setLayoutManager(new GridLayoutManager(this, 2));
        CatAdapter adapter = new CatAdapter(getMockData());
        getBinding().rvCat.setAdapter(adapter);
    }

    private List<CatBean> getMockData() {
        List<CatBean> list = new ArrayList<>();
        list.add(new CatBean("大黄", "出没：北苑食堂"));
        list.add(new CatBean("小白", "出没：图书馆"));
        list.add(new CatBean("小黑", "出没：教三楼"));
        list.add(new CatBean("花花", "出没：南苑宿舍"));
        list.add(new CatBean("胖虎", "出没：操场"));
        list.add(new CatBean("五花肉", "出没：东门"));
        return list;
    }

    static class CatBean {
        String name;
        String desc;
        public CatBean(String name, String desc) { this.name = name; this.desc = desc; }
    }

    static class CatAdapter extends BaseQuickAdapter<CatBean, BaseViewHolder> {
        public CatAdapter(List<CatBean> data) {
            super(R.layout.item_cat, data);
        }
        @Override
        protected void convert(BaseViewHolder helper, CatBean item) {
            helper.setText(R.id.tv_cat_name, item.name);
            helper.setText(R.id.tv_cat_desc, item.desc);
        }
    }
}