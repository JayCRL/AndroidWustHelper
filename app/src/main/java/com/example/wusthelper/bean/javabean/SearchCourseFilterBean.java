package com.example.wusthelper.bean.javabean;

import com.google.gson.annotations.SerializedName;

public class SearchCourseFilterBean {
    @SerializedName(value = "courseName", alternate = {"name"})
    private String courseName;
    @SerializedName(value = "teacherName", alternate = {"teacher"})
    private String teacherName;
    @SerializedName("classroom")
    private String classroom;
    @SerializedName("campusName")
    private String campusName;
    @SerializedName("weekDay")
    private String weekDay;
    @SerializedName(value = "startSection", alternate = {"section"})
    private String startSection;
    @SerializedName(value = "endSection", alternate = {"sectionEnd"})
    private String endSection;
    @SerializedName("startWeek")
    private String startWeek;
    @SerializedName("endWeek")
    private String endWeek;

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getClassroom() {
        return classroom;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }

    public String getCampusName() {
        return campusName;
    }

    public void setCampusName(String campusName) {
        this.campusName = campusName;
    }

    public String getWeekDay() {
        return weekDay;
    }

    public void setWeekDay(String weekDay) {
        this.weekDay = weekDay;
    }

    public String getStartSection() {
        return startSection;
    }

    public void setStartSection(String startSection) {
        this.startSection = startSection;
    }

    public String getEndSection() {
        return endSection;
    }

    public void setEndSection(String endSection) {
        this.endSection = endSection;
    }

    public String getStartWeek() {
        return startWeek;
    }

    public void setStartWeek(String startWeek) {
        this.startWeek = startWeek;
    }

    public String getEndWeek() {
        return endWeek;
    }

    public void setEndWeek(String endWeek) {
        this.endWeek = endWeek;
    }
}
