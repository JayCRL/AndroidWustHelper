package com.example.wusthelper.mvp.model;

import com.example.wusthelper.request.NewApiHelper;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;

public class CreditsStatisticsModel {
    public void getCreditFromNet(DisposeDataListener listener) {
        NewApiHelper.getCredit(listener);
    }

    public void getSchemeFromNet(DisposeDataListener listener) {
        NewApiHelper.getScheme(listener);
    }
}
