package com.example.wusthelper.bean.javabean.data;

import com.example.wusthelper.bean.javabean.EmptyClassroomBean;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class EmptyClassroomData extends BaseData{

    @SerializedName("data")
    private List<EmptyClassroomBean> data = new ArrayList<>();

    public List<EmptyClassroomBean> getData() {
        return data;
    }

    public void setData(List<EmptyClassroomBean> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "EmptyClassroomData{" +
                "data=" + data +
                '}';
    }
}
