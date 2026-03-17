package com.example.wusthelper.base.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;

import com.example.wusthelper.base.BasePresenter;
import com.example.wusthelper.base.BaseMvpView;

import org.jetbrains.annotations.NotNull;

/**封装原理类似于BaseMvpActivity
 * @param <V>
 *     @param <P>
 *         @param <VB> */
public abstract class BaseMvpFragment<V extends BaseMvpView,P extends BasePresenter<V>,VB extends ViewBinding>
        extends BaseBindingFragment<VB> {

    private static final String TAG = "BaseMvpActivity";
    private P presenter;
    private V view;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
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
        return super.onCreateView(inflater, container, savedInstanceState);
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
