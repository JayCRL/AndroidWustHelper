package com.example.wusthelper.bean.javabean.data;

import com.google.gson.annotations.SerializedName;

public class Test2 extends BaseData{

    @SerializedName("data")
    private String data;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Test2{" +
                "data='" + data + '\'' +
                '}';
    }
}
