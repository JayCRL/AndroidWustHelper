package com.example.wusthelper.mvp.presenter;

import android.annotation.SuppressLint;
import com.example.wusthelper.base.BasePresenter;
import com.example.wusthelper.bean.javabean.ScholarshipBean;
import com.example.wusthelper.mvp.view.ScholarshipView;
import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class ScholarshipPresenter extends BasePresenter<ScholarshipView> {

    public List<ScholarshipBean> scholarshipBeanList;

    public String semester;

    public double molecule;
    public double denominator;

    public boolean isChanged = false;

    @Override
    public void initPresenterData() {
        scholarshipBeanList = new ArrayList<>();
    }

    public void loadData()
    {
        if(LitePal.where("isChecked =? and semester = ?","1",semester).find(ScholarshipBean.class).isEmpty()){
            getView().ShowColorSnackBar();
        }else {
            updateList();
            getView().onGradeListShow();
            calculateScholarship();
        }
    }

    private void updateList() {
        scholarshipBeanList.clear();
        List<ScholarshipBean> beanList = LitePal.where("isChecked =? and semester =?","1",semester).find(ScholarshipBean.class);
//        Log.e(TAG, "updateList: size ====="+scholarshipBeanList.size() );
        scholarshipBeanList.addAll(beanList);
    }
    @SuppressLint("SetTextI18n")
    public void calculateScholarship()
    {
        if(scholarshipBeanList==null){
            return;
        }
        molecule = 0;
        denominator = 0;
        for(ScholarshipBean scholarshipBean :scholarshipBeanList){
            molecule +=(scholarshipBean.getWeight()*Double.parseDouble(scholarshipBean.getCredit())*Double.parseDouble(scholarshipBean.getGpa()));
            denominator +=(Double.parseDouble(scholarshipBean.getCredit()));
        }
        if(denominator ==0){
            getView().setScholarshipGrade();
        }else {
            getView().setStartAnim();
        }
    }

    public void isChanged(){
        if(isChanged){
            saveData();
        }
    }

    private void saveData() {
        LitePal.deleteAll(ScholarshipBean.class,"isChecked =? and semester = ?","1",semester);
//        Log.e(TAG, "saveData: 销毁后的个数  ==   "+scholarshipBeanList.size() +scholarshipBeanList.get(0).isChecked());
        for(ScholarshipBean scholarshipBean :scholarshipBeanList) {
//            Log.e(TAG, "saveData: 保存成功  " );
            scholarshipBean.assignBaseObjId(0);
            scholarshipBean.save();
        }
    }
}
