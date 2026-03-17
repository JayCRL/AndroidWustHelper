package com.example.wusthelper.bean.javabean.data;

import com.google.gson.annotations.SerializedName;

public class AiSubmitData extends BaseData {
    @SerializedName("data")
    private Object data;

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
