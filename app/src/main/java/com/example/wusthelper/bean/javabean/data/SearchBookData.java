package com.example.wusthelper.bean.javabean.data;

import com.example.wusthelper.bean.javabean.SearchBookBean;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class SearchBookData extends BaseData{

    @SerializedName("data")
    private List<SearchBookBean> data = new ArrayList<>();

    public List<SearchBookBean> getData() {
        return data;
    }

    public void setData(List<SearchBookBean> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "SearchBookData{" +
                "data=" + data +
                '}';
    }
}
