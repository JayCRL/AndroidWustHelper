package com.example.wusthelper.mvp.view;

import android.content.Context;
import com.example.wusthelper.base.BaseMvpView;

public interface CreditsStatisticsView extends BaseMvpView {
    /**
     * 显示学分html
     * */
    void showCreditsHtml(String htmlText);
    /**
     * 显示培养方案html
     * */
    void showSchemeHtml(String htmlText);
    /**
     * 显示底部提示信息
     * */
    void showSnackBar(String msg);
    /**
     * 取消加载
     * */
    void cancelDialog();

    Context getContext();
}
