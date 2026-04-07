package com.example.wusthelper.base.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

import com.dylanc.viewbinding.base.ViewBindingUtil;
import com.example.wusthelper.bindeventbus.BindEventBus;
import com.example.wusthelper.bindeventbus.EventBusUtils;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.request.NewApiHelper;


/**
 * 第一层BaseActivity封装，封装了一些常用方法，还封装了ViewBinding
 * 继承的类需要传入相应的ViewBinding类
 * ViewBinding封装思路来自 “https://blog.csdn.net/c10WTiybQ1Ye3/article/details/112690188”
 * (采用了反射，在进行代码混淆的时候要加上一些代码，否则会出问题)
 * 子类可以通过getBinding()来获取 相应ViewBinding对象
 * 抽象了两个个方法，子类必须实现两个个方法
 * //封装方法2.0 在onCreate() onDestroy()方法添加了相应代码
 * //封装方法2.0 实现了 EventBus的简单封装 具体使用参照：https://blog.csdn.net/u014619545/article/details/90512143
 * initView()       //完成一些视图的初始化
 * @param <VB>
 * */
public abstract class BaseActivity<VB extends ViewBinding> extends AppCompatActivity {

    private static final String TAG = "BaseActivity";

    private VB binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        //这个方法很牛逼，一行代码解决问题,调的依赖，原理是反射
        binding = ViewBindingUtil.inflateWithGeneric(this, getLayoutInflater());
        setContentView(binding.getRoot());
        setFullScreen();
        initView();

        // 若使用BindEventBus注解，则绑定EventBus
        if(this.getClass().isAnnotationPresent(BindEventBus.class)){
            EventBusUtils.register(this); //必需要先注册
        }

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 若使用BindEventBus注解，则解绑定EventBus
        if(this.getClass().isAnnotationPresent(BindEventBus.class)){
            EventBusUtils.unregister(this);//必须要解除注册，防止内存泄漏
        }
    }

    public VB getBinding() {
        return binding;
    }


    public abstract void initView();

    private void setFullScreen() {

        View view = getWindow().getDecorView();
        int options = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        view.setSystemUiVisibility(options);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor( Color.TRANSPARENT);
        }
        // 竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        //默认为设置白底黑字,可在activity中修改
        changeStatusBarTextColor( true );
    }

    /**
     * 状态栏白色就设置成深色字体(传入true)
     * 状态栏深色就设置成白色字体(传入false)
     * @param isBlack
     */
    @TargetApi(Build.VERSION_CODES.M)
    public void changeStatusBarTextColor(@NonNull boolean isBlack){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isBlack) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//设置状态栏黑色字体
            }else {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);//恢复状态栏白色字体
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart( );
        String token = SharePreferenceLab.getToken();
        String msg = SharePreferenceLab.getInstance().getMessage(this);
        if (!token.equals("")){
            NewApiHelper.setToken(token);
            NewApiHelper.setMessage(msg);
        }
    }
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier(
                "status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
