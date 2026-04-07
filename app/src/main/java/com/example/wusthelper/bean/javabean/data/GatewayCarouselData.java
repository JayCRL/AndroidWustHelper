package com.example.wusthelper.bean.javabean.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class GatewayCarouselData extends BaseData {
    @SerializedName("data")
    private List<String> data = new ArrayList<>();

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }
}
