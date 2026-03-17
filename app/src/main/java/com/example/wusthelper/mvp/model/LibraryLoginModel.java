package com.example.wusthelper.mvp.model;

import com.example.wusthelper.request.NewApiHelper;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;

public class LibraryLoginModel {

    public void login(String password, DisposeDataListener listener){
        NewApiHelper.postLoginLibrary(password,listener);
    }
}
