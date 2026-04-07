package com.example.wusthelper.bean.javabean.data;

import java.util.ArrayList;
import java.util.List;

public class CampusMateApplicationListData extends BaseData {
    private List<CampusMateApplication> data = new ArrayList<>();

    public List<CampusMateApplication> getData() {
        return data;
    }

    public void setData(List<CampusMateApplication> data) {
        this.data = data;
    }
}
