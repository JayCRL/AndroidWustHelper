package com.example.wusthelper.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.example.wusthelper.R;
import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.bean.javabean.SearchCourseFilterBean;
import com.example.wusthelper.bean.javabean.data.SearchCourseFilterData;
import com.example.wusthelper.databinding.ActivitySearchCourseBinding;
import com.example.wusthelper.request.NewApiHelper;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;
import com.example.wusthelper.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

public class SearchCourseActivity extends BaseActivity<ActivitySearchCourseBinding> {

    private final List<SearchCourseFilterBean> courseList = new ArrayList<>();
    private CourseSearchAdapter adapter;

    public static Intent newInstance(Context context){
        return new Intent(context,SearchCourseActivity.class);
    }

    @Override
    public void initView() {
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        getBinding().ivBack.setOnClickListener(v -> finish());
        getBinding().toolbar.setText("课程搜索");
        getBinding().searchCourseRecycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CourseSearchAdapter(courseList);
        getBinding().searchCourseRecycler.setAdapter(adapter);
        getBinding().btnSearch.setOnClickListener(v -> search());
        showEmpty("请输入条件后开始搜索");
    }

    private void search() {
        String courseName = textOf(getBinding().etCourseName);
        String teacherName = textOf(getBinding().etTeacherName);
        String classroom = textOf(getBinding().etClassroom);
        String weekDay = textOf(getBinding().etWeekday);
        String section = textOf(getBinding().etSection);
        if (TextUtils.isEmpty(courseName) && TextUtils.isEmpty(teacherName) && TextUtils.isEmpty(classroom)
                && TextUtils.isEmpty(weekDay) && TextUtils.isEmpty(section)) {
            ToastUtil.show("请至少填写一个筛选条件");
            return;
        }
        getBinding().progressBar.setVisibility(View.VISIBLE);
        getBinding().tvEmpty.setVisibility(View.GONE);
        NewApiHelper.searchCourses(courseName, teacherName, classroom, weekDay, section, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                getBinding().progressBar.setVisibility(View.GONE);
                SearchCourseFilterData data = (SearchCourseFilterData) responseObj;
                if ("401".equals(data.getCode())) {
                    handleUnauthorized(data.getMsg());
                    return;
                }
                if (data.isSuccess()) {
                    courseList.clear();
                    if (data.getData() != null) {
                        courseList.addAll(data.getData());
                    }
                    adapter.notifyDataSetChanged();
                    if (courseList.isEmpty()) {
                        showEmpty("暂无匹配课程");
                    } else {
                        getBinding().tvEmpty.setVisibility(View.GONE);
                    }
                } else {
                    showEmpty("课程搜索失败，请稍后再试");
                    ToastUtil.show(getMessage(data.getMsg(), "课程搜索失败"));
                }
            }

            @Override
            public void onFailure(Object reasonObj) {
                getBinding().progressBar.setVisibility(View.GONE);
                showEmpty("请求失败，请检查网络后重试");
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

    private void showEmpty(String text) {
        getBinding().tvEmpty.setText(text);
        getBinding().tvEmpty.setVisibility(View.VISIBLE);
    }

    private String textOf(TextView textView) {
        return textView.getText() == null ? "" : textView.getText().toString().trim();
    }

    private String getMessage(String msg, String fallback) {
        return msg == null || msg.trim().isEmpty() ? fallback : msg;
    }

    static class CourseSearchAdapter extends BaseQuickAdapter<SearchCourseFilterBean, BaseViewHolder> {
        CourseSearchAdapter(List<SearchCourseFilterBean> data) {
            super(android.R.layout.simple_list_item_2, data);
        }

        @Override
        protected void convert(BaseViewHolder helper, SearchCourseFilterBean item) {
            helper.setText(android.R.id.text1, item.getCourseName());
            String detail = (item.getTeacherName() == null ? "" : item.getTeacherName())
                    + "  " + (item.getClassroom() == null ? "" : item.getClassroom())
                    + "  周" + (item.getWeekDay() == null ? "-" : item.getWeekDay())
                    + " 第" + (item.getStartSection() == null ? "-" : item.getStartSection()) + "节";
            helper.setText(android.R.id.text2, detail.trim());
        }
    }
}
