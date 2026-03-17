package com.example.wusthelper.bean.javabean;

import com.google.gson.annotations.SerializedName;

public class CourseNameBean {

    @SerializedName("id")
    private String id;
    @SerializedName("courseName")
    private String courseName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    @Override
    public String toString() {
        return "CourseNameBean{" +
                "id='" + id + '\'' +
                ", courseName='" + courseName + '\'' +
                '}';
    }
}
