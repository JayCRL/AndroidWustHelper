package com.example.wusthelper.bean.javabean.data;

import java.util.ArrayList;
import java.util.List;

public class CampusMateNotificationListData extends BaseData {
    private List<CampusMateNotification> data = new ArrayList<>();

    public List<CampusMateNotification> getData() {
        return data;
    }

    public void setData(List<CampusMateNotification> data) {
        this.data = data;
    }
}
