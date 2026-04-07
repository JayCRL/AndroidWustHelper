package com.example.wusthelper.bean.itembean;

public class DateItemForShow {

    public String weekday;
    public String date;
    //当前日期是否为今日，默认为否
    public boolean isDay = false;

    public DateItemForShow() {
    }

    public DateItemForShow(String weekday, String date) {
        this.weekday = weekday;
        this.date = date;
    }

    public DateItemForShow(String weekday, String date, boolean isDay) {
        this.weekday = weekday;
        this.date = date;
        this.isDay = isDay;
    }

    public String getWeekday() {
        return weekday;
    }

    public void setWeekday(String weekday) {
        this.weekday = weekday;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean getIsDay() {
        return isDay;
    }

    public void setIsDay(boolean isDay) {
        this.isDay = isDay;
    }

    @Override
    public String toString() {
        return "DateItemForShow{" +
                "weekday='" + weekday + '\'' +
                ", date='" + date + '\'' +
                ", isDay=" + isDay +
                '}';
    }
}
