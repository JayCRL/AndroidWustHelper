package com.example.wusthelper.ui.dialog;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.wusthelper.R;
import com.example.wusthelper.bean.javabean.CourseBean;
import com.example.wusthelper.databinding.DialogDeleteCourseBinding;
import com.example.wusthelper.dbhelper.CourseDB;
import com.example.wusthelper.ui.activity.EditActivity;

import org.litepal.LitePal;

public class CourseDeleteDialog extends BaseDialogFragment<DialogDeleteCourseBinding>{
    private CourseBean mCourseBean;

    public CourseDeleteDialog (CourseBean courseBean) {
        mCourseBean = courseBean;
    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        setOnClickListener();
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void initView() {

    }

    @Override
    public void initListener() {

    }

    private void setOnClickListener () {
        getBinding().btnDialogDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LitePal.deleteAll(CourseBean.class,"courseName = ? and isDefault = ?",mCourseBean.getCourseName(),"1");
                dismiss();
                EditActivity.instance.finish();
            }
        });
        getBinding().btnDialogDeleteCurrent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CourseDB.deleteOneCourse(mCourseBean.getId());
                dismiss();
                EditActivity.instance.finish();
            }
        });
    }
}
