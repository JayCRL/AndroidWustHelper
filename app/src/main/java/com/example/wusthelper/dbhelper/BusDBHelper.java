package com.example.wusthelper.dbhelper;

import static com.example.wusthelper.utils.ResourcesUtils.getRealString;

import android.annotation.SuppressLint;

import com.example.wusthelper.R;
import com.example.wusthelper.bean.javabean.SchoolBusBean;


import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Gong Yunhao
 * @version: V1.0
 * @date: 2018/10/20
 * @github https://github.com/Roman-Gong
 * @blog https://www.jianshu.com/u/52a8fa1f29fb
 */
public class BusDBHelper {

    public static List<SchoolBusBean> findAll() {
        return LitePal.findAll(SchoolBusBean.class);
    }

    public static void deleteAll() {
        LitePal.deleteAll(SchoolBusBean.class);
    }

    // 校车时刻表没有录入数据库时调用此方法，生成数据
    @SuppressLint("NewApi")
    public static void initBusDB() {
        if (LitePal.findAll(SchoolBusBean.class).size() == 0) {
            deleteAll();

            List<String> hourlist = new ArrayList<>();
            List<String> minutelist = new ArrayList<>();

            SchoolBusBean busBean1 = new SchoolBusBean();
            busBean1.setStarting(getRealString(R.string.qingshan));
            busBean1.setDestination(getRealString(R.string.huangjiahu));
            busBean1.setWorkDay(1);
            hourlist.add("6");
            hourlist.add("6");
            hourlist.add("8");
            hourlist.add("12");
            hourlist.add("14");
            hourlist.add("17");
            busBean1.setHours(hourlist);
            minutelist.add("30");
            minutelist.add("50");
            minutelist.add("30");
            minutelist.add("50");
            minutelist.add("20");
            minutelist.add("30");
            busBean1.setMinutes(minutelist);
            busBean1.save();

            SchoolBusBean busBean2 = new SchoolBusBean();
            minutelist.clear();
            hourlist.clear();
            busBean2.setStarting(getRealString(R.string.huangjiahu));
            busBean2.setDestination(getRealString(R.string.qingshan));
            busBean2.setWorkDay(1);
            hourlist.add("6");
            hourlist.add("10");
            hourlist.add("12");
            hourlist.add("16");
            hourlist.add("17");
            hourlist.add("17");
            hourlist.add("20");
            busBean2.setHours(hourlist);
            minutelist.add("40");
            minutelist.add("15");
            minutelist.add("10");
            minutelist.add("10");
            minutelist.add("0");
            minutelist.add("55");
            minutelist.add("50");
            busBean2.setMinutes(minutelist);
            busBean2.save();

            SchoolBusBean busBean3 = new SchoolBusBean();
            minutelist.clear();
            hourlist.clear();
            busBean3.setStarting(getRealString(R.string.hongshan));
            busBean3.setDestination(getRealString(R.string.huangjiahu));
            busBean3.setWorkDay(1);
            hourlist.add("6");
            hourlist.add("6");
            hourlist.add("6");
            hourlist.add("8");
            hourlist.add("13");
            busBean3.setHours(hourlist);
            minutelist.add("30");
            minutelist.add("40");
            minutelist.add("50");
            minutelist.add("50");
            minutelist.add("0");
            busBean3.setMinutes(minutelist);
            busBean3.save();

            SchoolBusBean busBean4 = new SchoolBusBean();
            minutelist.clear();
            hourlist.clear();
            busBean4.setStarting(getRealString(R.string.huangjiahu));
            busBean4.setDestination(getRealString(R.string.hongshan));
            busBean4.setWorkDay(1);
            hourlist.add("10");
            hourlist.add("12");
            hourlist.add("16");
            hourlist.add("17");
            hourlist.add("17");
            busBean4.setHours(hourlist);
            minutelist.add("15");
            minutelist.add("10");
            minutelist.add("10");
            minutelist.add("0");
            minutelist.add("55");
            busBean4.setMinutes(minutelist);
            busBean4.save();

            SchoolBusBean busBean5 = new SchoolBusBean();
            minutelist.clear();
            hourlist.clear();
            busBean5.setStarting(getRealString(R.string.qingshan));
            busBean5.setDestination(getRealString(R.string.hongshan));
            busBean5.setWorkDay(1);
            hourlist.add("7");
            hourlist.add("12");
            hourlist.add("16");
            hourlist.add("17");
            busBean5.setHours(hourlist);
            minutelist.add("0");
            minutelist.add("0");
            minutelist.add("10");
            minutelist.add("20");
            busBean5.setMinutes(minutelist);
            busBean5.save();

            SchoolBusBean busBean6 = new SchoolBusBean();
            minutelist.clear();
            hourlist.clear();
            busBean6.setStarting(getRealString(R.string.hongshan));
            busBean6.setDestination(getRealString(R.string.qingshan));
            busBean6.setWorkDay(1);
            hourlist.add("7");
            hourlist.add("9");
            hourlist.add("13");
            hourlist.add("17");
            busBean6.setHours(hourlist);
            minutelist.add("0");
            minutelist.add("10");
            minutelist.add("10");
            minutelist.add("20");
            busBean6.setMinutes(minutelist);
            busBean6.save();

            SchoolBusBean busBean7 = new SchoolBusBean();
            minutelist.clear();
            hourlist.clear();
            busBean7.setStarting(getRealString(R.string.qingshan));
            busBean7.setDestination(getRealString(R.string.huangjiahu));
            busBean7.setWorkDay(0);
            hourlist.add("6");
            busBean7.setHours(hourlist);
            minutelist.add("50");
            busBean7.setMinutes(minutelist);
            busBean7.save();

            SchoolBusBean busBean8 = new SchoolBusBean();
            minutelist.clear();
            hourlist.clear();
            busBean8.setStarting(getRealString(R.string.huangjiahu));
            busBean8.setDestination(getRealString(R.string.qingshan));
            busBean8.setWorkDay(0);
            hourlist.add("17");
            busBean8.setHours(hourlist);
            minutelist.add("0");
            busBean8.setMinutes(minutelist);
            busBean8.save();
        }

    }

}
