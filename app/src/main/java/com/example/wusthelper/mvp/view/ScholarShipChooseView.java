package com.example.wusthelper.mvp.view;

import com.example.wusthelper.base.BaseMvpView;

public interface ScholarShipChooseView extends BaseMvpView {
    void ShowColorSnackBar();

    void onGradeListShow();

    void setScholarshipChooseButton(boolean msg);

    void setScholarshipChooseAllImage(boolean msg);
}
