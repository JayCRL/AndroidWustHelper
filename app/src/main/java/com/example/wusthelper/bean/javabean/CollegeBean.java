package com.example.wusthelper.bean.javabean;

import com.google.gson.annotations.SerializedName;

public class CollegeBean {
    @SerializedName("id")
    private String id;
    @SerializedName("collegeName")
    private String collegeName;
    @SerializedName("courseTotal")
    private String courseTotal;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCollegeName() {
        return collegeName;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }

    public String getCourseTotal() {
        return courseTotal;
    }

    public void setCourseTotal(String courseTotal) {
        this.courseTotal = courseTotal;
    }

    @Override
    public String toString() {
        return "CollegeBean{" +
                "id='" + id + '\'' +
                ", collegeName='" + collegeName + '\'' +
                ", courseTotal='" + courseTotal + '\'' +
                '}';
    }
}
