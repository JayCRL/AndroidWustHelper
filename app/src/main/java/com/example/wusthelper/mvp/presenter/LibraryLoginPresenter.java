package com.example.wusthelper.mvp.presenter;

import com.example.wusthelper.base.BasePresenter;
import com.example.wusthelper.bean.javabean.data.BaseData;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.mvp.model.LibraryLoginModel;
import com.example.wusthelper.mvp.view.LibraryLoginView;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;

public class LibraryLoginPresenter extends BasePresenter<LibraryLoginView> {

    private String password;

    private final LibraryLoginModel libraryLoginModel;

    public LibraryLoginPresenter(){
        this.libraryLoginModel = new LibraryLoginModel();
    }

    @Override
    public void initPresenterData() {

    }

    public void login(String password) {
        getView().onLoadingShow("正在登录图书馆...",false);
        this.password = password;
        this.libraryLoginModel.login(password, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                BaseData data = (BaseData) responseObj;
                if(data.getCode().equals("10000") || data.getCode().equals("11000")){
                    SharePreferenceLab.setIsLibraryLogin(true);
                    getView().onLoadingCancel();
                    getView().openMainActivity();
                }else{
                    getView().onLoadingCancel();
                    getView().onToastShow(data.getMsg());
                }
            }

            @Override
            public void onFailure(Object reasonObj) {
                getView().onLoadingCancel();
                getView().onToastShow("请求失败，可能是网络未链接或请求超时");
            }
        });
    }
}
