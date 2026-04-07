package com.example.wusthelper.bean.javabean.data;

import java.util.ArrayList;
import java.util.List;

public class EmptyClassroomSimpleData extends BaseData {
    private List<String> data = new ArrayList<>();

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }
}
