package com.example.wusthelper.bean.javabean.data;

import com.example.wusthelper.bean.javabean.GradeBean;
import com.example.wusthelper.bean.javabean.GraduateGradeBean;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GraduateGradeData extends BaseData{

    @SerializedName("data")
    public List<GraduateGradeBean> data;

    public List<GraduateGradeBean> getData() {
        return data;
    }

    public void setData(List<GraduateGradeBean> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return super.toString()+"GraduateGradeData{" +
                "data=" + data +
                '}';
    }
}
