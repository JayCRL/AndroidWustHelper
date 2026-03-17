package com.example.wusthelper.bean.javabean;

import com.example.wusthelper.bean.javabean.data.BaseData;
import com.google.gson.annotations.SerializedName;

public class CountDownChangeData extends BaseData {
    @SerializedName("data")
    public CountDownBean countDownBean;
}
