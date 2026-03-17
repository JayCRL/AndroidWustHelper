package com.example.wusthelper.bean.itembean;

import com.example.wusthelper.bean.javabean.CourseBean;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CourseListForShow {

    //一个课程数据都没有，在课表上显示为空白，点击期间以后可以暂时浮现添加图标，长按可以进入添加课表页面
    public static final int EMPTY = 0;
    //正常上课的情况，显示颜色，且显示名字，点击出现详情页面，长按可以进行修改页面
    public static final int COMMON = 1;
    //有两个或者两个以上的课程在同一时间点上课，只显示第一个课程，其余的课程在点击该课程以后，出现多项课程界面
    public static final int REPEAT = 2;

    public int type =EMPTY ;

    public List<CourseBean> itemList = new ArrayList<>();

    public CourseListForShow() {
        itemList = new ArrayList<>();
    }

    /**
     * 这个方法用于添加一条课程
     * */
    public void addListForShow(CourseBean courseBean){
        itemList.add(courseBean);
        if(itemList.size() == 1)
            type = COMMON;
        if (itemList.size()>1)
            type = REPEAT;
    }

    @NotNull
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("CourseListForShow{" +
                "type=" + type +
                ", itemList=");
        for(CourseBean courseBean : itemList){
            str.append(courseBean.toString()).append("\n");
        }
        return str.toString();
    }

    /**
     * 这个方法用于插入一条数据到第一条
     * */
    public void addListForShowInHead(CourseBean courseBean) {

        if(itemList.size()==0){
            addListForShow(courseBean);
        }else{
            itemList.add(0,courseBean);
        }

        if(itemList.size() == 1)
            type = COMMON;
        if (itemList.size()>1)
            type = REPEAT;
    }


}
