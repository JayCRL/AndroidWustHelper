package com.example.wusthelper.bean.javabean.data;

import java.util.ArrayList;
import java.util.List;

public class CampusMateActivityListData extends BaseData {
    private List<CampusMateActivity> data = new ArrayList<>();

    public List<CampusMateActivity> getData() {
        return data;
    }

    public void setData(List<CampusMateActivity> data) {
        this.data = data;
    }
}
