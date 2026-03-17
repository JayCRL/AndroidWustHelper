package com.example.wusthelper.bean.javabean;

import com.google.gson.annotations.SerializedName;

public class CycleImageBean {

    @SerializedName("actid")
    private int actId;
    //轮播图标题
    private String title;
    //轮播图内容，（附带的网址）
    private String content;
    //图片的链接
    private String imgUrl;

    private String updateTime;

    public int getActId() {
        return actId;
    }

    public void setActId(int actId) {
        this.actId = actId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "CycleImageBean{" +
                "actId=" + actId +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", imgUrl='" + imgUrl + '\'' +
                ", updateTime='" + updateTime + '\'' +
                '}';
    }
}
