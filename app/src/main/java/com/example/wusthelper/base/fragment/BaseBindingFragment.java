package com.example.wusthelper.base.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

import com.dylanc.viewbinding.base.ViewBindingUtil;
import com.example.wusthelper.bindeventbus.BindEventBus;
import com.example.wusthelper.bindeventbus.EventBusUtils;

/**
 * fragment第一次封装，主要是封装了ViewBinding绑定
 * 还简单封装了ViewPager+fragment下的懒加载
 * 懒加载的内容写在lazyLoad()中
 * 子类需要传入一个ViewBinding子类
 *  * //封装方法2.0 在onCreate() onDestroy()方法添加了相应代码
 *  * //封装方法2.0 实现了 EventBus的简单封装 具体使用参照：https://blog.csdn.net/u014619545/article/details/90512143
 *  @param <VB>
 *      */
public abstract class BaseBindingFragment<VB extends ViewBinding> extends Fragment {

    private static final String TAG = "BaseBindingFragment";

    private VB binding;

    //判断是否已进行过加载，避免重复加载
    private boolean isLoad = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ViewBindingUtil.inflateWithGeneric(this, getLayoutInflater(), container, false);

        // 仅对 WebView 相关页面打关键生命周期日志，方便定位“页面没启动/没触发回调”问题
        try {
            String name = getClass().getName();
            if (name.endsWith("VolunteerFragment") || name.endsWith("ConsultFragment")) {
                Log.e("WustWebDebug", "onCreateView " + name);
            }
        } catch (Exception ignore) {
        }

        initView();
        return binding.getRoot();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        try {
            String name = getClass().getName();
            if (name.endsWith("VolunteerFragment") || name.endsWith("ConsultFragment")) {
                Log.e("WustWebDebug", "onDestroyView " + name);
            }
        } catch (Exception ignore) {
        }

        binding = null;
        //懒加载
        isLoad=false;
    }

    public VB getBinding() {
        return binding;
    }

    public abstract void initView();

    @Override
    public void onResume() {
        super.onResume();

        try {
            String name = getClass().getName();
            if (name.endsWith("VolunteerFragment") || name.endsWith("ConsultFragment")) {
                Log.e("WustWebDebug", "onResume " + name + " isLoad=" + isLoad);
            }
        } catch (Exception ignore) {
        }

        if (!isLoad) {
            isLoad = true;
            lazyLoad();
        }
    }

    protected void lazyLoad() {
        //懒加载。。。
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
}