package com.example.wusthelper.bean.javabean;

import com.google.gson.annotations.SerializedName;

public class SearchCourseBean {

    @SerializedName(value = "courseName", alternate = {"name"})
    private String courseName;
    @SerializedName("classroom")
    private String classroom;
    @SerializedName("campusName")
    private String campusName;
    @SerializedName("startWeek")
    private String startWeek;
    @SerializedName("endWeek")
    private String endWeek;
    @SerializedName(value = "startSection", alternate = {"section"})
    private String startSection;
    @SerializedName(value = "endSection", alternate = {"section"})
    private String endSection;
    @SerializedName("weekDay")
    private String weekDay;
    @SerializedName(value = "teacherName", alternate = {"teacher"})
    private String teacherName;

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
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

    public String getWeekDay() {
        return weekDay;
    }

    public void setWeekDay(String weekDay) {
        this.weekDay = weekDay;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    @Override
    public String toString() {
        return "SearchCourseBean{" +
                "courseName='" + courseName + '\'' +
                ", classroom='" + classroom + '\'' +
                ", campusName='" + campusName + '\'' +
                ", startWeek='" + startWeek + '\'' +
                ", endWeek='" + endWeek + '\'' +
                ", startSection='" + startSection + '\'' +
                ", endSection='" + endSection + '\'' +
                ", weekDay='" + weekDay + '\'' +
                ", teacherName='" + teacherName + '\'' +
                '}';
    }
}
