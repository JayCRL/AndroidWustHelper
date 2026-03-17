package com.example.wusthelper.helper;

import android.util.Log;

import com.example.wusthelper.bean.javabean.CourseBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoughDataResolver {

    public static List<Integer> getRoughDataList(List<CourseBean> roughData) {
        List<Integer> integers = new ArrayList<>();
        for (CourseBean courseBean : roughData){
            integers.add(getPosition(courseBean));
            integers.add(getPosition(courseBean));
        }
        //去重
        removeDuplicate(integers);
        Collections.sort(integers);
        return integers;
    }

    private static Integer getPosition(CourseBean courseBean) {
        return (courseBean.getStartTime()-1) + (courseBean.getWeekday()-1)*6;
    }

    private static void removeDuplicate(List<Integer> integers) {
        //去重算法
        for (int i = 0; i < integers.size(); i++) {
            for (int j = 0; j < integers.size(); j++) {
                if(i!=j&& integers.get(i).equals(integers.get(j))) {
                    integers.remove(integers.get(j));
                }
            }
        }
    }
}
