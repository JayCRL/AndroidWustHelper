package com.example.wusthelper.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.wusthelper.R;
import com.example.wusthelper.base.activity.BaseMvpActivity;
import com.example.wusthelper.databinding.ActivityGradeChartBinding;
import com.example.wusthelper.mvp.presenter.GradeChartPresenter;
import com.example.wusthelper.mvp.view.GradeChartView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

public class GradeChartActivity extends BaseMvpActivity<GradeChartView, GradeChartPresenter,ActivityGradeChartBinding>
        implements GradeChartView {

    private static final String TAG = "GradeChartActivity";

    public static Intent newInstance(Context context) {
        return new Intent(context, GradeChartActivity.class);
    }

    @Override
    public void initView() {
        Log.e(TAG, "qzy146initView: ");
        getPresenter().initPresenterData();
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        setListener();
        initPieChart();
        initBarChart();
    }

    @Override
    public void showGradeChartGpa(String msg) {
        getBinding().tvGradeChartGpa.setText(msg);
    }

    @Override
    public void showGradeChartScore(String msg) {
        getBinding().tvGradeChartScore.setText(msg);
    }

    private void initBarChart() {
        //加载学期视图
        getPresenter().initBarChart(getBinding().barChartGrade,0);
        //加载学年视图
        getPresenter().initBarChart(getBinding().barChartGradeAcademicYear,1);

        getPresenter().showBarChart("历学期平均绩点视图",getBinding().barChartGrade);
        getPresenter().showAcademicYearBarChart("历学年评价绩点视图",getBinding().barChartGradeAcademicYear);
    }

    private void initPieChart() {
        getPresenter().initPieChart(getBinding().pieChartGradeView);
        getBinding().pieChartGradeView.postInvalidate();
    }

    private void setListener() {
        setSupportActionBar(getBinding().toolbarChart);
        getBinding().toolbarChart.setNavigationOnClickListener(v -> finish());
        getBinding().pieChartGradeView.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                PieEntry pieEntry = (PieEntry) e;
               // startActivity(ChartSearchActivity.newInstance(GradeChartActivity.this, String.valueOf(pieEntry.getX())));
            }

            @Override
            public void onNothingSelected() {

            }
        });
    }

    @Override
    public GradeChartPresenter createPresenter() {
        return new GradeChartPresenter();
    }

    @Override
    public GradeChartView createView() {
        return this;
    }


}
