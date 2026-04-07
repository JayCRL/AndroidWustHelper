package com.example.wusthelper.helper;

import android.annotation.SuppressLint;
import android.util.Log;

import com.example.wusthelper.MyApplication;
import com.example.wusthelper.bean.javabean.DateBean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TimeTools {

    private final static String FORMAT_DATE = "yyyy-MM-dd";
    private final static String FORMAT_YEAR = "yyyy";
    private final static String FORMAT_MONTH = "MM";
    private final static String FORMAT_DAY = "dd";

    private final static String FORMAT_HOURS = "HH";
    private final static String FORMAT_MINUTES = "mm";
    private final static String FORMAT_SECOND = "ss";
  
    private static final String TAG = "TimeTools";

    @SuppressLint("SimpleDateFormat")
    public static String getFormatToday() {

        Date currentDate = new Date();
        SimpleDateFormat simpleDateFormat;
        simpleDateFormat = new SimpleDateFormat(FORMAT_DATE);
        return simpleDateFormat.format(currentDate);

    }

    //判断str所在的周次与缓存相比较  返回str的周次  如果小于0   则将缓存标记改为VACATION
    /*因为之前的缓存是保存下当前刷新课表时间相对于本次选择的学期的开学时间的相对周次   所以如果以后只是想要记录当前时间
    所在的周次的话就调用getRealWeek（）    如果想要显示周次的话就调用getWeek（）函数
    区别在于   ：
    getRealWeek（）保存的是真实的值   如果为负数也是保存为负数
    getWeek（）保存的是正确的值   如果为负数则直接返回0  同时缓存的IS_VACATION发生变化

     */
    @SuppressLint("SimpleDateFormat")
    public static int getWeek(DateBean dateBean, String str) {

        SimpleDateFormat format;
        format = new SimpleDateFormat(FORMAT_DATE);
        Date startDate = null;
        try {
            startDate = format.parse(dateBean.getDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date currentDate = null;
        try {
            currentDate = format.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //如果date有为空的情况，则直接返回
        if( startDate == null || currentDate ==null) return 0;

        int gap = dateBetween(startDate, currentDate);
        if(SharePreferenceLab.getInstance().getIsChooseSundayFirst(MyApplication.getContext())){
            gap = gap + dateBean.getWeekday() ;
        }else{
            gap = gap + dateBean.getWeekday() -1;
        }


        int result = gap/7 + dateBean.getWeek();

        Log.e(TAG, "getWeek: gap === "+gap);
        Log.e(TAG, "getWeek: dateBean.getDate() == "+dateBean.getDate() );
        Log.e(TAG, "getWeek: dateBean.getWeek() == "+ dateBean.getWeek() );

        Log.e(TAG, "getWeek: result === "+result );




        if (result > 25){
            Log.e(TAG, "getWeek: 进入缓存1" );
            SharePreferenceLab.getInstance().set_is_vacationing(MyApplication.getContext(),SharePreferenceLab.OVER);
            return 25;
        }
        else if(result < 1){
            Log.e(TAG, "getWeek: 进入缓存2" +result);
            SharePreferenceLab.getInstance().set_is_vacationing(MyApplication.getContext(),SharePreferenceLab.VACATIONING);
            return 0;
        }else{
            Log.e(TAG, "getWeek: 进入缓存3" +result);
            SharePreferenceLab.getInstance().set_is_vacationing(MyApplication.getContext(),SharePreferenceLab.CLASSING);
        }
        return result;

    }

    //返回str所在的周次   可以为负数
    @SuppressLint("SimpleDateFormat")
    public static int getRealWeek(DateBean dateBean, String str) {

        SimpleDateFormat format;
        format = new SimpleDateFormat(FORMAT_DATE);
        Date startDate = null;
        try {
            startDate = format.parse(dateBean.getDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date currentDate = null;
        try {
            currentDate = format.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int gap = dateBetween(startDate, currentDate);
        Log.e(TAG, "getRealWeek:before=  "+gap );
        gap = gap + dateBean.getWeekday() - 1;

        Log.e(TAG, "getRealWeek:after "+gap );

        int result = gap/7 + dateBean.getWeek();


        Log.e(TAG, "getRealWeek: "+result );

        //开学周为第一周  所以：     (有0！！！)
        //如果为-45的话  当前周应该是第-6周  如果为45的话   当前周应该是第7周
        //如果为-5的话   当前周为第0周   如果为5天的话   当前周为第1周
        if(result >=0&&gap >=0){
            result += 1;
        }
        //如果为-42的话   当前周为第-5周
        if(gap < 0 && gap %7==0){
            result ++;
        }
        if (result > 25){
            return 25;
        }
        return result;
    }

    public static int getWeekday() {

        Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        if (calendar.get(Calendar.DAY_OF_WEEK)-1 == 0)
            return 7;
        else
            return calendar.get(Calendar.DAY_OF_WEEK) - 1;

    }

    public static int getWeekday(Date date) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        if (calendar.get(Calendar.DAY_OF_WEEK)-1 == 0)
            return 7;
        else
            return calendar.get(Calendar.DAY_OF_WEEK) - 1;

    }


    public static List<DateBean> getDateInAWeek(DateBean dateBean, int week) {

        List<DateBean> results = new ArrayList<>();
        String startDateStr = dateBean.getDate();
        Date startDate = getDate(startDateStr);
        //如果为空就直接返回，防止空指针异常
        if(startDate ==null)  return results;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.add(Calendar.WEEK_OF_YEAR, week-dateBean.getWeek());

        calendar.add(Calendar.DATE, 1-dateBean.getWeekday());

        for (int i=0; i<7; i++) {
            Date tempDate = calendar.getTime();
            int year = getYear(tempDate);
            int month = getMonth(tempDate);
            int day = getDay(tempDate);
            results.add(new DateBean(null,year, month, day));
            calendar.add(Calendar.DATE, 1);
        }
        return results;

    }


    public static List<DateBean> getDateInAWeekSundayFirst(DateBean dateBean, int week) {

        List<DateBean> results = new ArrayList<>();
        String startDateStr = dateBean.getDate();
        Date startDate = getDate(startDateStr);
        //如果为空就直接返回，防止空指针异常
        if(startDate ==null)  return results;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.setFirstDayOfWeek(Calendar.SUNDAY);
        calendar.add(Calendar.WEEK_OF_YEAR, week-dateBean.getWeek());

        calendar.add(Calendar.DATE, -dateBean.getWeekday());


        for (int i=0; i<7; i++) {

            Date tempDate = calendar.getTime();
            int year = getYear(tempDate);
            int month = getMonth(tempDate);
            int day = getDay(tempDate);
            results.add(new DateBean(null,year, month, day));
            calendar.add(Calendar.DATE, 1);
        }
        return results;

    }


    @SuppressLint("SimpleDateFormat")
    public static Date getDate(String str) {

        SimpleDateFormat format;
        format = new SimpleDateFormat(FORMAT_DATE);
        try {
            return format.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;

    }

    @SuppressLint("SimpleDateFormat")
    private static int getYear(Date date) {

        SimpleDateFormat format;
        format = new SimpleDateFormat(FORMAT_YEAR);
        String str = format.format(date);
        return Integer.parseInt(str);

    }

    @SuppressLint("SimpleDateFormat")
    public static int getMonth(Date date) {

        SimpleDateFormat format;
        format = new SimpleDateFormat(FORMAT_MONTH);
        String str = format.format(date);
        return Integer.parseInt(str);

    }

    @SuppressLint("SimpleDateFormat")
    public static String getMonth() {
        Date currentDate = new Date();
        SimpleDateFormat simpleDateFormat;
        simpleDateFormat = new SimpleDateFormat(FORMAT_MONTH);
        return simpleDateFormat.format(currentDate);
    }


    @SuppressLint("SimpleDateFormat")
    public static int getDay(Date date) {

        SimpleDateFormat format;
        format = new SimpleDateFormat(FORMAT_DAY);
        String str = format.format(date);
        return Integer.parseInt(str);

    }

    @SuppressLint("SimpleDateFormat")
    public static String getDay() {
        Date currentDate = new Date();
        SimpleDateFormat simpleDateFormat;
        simpleDateFormat = new SimpleDateFormat(FORMAT_DAY);
        return simpleDateFormat.format(currentDate);
    }


    private static int dateBetween(Date startDate, Date endDate) {

        return (int) ((endDate.getTime() - startDate.getTime())/1000/60/60/24);

    }

    /**
     * 获取客户端计算的“最新学期”。
     * 规则（按你确认的简化版）：
     * - 每年 8 月 15 日（含）起视为新学年第一学期：year-(year+1)-1
     * - 否则视为上一学年第二学期：(year-1)-year-2
     */
    public static String getLatestSemester() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        boolean isFirstSemester;
        if (month > 8) {
            isFirstSemester = true;
        } else if (month < 8) {
            isFirstSemester = false;
        } else {
            // month == 8
            isFirstSemester = day >= 15;
        }

        StringBuilder builder = new StringBuilder();
        if (isFirstSemester) {
            builder.append(year).append('-').append(year + 1).append('-').append(1);
        } else {
            builder.append(year - 1).append('-').append(year).append('-').append(2);
        }
        return builder.toString();
    }

    public static boolean isToday(DateBean dateBean){
        Date currentDate = new Date();
        return dateBean.getYear() == getYear(currentDate) && dateBean.getMonth() == getMonth(currentDate) && dateBean.getDay() == getDay(currentDate);
    }

    @SuppressLint("SimpleDateFormat")
    public static String getDateFromTime(long time){
        String res ="";
        Date timesDate = new Date();
        timesDate.setTime(time);
        SimpleDateFormat simpleDateFormat;
        simpleDateFormat = new SimpleDateFormat(FORMAT_DATE);
        return simpleDateFormat.format(timesDate);
    }

    public static int getCurrentWeek(){
        DateBean dateBean = SharePreferenceLab.getDateBean();
        return TimeTools.getWeek(dateBean, TimeTools.getFormatToday());
    }
    public static int getCurrentWeek_from_Date(DateBean dateBean){
        return TimeTools.getWeek(dateBean, TimeTools.getFormatToday());
    }



      
    @SuppressLint("SimpleDateFormat")
    public static String getDateNextXDay(int i) {
        SimpleDateFormat sf = new SimpleDateFormat("MM月dd日");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, i);
        return sf.format(c.getTime());
    }

    @SuppressLint("SimpleDateFormat")
    public static String getFormatHours() {

        Date currentDate = new Date();
        SimpleDateFormat simpleDateFormat;
        simpleDateFormat = new SimpleDateFormat(FORMAT_HOURS);
        return simpleDateFormat.format(currentDate);

    }

    @SuppressLint("SimpleDateFormat")
    public static String getFormatMinutes() {

        Date currentDate = new Date();
        SimpleDateFormat simpleDateFormat;
        simpleDateFormat = new SimpleDateFormat(FORMAT_MINUTES);
        return simpleDateFormat.format(currentDate);

    }

    @SuppressLint("SimpleDateFormat")
    public static String getFormatSecond() {

        Date currentDate = new Date();
        SimpleDateFormat simpleDateFormat;
        simpleDateFormat = new SimpleDateFormat(FORMAT_SECOND);
        return simpleDateFormat.format(currentDate);

    }

    //课程结束时间距离早上8：00点的分钟数、、黄家湖校区
    private static final int[] startTimeHUANGJIAHUList = {0,120,235,470,580,760,870};

    //课程结束时间距离早上8：00点的分钟数、、青山校区
    private static final int[] startTimeQINGSHANList = {0,100,230,470,580,760,870};

    /**
     * 获取当前时间所对应的课程时间,原理是获取当前时间，然后减去8:00，然后计算当前的课程时间
     * 比如现在是早上8.30,时间差30分，在数组对应中 小于120 对应黄家湖课表的 第1个课程时间
     * 一天最多有六节课，所以返回值是 1 - 6,如果返回 7，那么代表时间超过了最晚的课，今天就没课了
     * */
    public static int getFormatSection(){

        int section = 1;
        int hour = Integer.parseInt(TimeTools.getFormatHours());
        int minutes = Integer.parseInt(TimeTools.getFormatMinutes());
        int distance = hour*60+minutes-8*60;

        if(distance<0)
            return section;
        int position = 1;
        for(; position<=6;position++){
            if(distance<getSectionDistance(position)){
                section = position;
                break;
            }
        }

        if(position==7)
            section=position;

        return section;
    }

    private static int getSectionDistance(int position) {
        if(SharePreferenceLab.getCampus()==SharePreferenceLab.HUANGJIAHU){
            return startTimeHUANGJIAHUList[position];
        }else {
            return startTimeQINGSHANList[position];
        }
    }
}
