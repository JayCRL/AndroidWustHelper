package com.example.wusthelper.bean.itembean;

/**
 * @author: Gong Yunhao
 * @version: V1.0
 * @date: 2018/10/22
 * @github https://github.com/Roman-Gong
 * @blog https://www.jianshu.com/u/52a8fa1f29fb
 */
public class BusItemForShow {
    private int hour;
    private int minute;

    public BusItemForShow(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }
}
