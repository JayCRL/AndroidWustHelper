package com.example.wusthelper.bean.javabean.data;

import com.example.wusthelper.bean.javabean.NoticeBean;

import java.util.ArrayList;
import java.util.List;

public class GatewayNoticeData extends BaseData {
    private List<NoticeBean> data = new ArrayList<>();

    public List<NoticeBean> getData() {
        return data;
    }

    public void setData(List<NoticeBean> data) {
        this.data = data;
    }
}
