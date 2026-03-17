package com.example.wusthelper.bean.javabean;

import com.example.wusthelper.bean.javabean.data.BaseData;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class CountDownData extends BaseData {
    @SerializedName("data")
    public List<CountDownBean> countDownList;

    public void reMove(CountDownBean countDownBean){
        for(int i=0;i<countDownList.size();i++){
            if(countDownList.get(i).getOnlyId().equals(countDownBean.getOnlyId())){
                countDownList.remove(i--);
            }
        }
    }

    public CountDownData() {
        this.countDownList = new ArrayList<>();
    }
}
