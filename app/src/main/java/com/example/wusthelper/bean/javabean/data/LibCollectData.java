package com.example.wusthelper.bean.javabean.data;

import com.example.wusthelper.bean.javabean.CollectionBookBean;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class LibCollectData extends BaseData{
    @SerializedName("data")
    public List<CollectionBookBean> data=new ArrayList<>();


    public List<CollectionBookBean> getData() {
        return data;
    }

    public void setData(List<CollectionBookBean> data) {
        this.data = data;
    }
}
