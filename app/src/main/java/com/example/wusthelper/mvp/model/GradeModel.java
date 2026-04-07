package com.example.wusthelper.mvp.model;

import com.example.wusthelper.bean.javabean.GradeBean;
import com.example.wusthelper.bean.javabean.GraduateGradeBean;
import com.example.wusthelper.bean.javabean.data.GradeData;
import com.example.wusthelper.bean.javabean.data.GraduateData;
import com.example.wusthelper.bean.javabean.data.GraduateGradeData;
import com.example.wusthelper.request.NewApiHelper;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;

import org.litepal.LitePal;

import java.util.List;

public class GradeModel {
    public List<GradeBean> getGradeItemForShowFromDB() {
        return LitePal.order("schoolTerm desc,courseName desc").find(GradeBean.class);
    }

    public List<GraduateGradeBean> getGraduateGradeItemForShowFromDB() {
        return LitePal.order("term desc,name desc").find(GraduateGradeBean.class);
    }


    /**
     * 网络请求，获取课程表
     * */
    public void getGradeFromNet(DisposeDataListener listener) {
        NewApiHelper.getGrade(listener);
    }

    public void getGraduateGradeFromNet(DisposeDataListener listener) {
        NewApiHelper.getGraduateGrade(listener);
    }

    public void saveGradeListToDB(GradeData gradeData) {
        LitePal.deleteAll(GradeBean.class);
        for(GradeBean gradeBean : gradeData.getData()){
            gradeBean.save();
        }
    }

    public void saveGraduateGradeListToDB(GraduateGradeData graduateGradeData) {
        LitePal.deleteAll(GraduateGradeBean.class);
        for(GraduateGradeBean graduategradeBean : graduateGradeData.getData()){
            graduategradeBean.save();
        }
    }

    public boolean isGradeUpdate(GradeData gradeData) {
        return (gradeData.data.size() != LitePal.findAll(GradeBean.class).size()) ;
    }

    public boolean isGraduateGradeUpdate(GraduateGradeData graduateGradeData) {
        return (graduateGradeData.data.size() != LitePal.findAll(GraduateGradeBean.class).size()) ;
    }

    public List<GradeBean> queryGradeWithTermFromDB(String trueTerm) {

        return LitePal.where("schoolTerm like ? ", "%" + trueTerm + "%").
                order("schoolTerm desc,courseName desc").find(GradeBean.class);
    }

    public List<GraduateGradeBean> queryGraduateGradeWithTermFromDB(String trueTerm) {

        return LitePal.where("term like ? ", "%" + trueTerm + "%").
                order("term desc,name desc").find(GraduateGradeBean.class);
    }

    public List<GradeBean> queryGradeWithText(String name) {
        return LitePal.where("courseName like ?", "%" + name + "%")
                .order("schoolTerm desc,courseName desc").find(GradeBean.class);
    }

    public List<GraduateGradeBean> queryGraduateGradeWithText(String name) {
        return LitePal.where("name like ?", "%" + name + "%")
                .order("term desc,name desc").find(GraduateGradeBean.class);
    }
}
