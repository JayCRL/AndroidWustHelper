package com.example.wusthelper.mvp.view;

import com.example.wusthelper.base.BaseMvpView;
import com.example.wusthelper.bean.javabean.ConfigBean;

public interface LoginView extends BaseMvpView {

    void onToastShow(String msg);
    void onLoadingShow(String msg,boolean cancelable);
    void onLoadingCancel();
    void onFinish();
    void openMainActivity();

    void showUpdateDialog(ConfigBean bean);

    void showErrorDialog();
}
