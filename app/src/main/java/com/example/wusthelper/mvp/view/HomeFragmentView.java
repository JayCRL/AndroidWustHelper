package com.example.wusthelper.mvp.view;

import android.graphics.Bitmap;

import com.example.wusthelper.base.BaseMvpView;
import com.example.wusthelper.bean.javabean.CycleImageBean;

import java.util.List;

public interface HomeFragmentView extends BaseMvpView {

    void showCycleImageFromNet(List<CycleImageBean> data);

    void showCycleImageFromLocal(List<Bitmap> data);

    void showHomeNotice(String text);

    void hideHomeNotice();

    void showPhysicalLoginDialog();

    void showLoadDialog();

    void cancelLoadDialog();

    void startPhysicalDetailActivity();
}
