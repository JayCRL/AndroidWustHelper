package com.example.wusthelper.ui.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.viewbinding.ViewBinding;

import com.dylanc.viewbinding.base.ViewBindingUtil;
import com.example.wusthelper.bindeventbus.BindEventBus;
import com.example.wusthelper.bindeventbus.EventBusUtils;
import com.example.wusthelper.utils.ScreenUtils;

/**
 * 第一次DialogFragment的封装
 * 将ViewBinding的绑定逻辑放在该基类
 * 需要子类提供一个ViewBinding的子类
 * 子类需要实现onCreateViewBinding()才能进行绑定
 * onCreateViewBinding()方法的return必须是一个继承自ViewBinding的对象
 * 比如 return ViewBinding.inflate(inflater, parent, false);
 * 子类可以使用getBinding()来获取ViewBinding
 * 子类需要通过接口的形式来与外界交互
 * 接口实现可以在子类的onActivityCreated()方法中
 * 具体可以查一下Dialog的生命周期
 * //封装方法2.0 在onCreate() onDestroy()方法添加了相应代码
 * //封装方法2.0 实现了 EventBus的简单封装 具体使用参照：https://blog.csdn.net/u014619545/article/details/90512143
 * @param <VB> */
public abstract class BaseDialogFragment<VB extends ViewBinding> extends DialogFragment {

    private LayoutInflater inflater;
    private VB binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ViewBindingUtil.inflateWithGeneric(this, getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public VB getBinding() {
        return binding;
    }

    @Override
    public void onResume() {
        super.onResume( );
        Window window = getDialog().getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = ScreenUtils.dp2px(320);
        window.setAttributes(layoutParams);
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss( dialog );
    }


    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 若使用BindEventBus注解，则绑定EventBus
        if(this.getClass().isAnnotationPresent(BindEventBus.class)){
            EventBusUtils.register(this); //必需要先注册
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 若使用BindEventBus注解，则解绑定EventBus
        if(this.getClass().isAnnotationPresent(BindEventBus.class)){
            EventBusUtils.unregister(this);//必须要解除注册，防止内存泄漏
        }
    }


    @Override
    public void onActivityCreated(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        //设置监听器，用于调用接口
        initView();
        initListener();
        super.onActivityCreated(savedInstanceState);
    }

    public abstract void initView();
    public abstract void initListener();
}
