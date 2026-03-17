package com.example.wusthelper.base;

/**
 * MVP框架，Presenter的封装
 * 实现了 p层和 v层的绑定
 * */
public abstract class BasePresenter<V extends BaseMvpView> {

    private V view;
    public V getView() { return view; }
    public void attachView(V view){ this.view =view; }
    public void deleteView(){
        this.view = null;
        //终止请求
    }
    public abstract void initPresenterData();
}
