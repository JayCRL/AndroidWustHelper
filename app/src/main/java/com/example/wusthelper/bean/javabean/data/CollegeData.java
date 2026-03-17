package com.example.wusthelper.bean.javabean.data;

import com.example.wusthelper.bean.javabean.CollegeBean;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class CollegeData extends BaseData{

    @SerializedName("data")
    private List<CollegeBean> data = new ArrayList<>();

    public List<CollegeBean> getData() {
        return data;
    }

    public void setData(List<CollegeBean> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "CollegeData{" +
                "data=" + data +
                '}';
    }
}
