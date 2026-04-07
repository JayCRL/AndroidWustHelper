package com.example.wusthelper.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wusthelper.R;
import com.example.wusthelper.adapter.ScholarshipRecycleViewAdapter;
import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.base.activity.BaseMvpActivity;
import com.example.wusthelper.bean.javabean.ScholarshipBean;
import com.example.wusthelper.databinding.ActivityScholarshipBinding;
import com.example.wusthelper.mvp.presenter.ScholarshipPresenter;
import com.example.wusthelper.mvp.view.ScholarshipView;
import com.example.wusthelper.utils.NumRollAnim;
import com.example.wusthelper.utils.SnackbarUtils;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class ScholarshipActivity extends BaseMvpActivity<ScholarshipView, ScholarshipPresenter,ActivityScholarshipBinding>
        implements ScholarshipView,View.OnClickListener {

    public static Intent newInstance(Context context) {
        return new Intent(context, ScholarshipActivity.class);
    }

    private ScholarshipRecycleViewAdapter scholarshipRecycleViewAdapter = new ScholarshipRecycleViewAdapter();


    private static final String TAG = "ScholarshipActivity";

    @Override
    public ScholarshipPresenter createPresenter() {
        return new ScholarshipPresenter();
    }

    @Override
    public ScholarshipView createView() {
        return this;
    }

    @Override
    public void initView() {
        getPresenter().initPresenterData();
        getWindow().setStatusBarColor(getResources().getColor(R.color.scholarship_toolbar));
        getBinding().scholarshipBackImage.setOnClickListener(this);
        getBinding().scholarshipGrade.setText("0.0");
        initDate();
    }



    public void initDate() {

        getPresenter().semester = getIntent().getStringExtra("semester");
        Log.e(TAG, "initDate: " +getPresenter().semester);

        if(getPresenter().semester.equals("")){
            getPresenter().semester = "大一";
        }
        getBinding().scholarshipSemester.setText(getPresenter().semester+"学年");
        getBinding().scholarshipReChooseButton.setOnClickListener(this);
        updateRecycleView();
        getPresenter().loadData();
    }

    private void updateRecycleView()
    {
        scholarshipRecycleViewAdapter.setList(getPresenter().scholarshipBeanList);
//        scholarshipRecycleViewAdapter = new ScholarshipRecycleViewAdapter(getPresenter().scholarshipBeanList,this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        getBinding().scholarshipRecycleView.setAdapter(scholarshipRecycleViewAdapter);
        getBinding().scholarshipRecycleView.setLayoutManager(linearLayoutManager);
        getBinding().scholarshipRecycleView.scheduleLayoutAnimation();
    }

    @Override
    protected void onDestroy() {
        getPresenter().isChanged();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.scholarship_reChoose_button:
                startActivity(ScholarShipChooseActivity.newInstance(this));
                finish();
                break;
            case R.id.scholarship_back_image:
                finish();
                break;
        }
    }

    @Override
    public void ShowColorSnackBar() {
        SnackbarUtils.showColorSnackBar(getBinding().scholarshipRecycleView,"请先勾选科目", R.color.color_snack_bar_no_internet);
    }

    @Override
    public void onGradeListShow() {
        scholarshipRecycleViewAdapter.setList(getPresenter().scholarshipBeanList);
        scholarshipRecycleViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void setScholarshipGrade() {
        getBinding().scholarshipGrade.setText("0.0");
    }

    @Override
    public void setStartAnim() {
        NumRollAnim.startAnim(getBinding().scholarshipGrade,(float) (getPresenter().molecule/getPresenter().denominator),500);
    }
}