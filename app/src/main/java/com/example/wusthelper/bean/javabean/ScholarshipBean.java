package com.example.wusthelper.bean.javabean;

import org.litepal.crud.LitePalSupport;

public class ScholarshipBean extends LitePalSupport {
    private String courseName;
    private int id;
    private String semester;

    private String credit;

    private String gpa;
    private boolean isChecked;
    private double weight;

    public String getCredit() {
        return credit;
    }

    public void setCredit(String credit) {
        this.credit = credit;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGpa() {
        return gpa;
    }

    public void setGpa(String gpa) {
        this.gpa = gpa;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "ScholarshipBean{" +
                "courseName='" + courseName + '\'' +
                ", id=" + id +
                ", semester='" + semester + '\'' +
                ", credit='" + credit + '\'' +
                ", gpa='" + gpa + '\'' +
                ", isChecked=" + isChecked +
                ", weight=" + weight +
                '}';
    }
}