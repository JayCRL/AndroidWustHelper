package com.example.wusthelper.ui.activity;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.example.wusthelper.R;
import com.example.wusthelper.adapter.ViewPagerAdapter;
import com.example.wusthelper.base.activity.BaseMvpActivity;
import com.example.wusthelper.bean.javabean.ConfigBean;
import com.example.wusthelper.bean.javabean.LostBean;
import com.example.wusthelper.databinding.ActivityMainBinding;
import com.example.wusthelper.bean.javabean.NoticeBean;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.mvp.presenter.MainPresenter;
import com.example.wusthelper.mvp.view.MainView;
import com.example.wusthelper.ui.dialog.CourseDialogNew;
import com.example.wusthelper.ui.dialog.LostDialog;
import com.example.wusthelper.ui.dialog.NoticeDialog;
import com.example.wusthelper.ui.dialog.PermissionDialog;
import com.example.wusthelper.ui.dialog.UpdateDialog;
import com.example.wusthelper.utils.ToastUtil;
import com.example.wusthelper.ui.fragment.mainviewpager.ConsultFragment;
import com.example.wusthelper.ui.fragment.mainviewpager.CourseFragment;
import com.example.wusthelper.ui.fragment.mainviewpager.HomeFragment;
import com.example.wusthelper.ui.fragment.mainviewpager.MineFragment;
import com.example.wusthelper.ui.fragment.mainviewpager.VolunteerFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainMvpActivity extends BaseMvpActivity<MainView, MainPresenter, ActivityMainBinding>
        implements MainView, CourseFragment.CallBackListener {

    private static final String TAG = "MainMvpActivity";

    //从每日课表小组件跳转到添加页面，就传入WIDGET
    public static final int WIDGET = 10;
    public static final int APP = 11;

    public ViewPagerAdapter viewPagerAdapter;
    private MenuItem menuItem;
    private List<Fragment> list;

    /**
     * UI
     * */
    private HomeFragment homeFragment;
    private VolunteerFragment volunteerFragment;
    private CourseFragment courseFragment;
    private ConsultFragment consultFragment;
    private MineFragment mineFragment ;

    private NoticeDialog noticeDialog;

    public static Intent newInstance(Context context) {
        return new Intent(context, MainMvpActivity.class);
    }

    @Override
    public MainPresenter createPresenter() {
        return new MainPresenter();
    }

    @Override
    public MainView createView() {
        return this;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void initView() {

        getPresenter().initPresenterData();

        //把状态栏隐藏了
        Window window = getWindow();
//
        //View.SYSTEM_UI_FLAG_LAYOUT_STABLE 跟随系统 View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR 设置为黑色
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // Translucent status bar
//        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
//                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        initFragment();
        initRequest();

        if(SharePreferenceLab.getBarHide()) {
            getBinding().navigation.setVisibility(View.GONE);
        }
        // 让item的图标显示原色，否则会填充纯色
        getBinding().navigation.setItemIconTintList(null);

        getBinding().navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        getBinding().viewPager.setScrollable(false);

        initIntentExtra();
    }

    /**
     * 进行MainActivity的初始网络请求*/
    private void initRequest() {
        getPresenter().getNotice();//显示公告
        getPresenter().getLost();//显示失物招领
        getPresenter().getConfig();
    }

    private void initFragment() {


        FragmentManager fragmentManager = getSupportFragmentManager();
        viewPagerAdapter = new ViewPagerAdapter(fragmentManager);
        getBinding().viewPager.setAdapter(viewPagerAdapter);

        list = new ArrayList<>();
        homeFragment        =   HomeFragment.newInstance();
        volunteerFragment   =   VolunteerFragment.newInstance();
        courseFragment      =   CourseFragment.newInstance();
        consultFragment     =   ConsultFragment.newInstance();
        mineFragment        =   MineFragment.newInstance();

        Log.e("WustWebDebug", "initFragment created volunteer=" + volunteerFragment + " consult=" + consultFragment);

        homeFragment.setHeight(getStatusBarHeight(MainMvpActivity.this));
        courseFragment.setHeight(getStatusBarHeight(MainMvpActivity.this));
        mineFragment.setHeight(getStatusBarHeight(MainMvpActivity.this));
        volunteerFragment.setHeight(getStatusBarHeight(MainMvpActivity.this));
        consultFragment.setHeight(getStatusBarHeight(MainMvpActivity.this));

        Log.e(TAG,"SharePreferenceLab.getBarState() = " + SharePreferenceLab.getBarState());
        if(SharePreferenceLab.getBarState() == SharePreferenceLab.BARSTATEDEFAULT) {
            list.add(homeFragment);
            if(SharePreferenceLab.getIsGraduate()){
                getBinding().navigation.getMenu().removeItem(R.id.navigation_volunteering);
            }else {
                list.add(volunteerFragment);
            }
            list.add(courseFragment);
            list.add(consultFragment);
            list.add(mineFragment);
            viewPagerAdapter.setList(list);
            viewPagerAdapter.notifyDataSetChanged();
            getBinding().navigation.setVisibility(View.VISIBLE);
        }else if(SharePreferenceLab.getBarState() == SharePreferenceLab.BARSTATEDELETEVOL){
            list.add(homeFragment);
            getBinding().navigation.getMenu().removeItem(R.id.navigation_volunteering);
            list.add(courseFragment);
            list.add(consultFragment);
            list.add(mineFragment);
            viewPagerAdapter.setList(list);
            viewPagerAdapter.notifyDataSetChanged();
            getBinding().navigation.setVisibility(View.VISIBLE);
        }else if (SharePreferenceLab.getBarState() == SharePreferenceLab.BARSTATEDELETECON) {
            list.add(homeFragment);
            if(SharePreferenceLab.getIsGraduate()){
                getBinding().navigation.getMenu().removeItem(R.id.navigation_volunteering);
            }else {
                list.add(volunteerFragment);
            }
            getBinding().navigation.getMenu().removeItem(R.id.navigation_consult);
            list.add(courseFragment);
            list.add(mineFragment);
            viewPagerAdapter.setList(list);
            viewPagerAdapter.notifyDataSetChanged();
            getBinding().navigation.setVisibility(View.VISIBLE);
        }else if (SharePreferenceLab.getBarState() == SharePreferenceLab.BARSTATEDELETETOW) {
            list.add(homeFragment);
            getBinding().navigation.getMenu().removeItem(R.id.navigation_volunteering);
            getBinding().navigation.getMenu().removeItem(R.id.navigation_consult);
            list.add(courseFragment);
            list.add(mineFragment);
            viewPagerAdapter.setList(list);
            viewPagerAdapter.notifyDataSetChanged();
            getBinding().navigation.setVisibility(View.VISIBLE);
        }else if (SharePreferenceLab.getBarState() == SharePreferenceLab.BARSTATEDELETEALL) {
            list.add(homeFragment);
            if(SharePreferenceLab.getIsGraduate()){
                getBinding().navigation.getMenu().removeItem(R.id.navigation_volunteering);
            }else {
                list.add(volunteerFragment);
            }
            list.add(courseFragment);
            list.add(consultFragment);
            list.add(mineFragment);
            viewPagerAdapter.setList(list);
            viewPagerAdapter.notifyDataSetChanged();
            getBinding().navigation.setVisibility(View.GONE);
        }

        Log.e(TAG,"list" +list.toString());
        Log.e(TAG,"list" +viewPagerAdapter.list.toString());
        getBinding().viewPager.setCurrentItem(getPresenter().getCurrentItem());
        getBinding().navigation.getMenu().getItem(getPresenter().getCurrentItem()).setChecked(true);
        getBinding().viewPager.setOffscreenPageLimit(list.size()+1); //viewpager+fragment避免每次都重新加载   太强了   就一行代码。。。


    }


    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Log.e(TAG,"bar change");
            menuItem = item;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Log.e(TAG,"list.indexOf(homeFragment)" + list.indexOf(homeFragment));
                    getBinding().viewPager.setCurrentItem(list.indexOf(homeFragment));
                    return true;
                case R.id.navigation_volunteering:
                    Log.e(TAG,"list.indexOf(volunteerFragment)" + list.indexOf(volunteerFragment));
                    Log.e("WustWebDebug", "nav volunteer click index=" + list.indexOf(volunteerFragment));
                    getBinding().viewPager.setCurrentItem(list.indexOf(volunteerFragment));
                    return true;
                case R.id.navigation_course:
                    Log.e(TAG,"list.indexOf(courseFragment)" + list.indexOf(courseFragment));
                    getBinding().viewPager.setCurrentItem(list.indexOf(courseFragment));
                    return true;
                case R.id.navigation_consult:
                    Log.e(TAG,"list.indexOf(consultFragment)" + list.indexOf(consultFragment));
                    Log.e("WustWebDebug", "nav consult click index=" + list.indexOf(consultFragment));
                    getBinding().viewPager.setCurrentItem(list.indexOf(consultFragment));
                    return true;
                case R.id.navigation_mine:
                    Log.e(TAG,"list.indexOf(mineFragment)" + list.indexOf(mineFragment));
                    getBinding().viewPager.setCurrentItem(list.indexOf(mineFragment));
                    return true;
            }
            return false;
        }
    };




    @Override
    public void onBackPressed() {
        exit();
    }

    private long pressTime = 0L;

    /**
     * MainActivity点击返回按钮调用
     * 涉及的Fragment的返回按钮
     * */
    private void exit(){
        if(getBinding().viewPager.getCurrentItem() == list.indexOf(volunteerFragment) &&volunteerFragment.onKeyDownBack()){
            return;
        }else if(getBinding().viewPager.getCurrentItem() == list.indexOf(consultFragment) && consultFragment.onKeyDownBack()){
            return;
        }
        if((System.currentTimeMillis() - pressTime) > 2000){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.show("再按一次退出程序");
                }
            });
            pressTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    @Override
    public void showNoticeDialog(List<NoticeBean> list) {
        noticeDialog = new NoticeDialog(list);
        noticeDialog.show(getSupportFragmentManager(), "noticeDialog");
//        noticeDialog.setNoticeListForShow(list);
    }

    @Override
    public void showUpdateDialog(ConfigBean bean) {

        UpdateDialog updateDialog = new UpdateDialog();
        updateDialog.show(getSupportFragmentManager(), "Update");
    }

    @Override
    public void showLostDialog(LostBean bean) {
        LostDialog lostDialog = new LostDialog(bean);
        lostDialog.show(getSupportFragmentManager(), "lost");
    }

    /**
     * 初始化intent的Extra值，用于判断是否来自于小组件跳转
     * */
    private void initIntentExtra() {
        int kind = getIntent().getIntExtra("kind",APP);
        Log.d(TAG, "initIntentExtra: "+kind);
        if(kind==WIDGET){
            try{
                long courseID = getIntent().getLongExtra("courseID",0);
                new CourseDialogNew.Builder(this)
                        .setList(getPresenter().getCoursesByID(courseID))
                        .create();
            }catch (Exception e){
                Log.d(TAG, "initIntentExtra: 传入的courseID不是int,不能进行转化");
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void exitAPP() {
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.AppTask> appTaskList = activityManager.getAppTasks();
        for (ActivityManager.AppTask appTask : appTaskList) {
            appTask.finishAndRemoveTask();
        }
    }


    @Override
    public void setBarState(boolean value) {
        if(value) {
            TranslateAnimation hideAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f);
            hideAnim.setDuration(500);
            getBinding().navigation.startAnimation(hideAnim);
            hideAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    getBinding().navigation.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        } else {
            TranslateAnimation showAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
            showAnim.setDuration(500);
            getBinding().navigation.startAnimation(showAnim);
            getBinding().navigation.setVisibility(View.VISIBLE);
        }
    }

}