package com.example.wusthelper.bean.javabean;

import org.litepal.crud.LitePalSupport;

import java.util.List;

/**
 * @author: Gong Yunhao
 * @version: V1.0
 * @date: 2018/10/20
 * @github https://github.com/Roman-Gong
 * @blog https://www.jianshu.com/u/52a8fa1f29fb
 */
public class SchoolBusBean extends LitePalSupport {
    private String starting;
    private String destination;
    private List<String> hours;
    private List<String> minutes;
    private int isWorkDay;

    public String getStarting() {
        return starting;
    }

    public void setStarting(String starting) {
        this.starting = starting;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public List<String> getHours() {
        return hours;
    }

    public void setHours(List<String> hours) {
        this.hours = hours;
    }

    public List<String> getMinutes() {
        return minutes;
    }

    public void setMinutes(List<String> minutes) {
        this.minutes = minutes;
    }

    public int isWorkDay() {
        return isWorkDay;
    }

    public void setWorkDay(int workDay) {
        isWorkDay = workDay;
    }
}
