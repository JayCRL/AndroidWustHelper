package com.example.wusthelper.bean.javabean.data;

import com.example.wusthelper.bean.javabean.ConfigBean;
import com.google.gson.annotations.SerializedName;
/**
 * 解析配置文件接口的信息
 * */
public class ConfigData extends BaseData {
    private Boolean isUpdate;
    @SerializedName("data")
    public ConfigBean data;

    public ConfigData() {
        isUpdate=false;
        setCode(-1);
        setMsg("没有加载");
        data = new ConfigBean();
    }


    public ConfigBean getData() {
        return data;
    }

    public void setData(ConfigBean data) {
        this.data = data;
    }

    public Boolean getUpdate() {
        return isUpdate;
    }

    public void setUpdate(Boolean update) {
        isUpdate = update;
    }

    @Override
    public String toString() {
        return "isUpdate"+
                "code"+
                "msg"+
                data.toString();
    }
}
