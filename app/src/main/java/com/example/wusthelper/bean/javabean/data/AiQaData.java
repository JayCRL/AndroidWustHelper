package com.example.wusthelper.bean.javabean.data;

import com.example.wusthelper.bean.javabean.AiQaAnswerBean;
import com.google.gson.annotations.SerializedName;

public class AiQaData extends BaseData {
    @SerializedName("data")
    private AiQaAnswerBean data;

    public AiQaAnswerBean getData() {
        return data;
    }

    public void setData(AiQaAnswerBean data) {
        this.data = data;
    }
}
