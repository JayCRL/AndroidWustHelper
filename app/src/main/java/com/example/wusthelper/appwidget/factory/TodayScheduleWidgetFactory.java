package com.example.wusthelper.appwidget.factory;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.wusthelper.R;
import com.example.wusthelper.bean.itembean.TodayCourseListForWidget;
import com.example.wusthelper.dbhelper.WidgetCourseDB;
import com.example.wusthelper.helper.DrawableLab;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.ui.activity.MainMvpActivity;
import com.example.wusthelper.utils.ResourcesUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TodayScheduleWidgetFactory implements RemoteViewsService.RemoteViewsFactory {

    private static final String TAG = "TodayScheduleWidgetFact";

    HashMap<Integer, String > startMap  = new HashMap<Integer, String>(){{
        put(1,"上午8:20");
        put(2,"上午10:20");
        put(3,"下午2:00");
        put(4,"下午4:00");
        put(5,"晚上6:40");
        put(6,"晚上8:30");
    }};
    HashMap<Integer, String > endMap  = new HashMap<Integer, String>(){{
        put(1,"上午10:00");
        put(2,"上午12:00");
        put(3,"下午3:40");
        put(4,"傍晚5:40");
        put(5,"晚上8:20");
        put(6,"晚上10:10");
    }};

    //青山校区时间
    HashMap<Integer, String > startMap_QINGSAN  = new HashMap<Integer, String>(){{
        put(1,"上午8:00");
        put(2,"上午10:10");
        put(3,"下午2:00");
        put(4,"下午4:00");
        put(5,"晚上6:40");
        put(6,"晚上8:30");
    }};
    HashMap<Integer, String > endMap_QINGSAN  = new HashMap<Integer, String>(){{
        put(1,"上午9:40");
        put(2,"上午11:50");
        put(3,"下午3:40");
        put(4,"傍晚5:40");
        put(5,"晚上8:20");
        put(6,"晚上10:10");
    }};

    private static List<TodayCourseListForWidget> mList=new ArrayList<>() ;
    private static Context mContext;

    public TodayScheduleWidgetFactory(Context mContext,List<TodayCourseListForWidget> mList) {
        Log.d(TAG, "TodayScheduleWidgetFactory: ");
        this.mList = mList;
        this.mContext = mContext;

    }

    @Override
    public RemoteViews getViewAt(int position) {
//        Log.d(TAG, "getViewAt: ");
        //  HashMap<String, Object> map;

        // 设置 第position位的“视图”的数据
        TodayCourseListForWidget item = mList.get(position);



        RemoteViews rv;

//        Log.d(TAG, "getViewAt: "+item);

        if(item.getItemType()==TodayCourseListForWidget.DATE_TYPE){

            // 获取 item_widget_today_date.xml 对应的RemoteViews
            rv = new RemoteViews(mContext.getPackageName(), R.layout.item_widget_today_date);
            rv.setTextViewText(R.id.tv_item_widget_today_date,item.getDate());

            String stringColor= SharePreferenceLab.getInstance().getWidgetTodayTextColor(mContext);
            int color=java.lang.Integer.parseInt(stringColor);
            rv.setTextColor(R.id.tv_item_widget_today_date,color);

        }else if (item.getItemType()==TodayCourseListForWidget.EMPTY_TYPE){

            // 获取 item_widget_today_empty.xml 对应的RemoteViews
            rv = new RemoteViews(mContext.getPackageName(), R.layout.item_widget_today_empty);

            String stringColor= SharePreferenceLab.getInstance().getWidgetTodayTextColor(mContext);
            int color=java.lang.Integer.parseInt(stringColor);
            if(stringColor.equals("-16777216")){
                //主标题是黑色字体，证明是白色主题，改变副标题颜色（加深）
                rv.setTextColor(R.id.tv_item_widget_today_empty, ResourcesUtils.getRealColor(R.color.colorDarkGray));
            }else if(stringColor.equals("-1")){
                //白色字体，证明是黑色主题，改变副标题颜色（变浅）
                rv.setTextColor(R.id.tv_item_widget_today_empty, ResourcesUtils.getRealColor(R.color.colorGray));
            }

        }else {

            // 获取 item_widget_courseBean.xml 对应的RemoteViews
            rv = new RemoteViews(mContext.getPackageName(), R.layout.item_widget_today);

            setTextColor(rv);

            //  rv.setImageViewResource(R.id.iv_lock, ((Integer) map.get(IMAGE_ITEM)).intValue());

            rv.setInt(R.id.rl_widget_today_class_color, "setBackgroundResource",
                    DrawableLab.getResourceID(item.getCourseBean().getColor()));
            //判断校区，不同校区，时间不一样
            if(SharePreferenceLab.getCampus()==SharePreferenceLab.HUANGJIAHU){
                rv.setTextViewText(R.id.tv_widget_today_startTime, startMap.get(item.getCourseBean().getStartTime()));
                rv.setTextViewText(R.id.tv_widget_today_endTime, endMap.get(item.getCourseBean().getStartTime()));
            }else {
                rv.setTextViewText(R.id.tv_widget_today_startTime, startMap_QINGSAN.get(item.getCourseBean().getStartTime()));
                rv.setTextViewText(R.id.tv_widget_today_endTime, endMap_QINGSAN.get(item.getCourseBean().getStartTime()));
            }

            rv.setTextViewText(R.id.tv_widget_today_className, item.getCourseBean().getCourseName());

            rv.setTextViewText(R.id.tv_widget_today_place_and_teacher, item.getCourseBean().getClassRoom()+" | " +
                    item.getCourseBean().getTeacherName());

            // 设置 第position位的“视图”对应的响应事件
            // 通过Intent putExtra传值，则只能传一次，所以我选择用缓存，再在MainMvpActivity进行逻辑判断，达到多次相应事件
            Intent intent = MainMvpActivity.newInstance(mContext);
            intent.putExtra("kind",MainMvpActivity.WIDGET);
            intent.putExtra("courseID", item.getCourseBean().getId());
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            rv.setOnClickFillInIntent(R.id.lv_widget_today, intent);
            rv.setOnClickFillInIntent(R.id.rl_widget_today_all, intent);

        }
        return rv;

    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    public static void refresh(){
        if(mList.size()>0)
            mList.clear();
        mList.addAll(WidgetCourseDB.getTodayCourseListForWidget());
        
       // Log.d(TAG, "refresh: "+mList);
    }

    @Override
    public int getCount() {
        // 返回“集合视图”中的数据的总数
        return mList.size();
    }

    @Override
    public long getItemId(int position) {
        // 返回当前项在“集合视图”中的位置
        return position;
    }


    @Override
    public int getViewTypeCount() {
        // 只有一类 ListView
        return mList.size();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: ");
    }

    @Override
    public void onDataSetChanged() {
        Log.d(TAG, "onDataSetChanged: ");
    }

    @Override
    public void onDestroy() {
        mList.clear();
    }


    private void setTextColor(RemoteViews remoteViews) {

        String stringColor= SharePreferenceLab.getInstance().getWidgetTodayTextColor(mContext);
        Log.d(TAG, "setTextColor: "+stringColor);

        int Color=java.lang.Integer.parseInt(stringColor);
        remoteViews.setTextColor(R.id.tv_widget_today_startTime,Color);
        remoteViews.setTextColor(R.id.tv_widget_today_className,Color);

        if(stringColor.equals("-16777216")){
            remoteViews.setTextColor(R.id.tv_widget_today_endTime,ResourcesUtils.getRealColor(R.color.colorDarkGray));
            remoteViews.setTextColor(R.id.tv_widget_today_place_and_teacher,ResourcesUtils.getRealColor(R.color.colorDarkGray));
        }else if(stringColor.equals("-1")){
            //白色字体，证明是黑色主题，改变副标题颜色（变浅）
            remoteViews.setTextColor(R.id.tv_widget_today_endTime,ResourcesUtils.getRealColor(R.color.colorGray));
            remoteViews.setTextColor(R.id.tv_widget_today_place_and_teacher, ResourcesUtils.getRealColor(R.color.colorGray));
        }
    }

}