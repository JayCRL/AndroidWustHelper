package com.example.wusthelper.mvp.view;

import com.example.wusthelper.base.BaseMvpView;

public interface CountdownView extends BaseMvpView {

    //刷新页面  （空集）
    void emptyResult();

    //刷新页面  （不空）
    void refreshData();

    //取消dialog
    void dialogDismiss();
}
