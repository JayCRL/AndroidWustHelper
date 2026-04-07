package com.example.wusthelper.mvp.model;

import com.example.wusthelper.request.NewApiHelper;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;

public class HomePageModel {

    public void getCycleImageDataFormNet(DisposeDataListener listener) {
        NewApiHelper.getCarousels(listener);
    }

    public void getAndroidNotice(DisposeDataListener listener) {
        NewApiHelper.getAndroidNotices(listener);
    }

    public void postLoginPhysical(String password,DisposeDataListener listener) {
        NewApiHelper.postLoginPhysical(password,listener);
    }

    public void getPhysicalCourse(DisposeDataListener listener) {
        NewApiHelper.getPhysicalCourse(listener);
    }

}
