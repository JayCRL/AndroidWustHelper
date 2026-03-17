package com.example.wusthelper.appwidget;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.example.wusthelper.R;
import com.example.wusthelper.bean.javabean.DateBean;
import com.example.wusthelper.bean.javabean.WidgetCourseBean;
import com.example.wusthelper.dbhelper.WidgetCourseDB;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.helper.TimeTools;
import com.example.wusthelper.ui.activity.LaunchActivity;
import com.example.wusthelper.ui.activity.WidgetSettingsActivity;
import com.example.wusthelper.utils.MyBitmapTool;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

/**
 * 课表
 * 桌面小插件
 * */
public class WeekScheduleWidgetProvider  extends AppWidgetProvider {

    private static final String REFRESH_ACTION = "com.android.linghang.wustcampus.AppWidget.REFRESH";
    private static final String UP_ACTION = "com.android.linghang.wustcampus.AppWidget.UP";
    private static final String DOWN_ACTION = "com.android.linghang.wustcampus.AppWidget.DOWN";
    private static final String CONTENT_CLICK_ACTION = "com.android.linghang.wustcampus.AppWidget.CONTENT.CLICK";
    private static final String SETTINGS_ACTION = "com.android.linghang.wustcampus.AppWidget.SETTINGS";
    private static final String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    private static final String BOOT_LOCKED_COMPLETED = "android.intent.action.LOCKED_BOOT_COMPLETED";
    private static final String REBOOT = "android.intent.action.REBOOT";
    private static final String APPWIDGET_UPDATE = "android.appwidget.action.APPWIDGET_UPDATE_OPTIONS";
    private static final String TAG = "WeekWidgetProvider";
    private int[] backgroundArray = {R.drawable.layer_color_class_1, R.drawable.layer_color_class_2, R.drawable.layer_color_class_3
            , R.drawable.layer_color_class_4, R.drawable.layer_color_class_5, R.drawable.layer_color_class_6, R.drawable.layer_color_class_7
            , R.drawable.layer_color_class_8, R.drawable.layer_color_class_9, R.drawable.layer_color_class_10};
    private int[] parentResourceIdArray = {R.id.rl_mon, R.id.rl_tue, R.id.rl_wed, R.id.rl_thu, R.id.rl_fri, R.id.rl_sat, R.id.rl_sun};
    private String[] weekdays = {"Mon.", "Tue.", "Wed.", "Thu.", "Fri.", "Sat.", "Sun."};
    private int[] courseTextViewArray = {R.id.tv_course_mon_1, R.id.tv_course_mon_2, R.id.tv_course_mon_3, R.id.tv_course_mon_4
            , R.id.tv_course_tue_1, R.id.tv_course_tue_2, R.id.tv_course_tue_3, R.id.tv_course_tue_4
            , R.id.tv_course_wed_1, R.id.tv_course_wed_2, R.id.tv_course_wed_3, R.id.tv_course_wed_4
            , R.id.tv_course_thu_1, R.id.tv_course_thu_2, R.id.tv_course_thu_3, R.id.tv_course_thu_4
            , R.id.tv_course_fri_1, R.id.tv_course_fri_2, R.id.tv_course_fri_3, R.id.tv_course_fri_4
            , R.id.tv_course_sat_1, R.id.tv_course_sat_2, R.id.tv_course_sat_3, R.id.tv_course_sat_4
            , R.id.tv_course_sun_1, R.id.tv_course_sun_2, R.id.tv_course_sun_3, R.id.tv_course_sun_4};
    private int[] isMultipleArray = {R.id.iv_multiple_mon_1, R.id.iv_multiple_mon_2, R.id.iv_multiple_mon_3, R.id.iv_multiple_mon_4
            , R.id.iv_multiple_tue_1, R.id.iv_multiple_tue_2, R.id.iv_multiple_tue_3, R.id.iv_multiple_tue_4
            , R.id.iv_multiple_wed_1, R.id.iv_multiple_wed_2, R.id.iv_multiple_wed_3, R.id.iv_multiple_wed_4
            , R.id.iv_multiple_thu_1, R.id.iv_multiple_thu_2, R.id.iv_multiple_thu_3, R.id.iv_multiple_thu_4
            , R.id.iv_multiple_fri_1, R.id.iv_multiple_fri_2, R.id.iv_multiple_fri_3, R.id.iv_multiple_fri_4
            , R.id.iv_multiple_sat_1, R.id.iv_multiple_sat_2, R.id.iv_multiple_sat_3, R.id.iv_multiple_sat_4
            , R.id.iv_multiple_sun_1, R.id.iv_multiple_sun_2, R.id.iv_multiple_sun_3, R.id.iv_multiple_sun_4};

//    private String[] HangjiahuStartTime = {"08:20","10:15","14:00","15:55","19:00","20:50","——","——"};
//    private String[] HangjiahuStartEnd = {"10:00","11:55","15:40","17:35","20:40","22:30","——","——"};

    private String[] HangjiahuStartTime = {"08:20","10:20","14:00","16:00","18:40","20:30","——","——"};
    private String[] HangjiahuStartEnd = {"10:00","12:00","15:40","17:40","20:20","22:10","——","——"};

    private String[] QingShangStartTime = {"08:00","10:10","14:00","16:00","18:40","20:30","——","——"};
    private String[] QingShangEndTime = {"09:40","11:50","15:40","17:40","20:20","22:10","——","——"};
    
    //小组件当前的宽高
    private static int widgetWidth;

    private static int widgetHeight;



    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        Log.d(TAG, "onUpdate: "+appWidgetIds.length);
        for (int appWidgetId : appWidgetIds) {

            widgetWidth = SharePreferenceLab.getWidgetWeekWidth();
            widgetHeight = SharePreferenceLab.getWidgetWeekHeight();
//
//            widgetWidth = getWidgetWidth(context,appWidgetManager,appWidgetId);
//            widgetHeight = getWidgetHeight(context,appWidgetManager,appWidgetId);
            Log.d(TAG, "onUpdate: widgetWidth"+widgetWidth);
        }

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_week_schedule_layout);
        Intent refreshIntent = new Intent(context, WeekScheduleWidgetProvider.class);
        Intent upIntent = new Intent(context, WeekScheduleWidgetProvider.class);
        Intent downIntent = new Intent(context, WeekScheduleWidgetProvider.class);
        Intent contentClickIntent = new Intent(context, LaunchActivity.class);
        refreshIntent.setAction(REFRESH_ACTION);
        upIntent.setAction(UP_ACTION);
        downIntent.setAction(DOWN_ACTION);
        contentClickIntent.setAction(CONTENT_CLICK_ACTION);

        PendingIntent upPendingIntent = PendingIntent.getBroadcast(context, 0, upIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent downPendingIntent = PendingIntent.getBroadcast(context, 0, downIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent contentClickPendingIntent = PendingIntent.getActivity(context, 0, contentClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews.setOnClickPendingIntent(R.id.btn_up, upPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.btn_down, downPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.btn_refresh, refreshPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.ll_content, contentClickPendingIntent);

        Intent weekSettingsIntent = new Intent(context, WidgetSettingsActivity.class);

        weekSettingsIntent.putExtra("widgetType",2);
        weekSettingsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent weekSettings = PendingIntent.getActivity(context,2,weekSettingsIntent,PendingIntent.FLAG_CANCEL_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.btn_settings,weekSettings);


        String selectStudentId = SharePreferenceLab.getInstance().getStudentId(context);
        String selectSemester = SharePreferenceLab.getSelectSemester();
        String weekday = weekdays[TimeTools.getWeekday()-1];
        DateBean dateBean = SharePreferenceLab.getDateBean();
        int thisWeek = TimeTools.getWeek(dateBean, TimeTools.getFormatToday());
        remoteViews.setTextViewText(R.id.tv_weekday, weekday);
        remoteViews.setTextViewText(R.id.tv_week, '第' + String.valueOf(thisWeek) + '周');
        int campus = SharePreferenceLab.getInstance().getCampus(context);
        if (SharePreferenceLab.getInstance().isAtBottom(context)) {

            remoteViews.setViewVisibility(R.id.btn_up, View.INVISIBLE);
            remoteViews.setViewVisibility(R.id.btn_down,View.VISIBLE);
//            remoteViews.setInt(R.id.btn_down, "setBackgroundResource", R.drawable.shape_arrow_black);
            remoteViews.setInt(R.id.btn_down, "setImageResource", R.drawable.ic_keyboard_arrow_down_black_24dp);
            if (campus == SharePreferenceLab.HUANGJIAHU){
                remoteViews.setTextViewText(R.id.tv_time_1_start, HangjiahuStartTime[4]);
                remoteViews.setTextViewText(R.id.tv_time_1_end, HangjiahuStartEnd[4]);
                remoteViews.setTextViewText(R.id.tv_time_2_start, HangjiahuStartTime[5]);
                remoteViews.setTextViewText(R.id.tv_time_2_end, HangjiahuStartEnd[5]);
                remoteViews.setTextViewText(R.id.tv_time_3_start, HangjiahuStartTime[6]);
                remoteViews.setTextViewText(R.id.tv_time_3_end, HangjiahuStartEnd[6]);
                remoteViews.setTextViewText(R.id.tv_time_4_start, HangjiahuStartTime[7]);
                remoteViews.setTextViewText(R.id.tv_time_4_end, HangjiahuStartEnd[7]);
//                remoteViews.setTextViewText(R.id.tv_time_2, "6");
//                remoteViews.setTextViewText(R.id.tv_time_3, "7");
//                remoteViews.setTextViewText(R.id.tv_time_4, "8");
            }else{
                remoteViews.setTextViewText(R.id.tv_time_1_start, QingShangStartTime[4]);
                remoteViews.setTextViewText(R.id.tv_time_1_end, QingShangEndTime[4]);
                remoteViews.setTextViewText(R.id.tv_time_2_start, QingShangStartTime[5]);
                remoteViews.setTextViewText(R.id.tv_time_2_end, QingShangEndTime[5]);
                remoteViews.setTextViewText(R.id.tv_time_3_start, QingShangStartTime[6]);
                remoteViews.setTextViewText(R.id.tv_time_3_end, QingShangEndTime[6]);
                remoteViews.setTextViewText(R.id.tv_time_4_start, QingShangStartTime[7]);
                remoteViews.setTextViewText(R.id.tv_time_4_end, QingShangEndTime[7]);
            }
//            remoteViews.setTextViewText(R.id.tv_time_1, "5");
//            remoteViews.setTextViewText(R.id.tv_time_2, "6");
//            remoteViews.setTextViewText(R.id.tv_time_3, "7");
//            remoteViews.setTextViewText(R.id.tv_time_4, "8");
            refreshSchedule(context, selectStudentId, selectSemester, thisWeek, remoteViews, true);

        } else {

            remoteViews.setViewVisibility(R.id.btn_down,View.INVISIBLE);
            remoteViews.setViewVisibility(R.id.btn_up,View.VISIBLE);
//            remoteViews.setInt(R.id.btn_up, "setBackgroundResource", R.drawable.shape_arrow_black);
            remoteViews.setInt(R.id.btn_up, "setImageResource", R.drawable.ic_keyboard_arrow_up_black_24dp);
//            remoteViews.setTextViewText(R.id.tv_time_1, "1");
//            remoteViews.setTextViewText(R.id.tv_time_2, "2");
//            remoteViews.setTextViewText(R.id.tv_time_3, "3");
//            remoteViews.setTextViewText(R.id.tv_time_4, "4");
            if (campus == SharePreferenceLab.HUANGJIAHU){
                remoteViews.setTextViewText(R.id.tv_time_1_start, HangjiahuStartTime[0]);
                remoteViews.setTextViewText(R.id.tv_time_1_end, HangjiahuStartEnd[0]);
                remoteViews.setTextViewText(R.id.tv_time_2_start, HangjiahuStartTime[1]);
                remoteViews.setTextViewText(R.id.tv_time_2_end, HangjiahuStartEnd[1]);
                remoteViews.setTextViewText(R.id.tv_time_3_start, HangjiahuStartTime[2]);
                remoteViews.setTextViewText(R.id.tv_time_3_end, HangjiahuStartEnd[2]);
                remoteViews.setTextViewText(R.id.tv_time_4_start, HangjiahuStartTime[3]);
                remoteViews.setTextViewText(R.id.tv_time_4_end, HangjiahuStartEnd[3]);

//                remoteViews.setTextViewText(R.id.tv_time_2, "6");
//                remoteViews.setTextViewText(R.id.tv_time_3, "7");
//                remoteViews.setTextViewText(R.id.tv_time_4, "8");
            }else{
                remoteViews.setTextViewText(R.id.tv_time_1_start, QingShangStartTime[0]);
                remoteViews.setTextViewText(R.id.tv_time_1_end, QingShangEndTime[0]);
                remoteViews.setTextViewText(R.id.tv_time_2_start, QingShangStartTime[1]);
                remoteViews.setTextViewText(R.id.tv_time_2_end, QingShangEndTime[1]);
                remoteViews.setTextViewText(R.id.tv_time_3_start, QingShangStartTime[2]);
                remoteViews.setTextViewText(R.id.tv_time_3_end, QingShangEndTime[2]);
                remoteViews.setTextViewText(R.id.tv_time_4_start, QingShangStartTime[3]);
                remoteViews.setTextViewText(R.id.tv_time_4_end, QingShangEndTime[3]);
            }
            refreshSchedule(context, selectStudentId, selectSemester, thisWeek, remoteViews, false);

        }


        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);

    }

    private void refreshSchedule(Context context, String studentId, String semester, int week, RemoteViews parentRemoteViews, boolean isAtBottom) {

        setTextColor(parentRemoteViews,context);
        setBackground(parentRemoteViews,context);

        int startTime = isAtBottom ? 5 : 1;
        //判断是否选择了周日为首日
        if(SharePreferenceLab.getIsChooseSundayFirst()) {
            parentRemoteViews.setTextViewText(R.id.week_sun,"日");
            parentRemoteViews.setTextViewText(R.id.week_mon,"一");
            parentRemoteViews.setTextViewText(R.id.week_tue,"二");
            parentRemoteViews.setTextViewText(R.id.week_wed,"三");
            parentRemoteViews.setTextViewText(R.id.week_thu,"四");
            parentRemoteViews.setTextViewText(R.id.week_fri,"五");
            parentRemoteViews.setTextViewText(R.id.week_sta,"六");

        } else {
            parentRemoteViews.setTextViewText(R.id.week_sun,"一");
            parentRemoteViews.setTextViewText(R.id.week_mon,"二");
            parentRemoteViews.setTextViewText(R.id.week_tue,"三");
            parentRemoteViews.setTextViewText(R.id.week_wed,"四");
            parentRemoteViews.setTextViewText(R.id.week_thu,"五");
            parentRemoteViews.setTextViewText(R.id.week_fri,"六");
            parentRemoteViews.setTextViewText(R.id.week_sta,"日");
        }

        for (int id : courseTextViewArray) {
            parentRemoteViews.setViewVisibility(id, View.INVISIBLE);
        }

        for (int id : isMultipleArray) {
            parentRemoteViews.setViewVisibility(id, View.INVISIBLE);
        }

        if (SharePreferenceLab.getInstance().getIsLogin(context)) {

            for (int i = 1; i <= parentResourceIdArray.length; i++) {

                for (int j = startTime; j < (startTime + 4); j++) {

                    WidgetCourseBean widgetCourseBean = WidgetCourseDB.getWidgetCourse(studentId, semester, week, i, j);

                    Log.d(TAG, "refreshSchedule: "+widgetCourseBean);
                    if(widgetCourseBean.getNum()==0){
                       continue;
                    }
                    String className = widgetCourseBean.getClassName();
                    String classRoom = widgetCourseBean.getClassRoom();
                    String content = getContent(className, classRoom);
                    int color = widgetCourseBean.getColor();
                    int num = widgetCourseBean.getNum();
                    int time = j;
                    if (j > 4) {
                        time = j - 4;
                    }

                    int index;
                    if(SharePreferenceLab.getIsChooseSundayFirst()) {
                        index = 4 * (i % 7) + time - 1;
                    } else {
                        index = 4 * (i - 1) + time - 1;
                    }

                    if (num >= 1) {

                        parentRemoteViews.setViewVisibility(courseTextViewArray[index], View.VISIBLE);
                        parentRemoteViews.setInt(courseTextViewArray[index], "setBackgroundResource", backgroundArray[color]);
                        parentRemoteViews.setTextViewText(courseTextViewArray[index], content);

                        if (num > 1)
                            parentRemoteViews.setViewVisibility(isMultipleArray[index], View.VISIBLE);

                    }


                }

            }
        }

    }

    private String getContent(String className, String classRoom) {
        return className + '\n' + '@' + classRoom;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context,intent);

        widgetWidth = SharePreferenceLab.getWidgetWeekWidth();
        widgetHeight = SharePreferenceLab.getWidgetWeekHeight();

//        Toast.makeText(context, intent.getAction(), Toast.LENGTH_SHORT).show();

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_week_schedule_layout);
        Intent refreshIntent = new Intent(context, WeekScheduleWidgetProvider.class);
        Intent upIntent = new Intent(context, WeekScheduleWidgetProvider.class);
        Intent downIntent = new Intent(context, WeekScheduleWidgetProvider.class);
        Intent contentClickIntent = new Intent(context, LaunchActivity.class);

        refreshIntent.setAction(REFRESH_ACTION);
        upIntent.setAction(UP_ACTION);
        downIntent.setAction(DOWN_ACTION);
        contentClickIntent.setAction(CONTENT_CLICK_ACTION);
        PendingIntent upPendingIntent = PendingIntent.getBroadcast(context, 0, upIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent downPendingIntent = PendingIntent.getBroadcast(context, 0, downIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent contentClickPendingIntent = PendingIntent.getActivity(context, 0, contentClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews.setOnClickPendingIntent(R.id.btn_up, upPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.btn_down, downPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.btn_refresh, refreshPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.ll_content, contentClickPendingIntent);

        Intent weekSettingsIntent = new Intent(context, WidgetSettingsActivity.class);

        weekSettingsIntent.putExtra("widgetType",2);
        weekSettingsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent weekSettings = PendingIntent.getActivity(context,2,weekSettingsIntent,PendingIntent.FLAG_CANCEL_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.btn_settings,weekSettings);


        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        String selectStudentId = SharePreferenceLab.getInstance().getStudentId(context);
        String selectSemester = SharePreferenceLab.getSelectSemester();
        String weekday = weekdays[TimeTools.getWeekday()-1];
        DateBean dateBean = SharePreferenceLab.getInstance().getDateBean(context);
        int thisWeek = TimeTools.getWeek(dateBean, TimeTools.getFormatToday());
        if(thisWeek == 0){
            remoteViews.setTextViewText(R.id.tv_weekday, "假期中");
            remoteViews.setViewVisibility(R.id.tv_week, View.INVISIBLE);
        }else{
            remoteViews.setTextViewText(R.id.tv_weekday, weekday);
            remoteViews.setTextViewText(R.id.tv_week, '第' + String.valueOf(thisWeek) + '周');
        }

        switch (intent.getAction()) {

//            case BOOT_COMPLETED:
//
//            case REBOOT:
//
//            case BOOT_LOCKED_COMPLETED:
//
//            case APPWIDGET_UPDATE:
//
//            case REFRESH_ACTION:
//
//                remoteViews.setInt(R.id.btn_up, "setBackgroundResource", R.drawable.shape_arrow_gray);
//                remoteViews.setInt(R.id.btn_up, "setImageResource", R.drawable.ic_keyboard_arrow_up_gray_24dp);
//                remoteViews.setInt(R.id.btn_down, "setBackgroundResource", R.drawable.shape_arrow_black);
//                remoteViews.setInt(R.id.btn_down, "setImageResource", R.drawable.ic_keyboard_arrow_down_black_24dp);
//                remoteViews.setTextViewText(R.id.tv_time_1, "1");
//                remoteViews.setTextViewText(R.id.tv_time_2, "2");
//                remoteViews.setTextViewText(R.id.tv_time_3, "3");
//                remoteViews.setTextViewText(R.id.tv_time_4, "4");
//                SharePreferenceLab.getInstance().setIsAtBottom(context, false);
//
//                break;
            case UP_ACTION:
                if (SharePreferenceLab.getInstance().isAtBottom(context)) {
                    int campus = SharePreferenceLab.getInstance().getCampus(context);
                    remoteViews.setViewVisibility(R.id.btn_up,View.INVISIBLE);
                    remoteViews.setViewVisibility(R.id.btn_down,View.VISIBLE);
//                    remoteViews.setInt(R.id.btn_down, "setBackgroundResource", R.drawable.shape_arrow_black);
                    remoteViews.setInt(R.id.btn_down, "setImageResource", R.drawable.ic_keyboard_arrow_down_black_24dp);
                    if (campus == SharePreferenceLab.HUANGJIAHU){
                        remoteViews.setTextViewText(R.id.tv_time_1_start, HangjiahuStartTime[0]);
                        remoteViews.setTextViewText(R.id.tv_time_1_end, HangjiahuStartEnd[0]);
                        remoteViews.setTextViewText(R.id.tv_time_2_start, HangjiahuStartTime[1]);
                        remoteViews.setTextViewText(R.id.tv_time_2_end, HangjiahuStartEnd[1]);
                        remoteViews.setTextViewText(R.id.tv_time_3_start, HangjiahuStartTime[2]);
                        remoteViews.setTextViewText(R.id.tv_time_3_end, HangjiahuStartEnd[2]);
                        remoteViews.setTextViewText(R.id.tv_time_4_start, HangjiahuStartTime[3]);
                        remoteViews.setTextViewText(R.id.tv_time_4_end, HangjiahuStartEnd[3]);

//                remoteViews.setTextViewText(R.id.tv_time_2, "6");
//                remoteViews.setTextViewText(R.id.tv_time_3, "7");
//                remoteViews.setTextViewText(R.id.tv_time_4, "8");
                    }else{
                        remoteViews.setTextViewText(R.id.tv_time_1_start, QingShangStartTime[0]);
                        remoteViews.setTextViewText(R.id.tv_time_1_end, QingShangEndTime[0]);
                        remoteViews.setTextViewText(R.id.tv_time_2_start, QingShangStartTime[1]);
                        remoteViews.setTextViewText(R.id.tv_time_2_end, QingShangEndTime[1]);
                        remoteViews.setTextViewText(R.id.tv_time_3_start, QingShangStartTime[2]);
                        remoteViews.setTextViewText(R.id.tv_time_3_end, QingShangEndTime[2]);
                        remoteViews.setTextViewText(R.id.tv_time_4_start, QingShangStartTime[3]);
                        remoteViews.setTextViewText(R.id.tv_time_4_end, QingShangEndTime[3]);
                    }

//                    remoteViews.setTextViewText(R.id.tv_time_1, "1");
//                    remoteViews.setTextViewText(R.id.tv_time_2, "2");
//                    remoteViews.setTextViewText(R.id.tv_time_3, "3");
//                    remoteViews.setTextViewText(R.id.tv_time_4, "4");
                    SharePreferenceLab.getInstance().setIsAtBottom(context, false);
                    refreshSchedule(context, selectStudentId, selectSemester, thisWeek, remoteViews, false);

                }
                break;
            case DOWN_ACTION:
                if (!SharePreferenceLab.getInstance().isAtBottom(context)) {
                    int campus = SharePreferenceLab.getInstance().getCampus(context);
                    remoteViews.setViewVisibility(R.id.btn_down,View.INVISIBLE);
                    remoteViews.setViewVisibility(R.id.btn_up,View.VISIBLE);
//                    remoteViews.setInt(R.id.btn_up, "setBackgroundResource", R.drawable.shape_arrow_black);
                    remoteViews.setInt(R.id.btn_up, "setImageResource", R.drawable.ic_keyboard_arrow_up_black_24dp);
//                    remoteViews.setTextViewText(R.id.tv_time_1, "5");
//                    remoteViews.setTextViewText(R.id.tv_time_2, "6");
//                    remoteViews.setTextViewText(R.id.tv_time_3, "7");
//                    remoteViews.setTextViewText(R.id.tv_time_4, "8");
                    if (campus == SharePreferenceLab.HUANGJIAHU){
                        remoteViews.setTextViewText(R.id.tv_time_1_start, HangjiahuStartTime[4]);
                        remoteViews.setTextViewText(R.id.tv_time_1_end, HangjiahuStartEnd[4]);
                        remoteViews.setTextViewText(R.id.tv_time_2_start, HangjiahuStartTime[5]);
                        remoteViews.setTextViewText(R.id.tv_time_2_end, HangjiahuStartEnd[5]);
                        remoteViews.setTextViewText(R.id.tv_time_3_start, HangjiahuStartTime[6]);
                        remoteViews.setTextViewText(R.id.tv_time_3_end, HangjiahuStartEnd[6]);
                        remoteViews.setTextViewText(R.id.tv_time_4_start, HangjiahuStartTime[7]);
                        remoteViews.setTextViewText(R.id.tv_time_4_end, HangjiahuStartEnd[7]);
//                remoteViews.setTextViewText(R.id.tv_time_2, "6");
//                remoteViews.setTextViewText(R.id.tv_time_3, "7");
//                remoteViews.setTextViewText(R.id.tv_time_4, "8");
                    }else{
                        remoteViews.setTextViewText(R.id.tv_time_1_start, QingShangStartTime[4]);
                        remoteViews.setTextViewText(R.id.tv_time_1_end, QingShangEndTime[4]);
                        remoteViews.setTextViewText(R.id.tv_time_2_start, QingShangStartTime[5]);
                        remoteViews.setTextViewText(R.id.tv_time_2_end, QingShangEndTime[5]);
                        remoteViews.setTextViewText(R.id.tv_time_3_start, QingShangStartTime[6]);
                        remoteViews.setTextViewText(R.id.tv_time_3_end, QingShangEndTime[6]);
                        remoteViews.setTextViewText(R.id.tv_time_4_start, QingShangStartTime[7]);
                        remoteViews.setTextViewText(R.id.tv_time_4_end, QingShangEndTime[7]);
                    }
                    SharePreferenceLab.getInstance().setIsAtBottom(context, true);
                    refreshSchedule(context, selectStudentId, selectSemester, thisWeek, remoteViews, true);

                }
                break;
            default:
                int campus = SharePreferenceLab.getInstance().getCampus(context);
                remoteViews.setViewVisibility(R.id.btn_up,View.INVISIBLE);
                remoteViews.setViewVisibility(R.id.btn_down,View.VISIBLE);
//                remoteViews.setInt(R.id.btn_down, "setBackgroundResource", R.drawable.shape_arrow_black);
                remoteViews.setInt(R.id.btn_down, "setImageResource", R.drawable.ic_keyboard_arrow_down_black_24dp);
//                remoteViews.setTextViewText(R.id.tv_time_1, "1");
//                remoteViews.setTextViewText(R.id.tv_time_2, "2");
//                remoteViews.setTextViewText(R.id.tv_time_3, "3");
//                remoteViews.setTextViewText(R.id.tv_time_4, "4");
                if (campus == SharePreferenceLab.HUANGJIAHU){
                    remoteViews.setTextViewText(R.id.tv_time_1_start, HangjiahuStartTime[0]);
                    remoteViews.setTextViewText(R.id.tv_time_1_end, HangjiahuStartEnd[0]);
                    remoteViews.setTextViewText(R.id.tv_time_2_start, HangjiahuStartTime[1]);
                    remoteViews.setTextViewText(R.id.tv_time_2_end, HangjiahuStartEnd[1]);
                    remoteViews.setTextViewText(R.id.tv_time_3_start, HangjiahuStartTime[2]);
                    remoteViews.setTextViewText(R.id.tv_time_3_end, HangjiahuStartEnd[2]);
                    remoteViews.setTextViewText(R.id.tv_time_4_start, HangjiahuStartTime[3]);
                    remoteViews.setTextViewText(R.id.tv_time_4_end, HangjiahuStartEnd[3]);
                }else{
                    remoteViews.setTextViewText(R.id.tv_time_1_start, QingShangStartTime[0]);
                    remoteViews.setTextViewText(R.id.tv_time_1_end, QingShangEndTime[0]);
                    remoteViews.setTextViewText(R.id.tv_time_2_start, QingShangStartTime[1]);
                    remoteViews.setTextViewText(R.id.tv_time_2_end, QingShangEndTime[1]);
                    remoteViews.setTextViewText(R.id.tv_time_3_start, QingShangStartTime[2]);
                    remoteViews.setTextViewText(R.id.tv_time_3_end, QingShangEndTime[2]);
                    remoteViews.setTextViewText(R.id.tv_time_4_start, QingShangStartTime[3]);
                    remoteViews.setTextViewText(R.id.tv_time_4_end, QingShangEndTime[3]);
                }
                SharePreferenceLab.getInstance().setIsAtBottom(context, false);
                refreshSchedule(context, selectStudentId, selectSemester, thisWeek, remoteViews, false);
                break;
        }

        appWidgetManager.updateAppWidget(new ComponentName(context, WeekScheduleWidgetProvider.class), remoteViews);

    }


    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);

    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }
    

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);

        widgetWidth = getWidgetWidth(context,appWidgetManager,appWidgetId);
        widgetHeight = getWidgetHeight(context,appWidgetManager,appWidgetId);

        SharePreferenceLab.setWidgetWeekWidth(widgetWidth);
        SharePreferenceLab.setWidgetWeekHeight(widgetHeight);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_week_schedule_layout);
        setBackground(remoteViews,context);

        appWidgetManager.updateAppWidget(new ComponentName(context,WeekScheduleWidgetProvider.class),remoteViews);

    }

    private static int getWidgetWidth(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        if(context.getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT){
            //竖屏
            return appWidgetManager.getAppWidgetOptions(appWidgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0);
        }else {
            return appWidgetManager.getAppWidgetOptions(appWidgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 0);
        }
    }

    private static int getWidgetHeight(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        if(context.getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT){
            //竖屏
            return appWidgetManager.getAppWidgetOptions(appWidgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 0);
        }else {
            return appWidgetManager.getAppWidgetOptions(appWidgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0);
        }
    }
    
    

    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        super.onRestored(context, oldWidgetIds, newWidgetIds);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_week_schedule_layout);
        appWidgetManager.updateAppWidget(oldWidgetIds, remoteViews);
//        Toast.makeText(context, "调用了 WeekScheduleWidgetProvider onRestored", Toast.LENGTH_SHORT).show();
    }
    @SuppressLint("LongLogTag")
    private void setBackground(RemoteViews parentRemoteViews, Context context) {
        try{
            //方法笨，而且不好用，希望以后能修改
            String stringAlpha= SharePreferenceLab.getInstance().getWidgetWeekBgAlpha(context);
            int alpha=java.lang.Integer.parseInt(stringAlpha);
            String path=SharePreferenceLab.getInstance().getWidgetWeekBgPath(context);
            if(path.equals("")){
                if(SharePreferenceLab.getInstance().getWidgetWeekBgRefreshType(context).equals("white")){
                    parentRemoteViews.setImageViewResource(R.id.widget_week_bg_imageView,R.drawable.shape_widget_bg_round_corners_100);
                    parentRemoteViews.setInt(R.id.widget_week_bg_imageView, "setImageAlpha", alpha);
                }else {
                    parentRemoteViews.setImageViewResource(R.id.widget_week_bg_imageView,R.drawable.shape_widget_bg_round_corners_100_black);
                    parentRemoteViews.setInt(R.id.widget_week_bg_imageView, "setImageAlpha", alpha);
                }
            }else{ 
                Bitmap bitmap = MyBitmapTool.getRoundedCornerBitmap_from_PathFromString(context,path,alpha,widgetWidth,widgetHeight);
//        bitmap=MyBitmapTool.compressBitmap(bitmap,0);
                if(bitmap==null){
                    parentRemoteViews.setImageViewResource(R.id.widget_week_bg_imageView,R.drawable.shape_widget_bg_round_corners_100);
                    parentRemoteViews.setInt(R.id.widget_week_bg_imageView, "setImageAlpha", alpha);
                }else{
                    parentRemoteViews.setImageViewBitmap(R.id.widget_week_bg_imageView,bitmap);
                }
            }

        }catch (Exception e){
            SharePreferenceLab.getInstance().setWidgetWeekBgPath(context,"");
        }
    }

    public static void sendRefreshBroadcast(Context context){
        Intent intent = new Intent(REFRESH_ACTION);
        intent.setComponent(new ComponentName(context,WeekScheduleWidgetProvider.class));
        context.sendBroadcast(intent);
    }
    private static void setTextColor(RemoteViews views, Context context) {
        String textColor= SharePreferenceLab.getInstance().getWidgetWeekTextColor(context);
        if(!textColor.equals("")){
            int Color=java.lang.Integer.parseInt(textColor);
            views.setTextColor(R.id.tv_weekday,Color);
            views.setTextColor(R.id.tv_week,Color);
        }
    }

}

