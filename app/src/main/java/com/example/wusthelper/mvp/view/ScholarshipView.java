package com.example.wusthelper.mvp.view;

import com.example.wusthelper.base.BaseMvpView;

public interface ScholarshipView extends BaseMvpView {
    void ShowColorSnackBar();

    void onGradeListShow();

    void setScholarshipGrade();

    void setStartAnim();
}
