package com.example.wusthelper.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author Asche
 * @github asche910
 * @date 2019年8月1日
 */
public class CountDownUtils {

    public static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    /**
     * 检测目标时间是否为将来时间
     * @param time
     * @return
     */
    public static boolean isFuture(long time){
        return time > System.currentTimeMillis();
    }


    public static long getTimeLong(String time){
        try {
            Date date = format.parse(time);
            return date == null ? 0:date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 检查时间的大小
     * @param target
     * @return
     */
    public static boolean checkState(long target){
        long cur = System.currentTimeMillis();
        return target > cur;
    }

    /**
     * 由考试时间戳计算出倒计时剩余天数
     * @param target
     * @return
     */
    public static int getRemainDays(long target){
        return getDays(System.currentTimeMillis(), target);
    }

    /**
     * 由开始时间戳计算出倒计时开始天数
     * @param start
     * @return
     */
    public static int getStartDays(long start){
        return getDays(start, System.currentTimeMillis());
    }

    public static String getShowTime(long target){
        Date date = new Date(target);
        return CountDownUtils.format.format(date);
    }

    /**
     * 计算两个时间戳间隔的天数
     * @param start
     * @param end
     * @return
     */
    private static int getDays(long start, long end){
        Calendar curCal = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
        curCal.setTimeInMillis(start);
        curCal.set(curCal.get(Calendar.YEAR), curCal.get(Calendar.MONTH), curCal.get(Calendar.DAY_OF_MONTH), 0, 0, 1);

        Date date = new Date(end);
        Calendar targetCal = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
        targetCal.setTime(date);
        targetCal.set(targetCal.get(Calendar.YEAR), targetCal.get(Calendar.MONTH), targetCal.get(Calendar.DAY_OF_MONTH), 0, 0, 1);

        int days =  (int)(targetCal.getTimeInMillis() / 1000 - curCal.getTimeInMillis() / 1000) / 86400;
        System.out.println("Get days ---> " + days);
        return days;
    }
}
