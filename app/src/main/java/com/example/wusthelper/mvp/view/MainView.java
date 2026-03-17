package com.example.wusthelper.mvp.view;

import com.example.wusthelper.base.BaseMvpView;
import com.example.wusthelper.bean.javabean.ConfigBean;
import com.example.wusthelper.bean.javabean.LostBean;
import com.example.wusthelper.bean.javabean.NoticeBean;

import java.util.List;

public interface MainView extends BaseMvpView {
    void showNoticeDialog(List<NoticeBean> list);

    void showUpdateDialog(ConfigBean bean);

    void showLostDialog(LostBean bean);
}
