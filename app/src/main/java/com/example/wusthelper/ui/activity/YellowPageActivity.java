package com.example.wusthelper.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.wusthelper.R;
import com.example.wusthelper.adapter.CountdownAdapter;
import com.example.wusthelper.adapter.YellowPageAdapter;
import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.base.activity.BaseMvpActivity;
import com.example.wusthelper.bean.javabean.YellowPageData;
import com.example.wusthelper.databinding.ActivityYellowPageBinding;
import com.example.wusthelper.mvp.presenter.YellowPagePresenter;
import com.example.wusthelper.mvp.view.YellowPageView;

import java.util.ArrayList;
import java.util.List;

public class YellowPageActivity extends BaseMvpActivity<YellowPageView, YellowPagePresenter,ActivityYellowPageBinding>
implements YellowPageView{

    private YellowPageAdapter yellowPageAdapter;

    public static Intent newInstance(Context context) {
        return new Intent(context, YellowPageActivity.class);
    }

    @Override
    public YellowPagePresenter createPresenter() {
        return new YellowPagePresenter();
    }

    @Override
    public YellowPageView createView() {
        return this;
    }


    @Override
    public void initView() {
        getWindow().setStatusBarColor(getResources().getColor(R.color.white));
        changeStatusBarTextColor(true);
        getPresenter().initDataBeanList();
        initRecycleView();
        setListener();
    }

    private void setListener() {
        getBinding().ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void initRecycleView() {
        yellowPageAdapter = new YellowPageAdapter(YellowPageActivity.this, getPresenter().getDataBeanList());
        getBinding().rvYellowPage.setLayoutManager(new LinearLayoutManager(YellowPageActivity.this));
        getBinding().rvYellowPage.setAdapter(yellowPageAdapter);
    }
}