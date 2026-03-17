package com.example.wusthelper.bean.javabean;

import com.google.gson.annotations.SerializedName;

import org.litepal.crud.LitePalSupport;

public class NoticeBean extends LitePalSupport {

    @SerializedName("newsid")
    private int newsId;
    @SerializedName("title")
    private String title;
    @SerializedName(value = "content", alternate = {"context"})
    private String content;
    //公告发送的对象
    @SerializedName("obj")
    private String obj;
    //这条公告的更新时间，可以通过时间改变来判断公告是否更新过，如果更新过，则要重新给用户显示
    @SerializedName(value = "updateTime", alternate = {"createdAt"})
    private String updateTime;
    //用于是否确认过公告，默认为否
    private Boolean if_confirm;

    public NoticeBean() {
        this.if_confirm=false;
    }

    public int getNewsId() {
        return newsId;
    }

    public void setNewsId(int newsid) {
        this.newsId = newsid;
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

    public String getObj() {
        return obj;
    }

    public void setObj(String obj) {
        this.obj = obj;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public Boolean getIf_confirm() {
        return if_confirm;
    }

    public void setIf_confirm(Boolean if_confirm) {
        this.if_confirm = if_confirm;
    }

    @Override
    public String toString() {
        return "NoticeBean{" +
                "newsId=" + newsId +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", obj='" + obj + '\'' +
                ", updateTime='" + updateTime + '\'' +
                ", if_confirm=" + if_confirm +
                '}';
    }
}