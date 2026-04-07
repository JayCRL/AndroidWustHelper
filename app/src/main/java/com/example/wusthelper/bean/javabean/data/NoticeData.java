package com.example.wusthelper.bean.javabean.data;

import com.example.wusthelper.bean.javabean.NoticeBean;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NoticeData extends BaseData {

    @SerializedName("data")
    public List<NoticeBean> data ;

    public NoticeData(List<NoticeBean> data) {
        this.data = data;
    }

    public List<NoticeBean> getData() {
        return data;
    }

    public void setData(List<NoticeBean> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return super.toString()+"NoticeData{" +
                "data=" + data +
                '}';
    }
}
