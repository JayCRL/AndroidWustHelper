package com.example.wusthelper.bean.itembean;

import com.example.wusthelper.bean.javabean.CourseBean;

/**
 * 专用于每日课表小组件的显示
 * */
public class TodayCourseListForWidget {

    //课程类数据
    public static final int COURSE_TYPE = 0;
    //日期数据
    public static final int DATE_TYPE = 1;
    //该天没有数据
    public static final int EMPTY_TYPE = 2;

    private int itemType;

    private String date;

    private CourseBean courseBean;

    public TodayCourseListForWidget(int itemType) {
        this.itemType = EMPTY_TYPE;
    }

    public TodayCourseListForWidget(int itemType, String date) {
        this.itemType = DATE_TYPE;
        this.date = date;
        courseBean = new CourseBean();
    }

    public TodayCourseListForWidget(int itemType, CourseBean courseBean) {
        this.itemType = COURSE_TYPE;
        this.date = "";
        this.courseBean = courseBean;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public CourseBean getCourseBean() {
        return courseBean;
    }

    public void setCourseBean(CourseBean courseBean) {
        this.courseBean = courseBean;
    }

    @Override
    public String toString() {
        return "TodayCourseListForWidget{" +
                "itemType=" + itemType +
                ", date='" + date + '\'' +
                ", courseBean=" + courseBean +
                '}';
    }
}
