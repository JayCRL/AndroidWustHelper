package com.example.wusthelper.mvp.view;

import com.example.wusthelper.base.BaseMvpView;
import com.example.wusthelper.bean.javabean.GradeBean;
import com.example.wusthelper.bean.javabean.GraduateGradeBean;

import java.util.List;

public interface GradeView extends BaseMvpView {

    void onGradeListShow(List<GradeBean> list);
    void onGraduateListShow(List<GraduateGradeBean> list);

    void showLoading(String msg);

    void cancelDialog();

    void showSuccessColorSnackBar(String msg);

    void showFailureColorSnackBar(String msg);

    void showListDialog();
}
