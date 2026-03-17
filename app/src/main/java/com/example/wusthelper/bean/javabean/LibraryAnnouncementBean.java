package com.example.wusthelper.bean.javabean;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class LibraryAnnouncementBean {
    @SerializedName("code")
    private int code;
    @SerializedName("msg")
    private String msg;
    @SerializedName("data")
    public List<AnnouncementBean> data=new ArrayList<>();

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<AnnouncementBean> getData() {
        return data;
    }

    public void setData(List<AnnouncementBean> data) {
        this.data = data;
    }
}
