package com.example.wusthelper.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.example.wusthelper.R;
import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.databinding.ActivitySearchClassRoomBinding;
import com.example.wusthelper.utils.ToastUtil;

public class SearchClassRoomActivity extends BaseActivity<ActivitySearchClassRoomBinding> implements View.OnClickListener {

    public static Intent newInstance(Context context) {
        return new Intent(context, SearchClassRoomActivity.class);
    }

    @Override
    public void initView() {
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        getBinding().ivBack.setOnClickListener(this);
        getBinding().searchRoom.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(getBinding().ivBack)) {
            finish();
            return;
        }
        String week = getValue(getBinding().etWeek);
        String weekDay = getValue(getBinding().etWeekday);
        String section = getValue(getBinding().etSection);
        if (week.isEmpty() || weekDay.isEmpty() || section.isEmpty()) {
            ToastUtil.show("请完整选择周次、星期和节次");
            return;
        }
        startActivity(SearchRoomResultActivity.newInstance(this, week, weekDay, section));
    }

    private String getValue(android.widget.TextView textView) {
        return textView.getText() == null ? "" : textView.getText().toString().trim();
    }
}
