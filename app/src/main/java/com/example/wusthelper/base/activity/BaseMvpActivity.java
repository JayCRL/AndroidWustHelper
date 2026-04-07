package com.example.wusthelper.base.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

import com.example.wusthelper.base.BasePresenter;
import com.example.wusthelper.base.BaseMvpView;


/**
 * 第二层BaseMvpActivity封装,仅封装了与Mvp框架相关的内容
 * 需要在BaseMvpActivity<>中传入一个继承自BaseView的子类View
 * 一个继承自BasePresenter的子类Presenter
 * 还需要一个ViewBinding的子类
 * 子类通过getPresenter()来在相应的Activity获取相应的Presenter
 * 子类可以通过getBinding()来获取 相应ViewBinding对象
 * @param <V>
 * @param <P>
 * @param <VB> */
//抽象出解绑和绑定操作
//注意，为了能够兼容多个模块，兼容多个Activity，所以采用泛型设计
public abstract class BaseMvpActivity<V extends BaseMvpView,P extends BasePresenter<V>,VB extends ViewBinding>
        extends BaseActivity<VB> {

    private static final String TAG = "BaseMvpActivity";
    private P presenter;
    private V view;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if( this.presenter == null){
            this.presenter = createPresenter();
        }
        if( this.view == null){
            this.view = createView();
        }
        if(this.presenter != null && this.view != null){
            this.presenter.attachView(this.view);
            //初始化presenter的数据
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(this.presenter != null && this.view != null ){
            this.presenter.deleteView();
        }
    }

    public abstract P createPresenter();
    public abstract V createView();
    public P getPresenter(){
        if(presenter != null){
            return presenter;
        }else {
            return createPresenter();
        }

    }

}
