package com.example.wusthelper.mvp.view;

import com.example.wusthelper.base.BaseMvpView;

public interface LibraryLoginView extends BaseMvpView {

    void onToastShow(String msg);
    void onLoadingShow(String msg,boolean cancelable);
    void onLoadingCancel();
    void openMainActivity();
}
