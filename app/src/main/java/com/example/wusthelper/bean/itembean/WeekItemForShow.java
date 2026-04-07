package com.example.wusthelper.bean.itembean;

import java.util.ArrayList;
import java.util.List;

public class WeekItemForShow {

    public int week;
    public List<Integer> list = new ArrayList<>();
    //是否为当前选中的周数,默认为否
    private boolean isSelectWeek = false;
    //是否为当前真实的周数，默认为否
    private boolean isRealWeek = false;



    public WeekItemForShow(int week) {
        this.week = week;
    }

    public WeekItemForShow(int week, List<Integer> list) {
        this.week = week;
        this.list = list;
    }

    public WeekItemForShow(int week, List<Integer> list,boolean isSelectWeek, boolean isRealWeek) {
        this.week = week;
        this.list = list;
        this.isSelectWeek = isSelectWeek;
        this.isRealWeek = isRealWeek;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    public List<Integer> getList() {
        return list;
    }

    public void setList(List<Integer> list) {
        this.list = list;
    }

    public boolean isSelectWeek() {
        return isSelectWeek;
    }

    public void setSelectWeek(boolean selectWeek) {
        isSelectWeek = selectWeek;
    }

    public boolean isRealWeek() {
        return isRealWeek;
    }

    public void setRealWeek(boolean realWeek) {
        isRealWeek = realWeek;
    }
}
