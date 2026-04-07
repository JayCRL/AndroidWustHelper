package com.example.wusthelper.bean.javabean;
/**
 * 自定义的时间类
 * 用于管理和计算时间，比如当前周数等等
 * */
public class DateBean {
    private String date;

    private int week;

    private int weekday;

    private int year;

    private int month;

    private int day;

    public DateBean(String date, int week, int weekday) {
        this.date = date;
        this.week = week;
        this.weekday = weekday;
    }

    public DateBean(String date,int year, int month, int day) {
        this.date = date;
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    public int getWeekday() {
        return weekday;
    }

    public void setWeekday(int weekday) {
        this.weekday = weekday;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    @Override
    public String toString() {
        return "DateBean{" +
                "date='" + date + '\'' +
                ", week=" + week +
                ", weekday=" + weekday +
                ", year=" + year +
                ", month=" + month +
                ", day=" + day +
                '}';
    }
}

