package com.example.wusthelper.bean.javabean.data;

import com.example.wusthelper.bean.javabean.GradeBean;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GradeData extends BaseData{

    @SerializedName("data")
    public List<GradeBean> data;

    public GradeData(List<GradeBean> data) {
        this.data = data;
    }

    public List<GradeBean> getData() {
        return data;
    }

    public void setData(List<GradeBean> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return super.toString()+"GradeData{" +
                "data=" + data +
                '}';
    }
}
