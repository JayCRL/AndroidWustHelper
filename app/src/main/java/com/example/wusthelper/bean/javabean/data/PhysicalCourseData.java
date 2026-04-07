package com.example.wusthelper.bean.javabean.data;

import com.example.wusthelper.bean.javabean.CourseBean;
import com.example.wusthelper.bean.javabean.CycleImageBean;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PhysicalCourseData extends BaseData{

    @SerializedName("data")
    public List<CourseBean> data ;

    public List<CourseBean> getData() {
        return data;
    }

    public void setData(List<CourseBean> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return super.toString()+
                "PhysicalCourseData{" +
                "data=" + data +
                '}';
    }
}
