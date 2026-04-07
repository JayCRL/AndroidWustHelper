package com.example.wusthelper.ui.activity;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.wusthelper.R;
import com.example.wusthelper.adapter.ScholarshipChooseAdapter;
import com.example.wusthelper.base.activity.BaseMvpActivity;
import com.example.wusthelper.bean.javabean.ScholarshipBean;
import com.example.wusthelper.databinding.ActivityScholarshipChooseBinding;
import com.example.wusthelper.mvp.presenter.ScholarShipChoosePresenter;
import com.example.wusthelper.mvp.view.ScholarShipChooseView;
import com.example.wusthelper.utils.CustomPopWindowUtil;
import com.example.wusthelper.utils.SnackbarUtils;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ScholarShipChooseActivity extends BaseMvpActivity<ScholarShipChooseView, ScholarShipChoosePresenter,ActivityScholarshipChooseBinding>
        implements ScholarShipChooseView,View.OnClickListener {

    private static final String TAG = "ScholarShipActivity";

    public static Intent newInstance(Context context) {
        return new Intent(context, ScholarShipChooseActivity.class);
    }
    private ScholarshipChooseAdapter scholarshipChooseAdapter = new ScholarshipChooseAdapter();
    private CustomPopWindowUtil customPopWindowUtil;


    @Override
    public ScholarShipChoosePresenter createPresenter() {
        return new ScholarShipChoosePresenter();
    }

    @Override
    public ScholarShipChooseView createView() {
        return this;
    }

    @Override
    public void initView() {
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        setListener();
        getPresenter().initPresenterData();
        updateRecycleView();
        scholarshipChooseAllImage();
        getPresenter().loadScholarship();
    }

    private void scholarshipChooseAllImage(){
        if(!isExistChecked()) {
            getPresenter().isChooseAll = false;
            getBinding().scholarshipChooseAllImage.setImageResource(R.drawable.scholarship_choose_all_no);
        }else{
            getPresenter().isChooseAll = true;
            getBinding().scholarshipChooseAllImage.setImageResource(R.drawable.scholarship_choose_all_yes);
        }
    }

    private void setListener(){
        getBinding().scholarshipChooseToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        getBinding().scholarshipChooseButton.setOnClickListener(this);
        getBinding().scholarshipChooseSort.setOnClickListener(this);
        getBinding().scholarshipChooseSemesterChoose.setOnClickListener(this);
        getBinding().scholarshipChooseAllImage.setOnClickListener(this);
    }

    private void updateRecycleView()
    {
        getPresenter().scholarshipBeanList = LitePal.findAll(ScholarshipBean.class);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        scholarshipChooseAdapter.setList(getPresenter().scholarshipBeanList);
//        scholarshipChooseAdapter = new ScholarshipChooseAdapter(getPresenter().scholarshipBeanList,this);
        Log.e(TAG, "updateRecycleView: size == "+getPresenter().scholarshipBeanList.size() );
        getBinding().scholarshipChooseRecycleView.setLayoutManager(linearLayoutManager);
        getBinding().scholarshipChooseRecycleView.setAdapter(scholarshipChooseAdapter);
        getBinding().scholarshipChooseRecycleView.scheduleLayoutAnimation();
    }

    @Override
    protected void onDestroy() {
        getPresenter().isChanged();
        super.onDestroy();
    }

    private void initSortPopUpWindow()
    {
        View view = LayoutInflater.from(ScholarShipChooseActivity.this).inflate(R.layout.scholarship_dialog_sort,null);
        customPopWindowUtil = new CustomPopWindowUtil.PopupWindowBuilder(this)
                .setView(view)
                .setFocusable(true)
                .setOutsideTouchable(true)
                .setAnimationStyle(R.style.PopupAnimation)
                .create()
                .showAtParentTop(getBinding().scholarshipChooseSort);

        handleSortClick(view);
    }

    private void handleSortClick(final View contentView)
    {
        View.OnClickListener listener = new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                if(customPopWindowUtil!=null){
                    customPopWindowUtil.dissmiss();
                }
                switch (view.getId()){
                    case R.id.scholarship_sort_credit:
                        Log.d(TAG,"sort_credit");
                        getPresenter().scholarshipBeanList.sort(new Comparator<ScholarshipBean>() {
                            @Override
                            public int compare(ScholarshipBean scholarshipBean, ScholarshipBean t1) {
                                return t1.getCredit().compareTo(scholarshipBean.getCredit());
                            }
                        });
                        scholarshipChooseAdapter.setList(getPresenter().scholarshipBeanList);
                        Log.d(TAG,"scholarshipBeanList" + getPresenter().scholarshipBeanList);
                        break;
                    case R.id.scholarship_sort_gpa:
                        Log.d(TAG,"sort_gpa");
                        getPresenter().scholarshipBeanList.sort(new Comparator<ScholarshipBean>() {
                            @Override
                            public int compare(ScholarshipBean scholarshipBean, ScholarshipBean t1) {
                                return t1.getGpa().compareTo(scholarshipBean.getGpa());
                            }
                        });
                        scholarshipChooseAdapter.setList(getPresenter().scholarshipBeanList);
                        Log.d(TAG,"scholarshipBeanList" + getPresenter().scholarshipBeanList);
                        break;

                }
                getBinding().scholarshipChooseRecycleView.scheduleLayoutAnimation();
                scholarshipChooseAdapter.notifyDataSetChanged();
            }
        };
        contentView.findViewById(R.id.scholarship_sort_credit).setOnClickListener(listener);
        contentView.findViewById(R.id.scholarship_sort_gpa).setOnClickListener(listener);
    }

    private void showListDialog() {
        // 这句话 - 显示所有学期
        final String[] items = {"大四学年", "大三学年"
                ,"大二学年", "大一学年"};
        AlertDialog.Builder listDialog = new AlertDialog.Builder(ScholarShipChooseActivity.this);
        listDialog.setTitle("选择学年");
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int invertedOrderIndex = items.length - 1 - which;  //这是个倒叙  ！！！！！
                getPresenter().semester = getPresenter().SEMESTER[invertedOrderIndex];
                getPresenter().semesterChange = invertedOrderIndex;
                getPresenter().loadScholarship();
            }
        });
        listDialog.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.scholarship_choose_button:
                getPresenter().isChanged();
                if(!isExistChecked()){
                    SnackbarUtils.showColorSnackBar(getBinding().scholarshipChooseRecycleView,"请先勾选科目", R.color.color_snack_bar_no_internet);
                }else{
                    startActivity(ScholarshipActivity.newInstance(this).putExtra("semester",getPresenter().semester));
                    finish();
                }
                break;
            case R.id.scholarship_choose_sort:
                initSortPopUpWindow();
                break;
            case R.id.scholarship_choose_semesterChoose:
                showListDialog();
                break;
            case R.id.scholarship_choose_all_image:
                getBinding().scholarshipChooseSort.setAlpha(0.5f);
                getPresenter().chooseChange();
        }
    }

    private boolean isExistChecked()
    {
        for(ScholarshipBean scholarshipBean:getPresenter().scholarshipBeanList) {
            if(scholarshipBean.isChecked()){
                return true;
            }
        }
        return false;
    }

    @Override
    public void ShowColorSnackBar() {
        SnackbarUtils.showColorSnackBar(getBinding().scholarshipChooseRecycleView,"未找到"+getPresenter().semester+"学期成绩信息",R.color.color_snack_bar_no_internet);
    }

    @Override
    public void onGradeListShow() {
        scholarshipChooseAdapter.setList(getPresenter().scholarshipBeanList);
        scholarshipChooseAdapter.notifyDataSetChanged();
    }

    @Override
    public void setScholarshipChooseButton(boolean msg) {
        getBinding().scholarshipChooseButton.setEnabled(msg);
    }

    @Override
    public void setScholarshipChooseAllImage(boolean msg) {
        if(msg){
            getBinding().scholarshipChooseAllImage.setImageResource(R.drawable.scholarship_choose_all_yes);
        }else{
            getBinding().scholarshipChooseAllImage.setImageResource(R.drawable.scholarship_choose_all_no);
        }
        getBinding().scholarshipChooseSort.setAlpha(1.0f);
    }
}
