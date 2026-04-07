package com.example.wusthelper.bean.javabean.data;

import com.example.wusthelper.bean.javabean.SearchCourseFilterBean;

import java.util.ArrayList;
import java.util.List;

public class SearchCourseFilterData extends BaseData {
    private List<SearchCourseFilterBean> data = new ArrayList<>();

    public List<SearchCourseFilterBean> getData() {
        return data;
    }

    public void setData(List<SearchCourseFilterBean> data) {
        this.data = data;
    }
}
