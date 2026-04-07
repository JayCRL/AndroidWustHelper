package com.example.wusthelper.dbhelper;

import android.util.Log;

import com.example.wusthelper.bean.itembean.TodayCourseListForWidget;
import com.example.wusthelper.bean.javabean.CourseBean;
import com.example.wusthelper.bean.javabean.WidgetCourseBean;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.helper.TimeTools;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

/**
 * 小插件专用课表数据库Helper
 * */
public class WidgetCourseDB {

    private static final String TAG = "WidgetCourseDB";

    private static final String[] weekdays = {"","周一", "周二", "周三", "周四", "周五", "周六", "周日"};

    public static List<TodayCourseListForWidget> getTodayCourseListForWidget() {

        Log.d(TAG, "getTodayCourseListForWidget: ");

        List<TodayCourseListForWidget> listForWidgets = new ArrayList<>();

        String studentId = SharePreferenceLab.getStudentId();
        String selectSemester = SharePreferenceLab.getSelectSemester();

        int currentWeek = TimeTools.getCurrentWeek();
        int currentWeekday =  TimeTools.getWeekday();
        int section = TimeTools.getFormatSection();

        //加载今日课表
        List<CourseBean> list = CourseDB.getTodayCourse(studentId,selectSemester,currentWeek,
                currentWeekday,section);

        if(list.size()==0){
            //今日的课表为空
            listForWidgets.add(new TodayCourseListForWidget(TodayCourseListForWidget.EMPTY_TYPE));
        }else{
            for(CourseBean courseBean : list){
                listForWidgets.add(new TodayCourseListForWidget(TodayCourseListForWidget.COURSE_TYPE,
                        courseBean));
            }
        }

        //加载后续五天的课程
        for (int i=1;i<=5;i++){
            list.clear();

            //当前周数加一（周日加一 变为周一，当前周加一）
            currentWeekday++;
            if(currentWeekday>7){
                currentWeekday=1;
                currentWeek++;
                if(currentWeek==25)
                    currentWeek=0;
            }

            list =CourseDB.getTodayCourse(studentId,selectSemester,currentWeek,
                    currentWeekday,1);
            if(list.size()==0){
                //当日日的课表为空
                continue;
            }else{
                listForWidgets.add(new TodayCourseListForWidget(TodayCourseListForWidget.DATE_TYPE,
                        TimeTools.getDateNextXDay(i)+weekdays[currentWeekday]));
                for(CourseBean courseBean : list){
                    listForWidgets.add(new TodayCourseListForWidget(TodayCourseListForWidget.COURSE_TYPE,
                            courseBean));
                }
            }
        }

        return listForWidgets;
    }


    /**
     * 每周课表小插件专用
     * */
    public static WidgetCourseBean getWidgetCourse(String studentId, String semester, int week, int i, int j) {

//        Log.d(TAG, "getWidgetCourse: "+studentId);
//        Log.d(TAG, "getWidgetCourse: "+semester);
//        Log.d(TAG, "getWidgetCourse: i"+i);
//        Log.d(TAG, "getWidgetCourse: week"+week);
//        Log.d(TAG, "getWidgetCourse: j"+j);
        if(SharePreferenceLab.getIsChooseSundayFirst() && i == 7) {
            week--;
        }
        List<CourseBean> list =new ArrayList<>();
        WidgetCourseBean bean = new WidgetCourseBean();
//        list= LitePal.where("startWeek <= ? " +
//                "and endWeek >= ?" +
//                "and studentId = ?" +
//                "and semester = ?" +
//                "and weekday = ?" +
//                "and startTime = ?", week+"",week+"",studentId,semester,i+"",j+"")
//                .find(CourseBean.class);
        list= LitePal.where("startWeek <= ? " +
                "and endWeek >= ?" +
                "and studentId = ?" +
                "and semester = ?" +
                "and weekday = ?" +
                "and startTime = ?"+
                "and classType <> ? ", week+"",week+"",studentId,semester,i+"",j+"",CourseBean.TYPE_QR+"")
                .find(CourseBean.class);

//        Log.d(TAG, "getWidgetCourse: "+list);
        if(list.size()>=1){
            //同一时间有多个数据（2节课或者2节课以上）,那也只取第一个课程上课
            bean.setCourseData(list.get(0));
            if(list.size()>1){
                bean.setNum(2);
            }
        }
        return bean;
    }


}
