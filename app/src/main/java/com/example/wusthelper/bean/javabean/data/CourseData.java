package com.example.wusthelper.bean.javabean.data;

import com.example.wusthelper.bean.javabean.CourseBean;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
/**
 * 解析获取课程接口的信息
 * */
public class CourseData extends BaseData {

    @SerializedName("data")
    public List<CourseBean> data = new ArrayList<>();

    public List<CourseBean> getData() {
        return data;
    }

    public void setData(List<CourseBean> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "CourseData{" +
                "data=" + data +
                '}';
    }
}
