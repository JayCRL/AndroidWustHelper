package com.example.wusthelper.bean.javabean.data;

import com.example.wusthelper.bean.javabean.ConfigBean;
import com.example.wusthelper.bean.javabean.CycleImageBean;
import com.example.wusthelper.bean.javabean.NoticeBean;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CycleImageData extends BaseData{

    @SerializedName("data")
    public List<CycleImageBean> data ;

    public List<CycleImageBean> getData() {
        return data;
    }

    public void setData(List<CycleImageBean> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return super.toString()+
                "CycleImageData{" +
                "data=" + data +
                '}';
    }
}
