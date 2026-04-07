package com.example.wusthelper.bean.javabean;

import com.google.gson.annotations.SerializedName;

public class AnnouncementBean {
    @SerializedName("id")
    private String announcementId;
    @SerializedName("title")
    private String announcementTitle;
    @SerializedName("publishTime")
    private String announcementCreatetime;

    public String getAnnouncementId() {
        return announcementId;
    }

    public void setAnnouncementId(String announcementId) {
        this.announcementId = announcementId;
    }

    public String getAnnouncementTitle() {
        return announcementTitle;
    }

    public void setAnnouncementTitle(String announcementTitle) {
        this.announcementTitle = announcementTitle;
    }

    public String getAnnouncementCreatetime() {
        return announcementCreatetime;
    }

    public void setAnnouncementCreatetime(String announcementCreatetime) {
        this.announcementCreatetime = announcementCreatetime;
    }

    @Override
    public String toString() {
        return "announcementData{" +
                "announcementId='" + announcementId + '\'' +
                ", announcementTitle='" + announcementTitle + '\'' +
                ", announcementCreatetime='" + announcementCreatetime + '\'' +
                '}';
    }
}
