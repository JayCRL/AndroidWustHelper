package com.example.wusthelper.bean.javabean.data;

import com.example.wusthelper.bean.javabean.LibraryHistoryBean;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * 解析获取借阅历史接口的信息
 * */
public class LibraryHistoryData extends BaseData {

    @SerializedName("data")
    public List<LibraryHistoryBean> data = new ArrayList<>();

    public List<LibraryHistoryBean> getData() {
        return data;
    }

    public void setData(List<LibraryHistoryBean> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "LibraryHistoryData{" +
                "data=" + data +
                '}';
    }
}
