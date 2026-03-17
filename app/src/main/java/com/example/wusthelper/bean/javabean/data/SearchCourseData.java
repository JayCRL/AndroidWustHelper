package com.example.wusthelper.bean.javabean.data;

import com.example.wusthelper.bean.javabean.SearchCourseBean;

import java.util.ArrayList;
import java.util.List;

public class SearchCourseData extends BaseData{

    private List<SearchCourseBean> data = new ArrayList<>();

    public List<SearchCourseBean> getData() {
        return data;
    }

    public void setData(List<SearchCourseBean> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "SearchCourseData{" +
                "data=" + data +
                '}';
    }
}
