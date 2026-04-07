package com.example.wusthelper.bean.javabean.data;

import com.example.wusthelper.bean.javabean.AnnouncementBean;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class AnnouncementData extends BaseData{
    @SerializedName("data")
    private List<AnnouncementBean> data = new ArrayList<>();

    public List<AnnouncementBean> getData() {
        return data;
    }

    public void setData(List<AnnouncementBean> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "AnnouncementData{" +
                "data=" + data +
                '}';
    }
}
