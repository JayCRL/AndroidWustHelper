package com.example.wusthelper.bean.javabean.data;

import com.example.wusthelper.bean.javabean.CourseBean;
import com.example.wusthelper.bean.javabean.CourseNameBean;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class CourseNameData extends BaseData{

    @SerializedName("data")
    private List<CourseNameBean> data = new ArrayList<>();

    public List<CourseNameBean> getData() {
        return data;
    }

    public void setData(List<CourseNameBean> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "CourseNameData{" +
                "data=" + data +
                '}';
    }
}
