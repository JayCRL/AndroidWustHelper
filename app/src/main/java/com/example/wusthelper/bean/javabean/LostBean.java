package com.example.wusthelper.bean.javabean;

import com.google.gson.annotations.SerializedName;

public class LostBean {
    @SerializedName("title")
    private String title;
    @SerializedName("context")
    private String context;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    @Override
    public String toString() {
        return "LostBean{" +
                "title='" + title + '\'' +
                ", context='" + context + '\'' +
                '}';
    }
}
