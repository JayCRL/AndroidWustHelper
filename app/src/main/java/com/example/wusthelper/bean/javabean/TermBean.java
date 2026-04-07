package com.example.wusthelper.bean.javabean;

import com.google.gson.annotations.SerializedName;
/**
 * CourseData的变量组成之一
 * 主要是用于储存学期，以及其开学日期
 * */
public class TermBean {
    @SerializedName("term")
    public String term;
    @SerializedName("startDate")
    public String startDate;

    public TermBean() {
        term="";
        startDate= "";
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    @Override
    public String toString() {
        return "TermBean{" +
                "term='" + term + '\'' +
                ", startDate='" + startDate + '\'' +
                '}';
    }
}
