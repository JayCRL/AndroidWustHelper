package com.example.wusthelper.bean.javabean.data;

import com.example.wusthelper.bean.javabean.LostBean;
import com.google.gson.annotations.SerializedName;

public class LostData extends BaseData{

    @SerializedName("data")
    public LostBean data;

    public LostBean getData() {
        return data;
    }

    public void setData(LostBean data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "LostData{" +
                "data=" + data +
                '}';
    }
}
