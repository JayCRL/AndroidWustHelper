package com.example.wusthelper.appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.example.wusthelper.appwidget.factory.TodayScheduleWidgetFactory;
import com.example.wusthelper.appwidget.service.*;
import com.example.wusthelper.R;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.helper.TimeTools;
import com.example.wusthelper.ui.activity.CountdownActivity;
import com.example.wusthelper.ui.activity.MainMvpActivity;
import com.example.wusthelper.ui.activity.WidgetSettingsActivity;
import com.example.wusthelper.utils.DensityUtil;
import com.example.wusthelper.utils.MyBitmapTool;
import com.example.wusthelper.utils.ResourcesUtils;
import com.example.wusthelper.utils.ScreenUtils;
import com.example.wusthelper.utils.ToastUtil;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

public class TodayScheduleWidgetProvider extends AppWidgetProvider {

    private static final String TAG = "WIDGET";

    public static final String REFRESH="com.example.wusthelper.appwidget.REFRESH";
    public static final String SETTING_CHANGE="com.example.wusthelper.appwidget.SETTING_CHANGE";
    public static final String UPDATE_ALL="com.example.wusthelper.appwidget.UPDATE_ALL";

    public static final String COLLECTION_VIEW_EXTRA = "android.appwidget.COLLECTION_VIEW_EXTRA";

    private static  String[] weekdays = {"","周一", "周二", "周三", "周四", "周五", "周六", "周日"};

    //小组件当前的宽高
    private static int widgetWidth ;

    private static int widgetHeight ;


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
//        Log.d(TAG, "onUpdate: "+appWidgetIds);
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // 获取AppWidget对应的视图
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_today_schedule_layout);

        widgetWidth = SharePreferenceLab.getWidgetTodayWidth();
        widgetHeight = SharePreferenceLab.getWidgetTodayHeight();
//        widgetWidth = getWidgetWidth(context,appWidgetManager,appWidgetId);
//        widgetHeight = getWidgetHeight(context,appWidgetManager,appWidgetId);

        initView(remoteViews,context);
        initData(appWidgetManager,remoteViews,context);

        setBackground(remoteViews,context);

        // 调用集合管理器对集合进行更新
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d(TAG, "onReceive: ");
        String action = intent.getAction();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_today_schedule_layout);

        if (action.equals(REFRESH)) {
            Log.e(TAG,"课表刷新成功");
            ToastUtil.showShortToastCenter("课程表刷新成功");
        }




        initView(remoteViews,context);
        initData(appWidgetManager,remoteViews,context);

        widgetWidth = SharePreferenceLab.getWidgetTodayWidth();
        widgetHeight = SharePreferenceLab.getWidgetTodayHeight();
        setBackground(remoteViews,context);

        appWidgetManager.updateAppWidget(new ComponentName(context,TodayScheduleWidgetProvider.class),remoteViews);

    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        // 当某一个widget被删除的时候 会执行ondelete方法
//        Intent intent = new Intent(context, UpdateWidgetService.class);
//        context.stopService(intent);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        // widget第一次创建的时候 执行的方法
        // 初始化widget数据的操作,开启后台服务等
//        Intent intent = new Intent(context, UpdateWidgetService.class);
//        context.startService(intent);
    }


    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);

        widgetWidth = getWidgetWidth(context,appWidgetManager,appWidgetId);
        widgetHeight = getWidgetHeight(context,appWidgetManager,appWidgetId);

        SharePreferenceLab.setWidgetTodayWidth(widgetWidth);
        SharePreferenceLab.setWidgetTodayHeight(widgetHeight);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_today_schedule_layout);
        setBackground(remoteViews,context);

        Log.d(TAG, "onAppWidgetOptionsChanged: ");

        appWidgetManager.updateAppWidget(new ComponentName(context,TodayScheduleWidgetProvider.class),remoteViews);

    }



    private static void initView(RemoteViews remoteViews, Context context) {
        // 设置响应 “按钮(bt_refresh)” 的intent
//            Intent btIntent = new Intent().setAction(REFRESH);
//            PendingIntent btPendingIntent = PendingIntent.getBroadcast(context, 0, btIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//            remoteViews.setOnClickPendingIntent(R.id.btn_widget_refresh, btPendingIntent);

        Intent settingsIntent = new Intent(context, WidgetSettingsActivity.class);

        settingsIntent.putExtra("widgetType",1);
        settingsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent  settings = PendingIntent.getActivity(context,1,settingsIntent,PendingIntent.FLAG_CANCEL_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.btn_today_settings,settings);


        Intent refreshIntent = new Intent(context, TodayScheduleWidgetProvider.class);
        refreshIntent.setAction(REFRESH);
        refreshIntent.setComponent(new ComponentName(context,TodayScheduleWidgetProvider.class));
        PendingIntent intent1 = PendingIntent.getBroadcast(context,0,refreshIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.btn_widget_refresh,intent1);



        // 设置 “ListView” 的adapter。
        // (01) intent: 对应启动 ListWidgetService(RemoteViewsService) 的intent
        // (02) setRemoteAdapter: 设置 gridview的适配器
        //    通过setRemoteAdapter将ListView和ListWidgetService关联起来，
        //    以达到通过 ListWidgetService 更新 ListView的目的
        Intent serviceIntent = new Intent(context, TodayScheduleWidgetService.class);
        remoteViews.setRemoteAdapter(R.id.lv_widget_today, serviceIntent);
        remoteViews.setEmptyView(R.id.lv_widget_today,R.id.tv_widget_today_empty);
        remoteViews.setScrollPosition(R.id.lv_widget_today,5);

        // 设置响应 “ListView” 的intent模板
        // 说明：“集合控件(如GridView、ListView、StackView等)”中包含很多子元素，如GridView包含很多格子。
        //     它们不能像普通的按钮一样通过 setOnClickPendingIntent 设置点击事件，必须先通过两步。
        //        (01) 通过 setPendingIntentTemplate 设置 “intent模板”，这是比不可少的！
        //        (02) 然后在处理该“集合控件”的RemoteViewsFactory类的getViewAt()接口中 通过 setOnClickFillInIntent 设置“集合控件的某一项的数据”

        Intent detailIntent = MainMvpActivity.newInstance(context);
        PendingIntent detailPending = PendingIntent.getActivity(context,0,detailIntent,0);
        remoteViews.setPendingIntentTemplate(R.id.lv_widget_today,detailPending);

//        remoteViews.setTextColor(R.id.tv_widget_today_date, Color.BLACK);

        String stringColor= SharePreferenceLab.getInstance().getWidgetTodayTextColor(context);
        int color=java.lang.Integer.parseInt(stringColor);
        remoteViews.setTextColor(R.id.tv_widget_today_date, color);
        if(stringColor.equals("-16777216")){
            //黑色字体，证明是白色主题，改变副标题颜色（加深）
            remoteViews.setTextColor(R.id.tv_widget_today_weeks, ResourcesUtils.getRealColor(R.color.colorDarkGray));
        }else if(stringColor.equals("-1")){
            //白色字体，证明是黑色主题，改变副标题颜色（变浅）
            remoteViews.setTextColor(R.id.tv_widget_today_weeks, ResourcesUtils.getRealColor(R.color.colorGray));
        }

    }


    private static void initData(AppWidgetManager appWidgetManager,RemoteViews remoteViews, Context context) {

        TodayScheduleWidgetFactory.refresh();
        remoteViews.setScrollPosition(R.id.lv_widget_today,0);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetManager.getAppWidgetIds(new ComponentName(context,TodayScheduleWidgetProvider.class)),
                R.id.lv_widget_today);

        Log.d(TAG, "initData: "+TimeTools.getWeekday());
        if(TimeTools.getCurrentWeek() == 0){
            remoteViews.setTextViewText(R.id.tv_widget_today_date,"假期中");
            remoteViews.setViewVisibility(R.id.tv_widget_today_weeks, View.INVISIBLE);
        }else{
            remoteViews.setTextViewText(R.id.tv_widget_today_date,
                    TimeTools.getMonth()+"月"+TimeTools.getDay()+"日");
            remoteViews.setTextViewText(R.id.tv_widget_today_weeks,"第"+TimeTools.getCurrentWeek()+
                    "周 "+weekdays[TimeTools.getWeekday()]);
        }
    }





    public static void sendRefreshBroadcast(Context context){
        Intent intent = new Intent(UPDATE_ALL);
        intent.setComponent(new ComponentName(context,TodayScheduleWidgetProvider.class));
        context.sendBroadcast(intent);
    }

    private static void sendRefreshSetting(Context context){
        Intent intent = new Intent(SETTING_CHANGE);
        intent.setComponent(new ComponentName(context,TodayScheduleWidgetProvider.class));
        context.sendBroadcast(intent);
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

    private static void setBackground(RemoteViews parentRemoteViews, Context context) {
        //方法笨，而且不好用，希望以后能修改
        try {
            String stringAlpha= SharePreferenceLab.getInstance().getWidgetTodayBgAlpha(context);
            int alpha=java.lang.Integer.parseInt(stringAlpha);
            String path=SharePreferenceLab.getInstance().getWidgetTodayBgPath(context);
//            Log.d(TAG, "setBackground: "+path);
            if(path.equals("")){
                if(SharePreferenceLab.getInstance().getWidgetTodayBgRefreshType(context).equals("white")){
                    parentRemoteViews.setImageViewResource(R.id.widget_today_bg_imageView,R.drawable.shape_widget_bg_round_corners_100);
                    parentRemoteViews.setInt(R.id.widget_today_bg_imageView, "setImageAlpha", alpha);
//                    parentRemoteViews.setInt(R.id.widget_today_bg_imageView, "setImageAlpha", 125);
                }else {
                    parentRemoteViews.setImageViewResource(R.id.widget_today_bg_imageView,R.drawable.shape_widget_bg_round_corners_100_black);
                    parentRemoteViews.setInt(R.id.widget_today_bg_imageView, "setImageAlpha", alpha);
//                    parentRemoteViews.setInt(R.id.widget_today_bg_imageView, "setImageAlpha", 250);
                }
            }else{
                Bitmap bitmap = MyBitmapTool.getRoundedCornerBitmap_from_PathFromString(context,path,alpha,widgetWidth,widgetHeight);
//        bitmap=MyBitmapTool.compressBitmap(bitmap,0);
                if(bitmap==null){
                    parentRemoteViews.setImageViewResource(R.id.widget_today_bg_imageView,R.drawable.shape_widget_bg_round_corners_100);
                    parentRemoteViews.setInt(R.id.widget_today_bg_imageView, "setImageAlpha", alpha);
//                    parentRemoteViews.setInt(R.id.widget_today_bg_imageView, "setImageAlpha", 250);
                }else{
                    parentRemoteViews.setImageViewBitmap(R.id.widget_today_bg_imageView, bitmap);
                    parentRemoteViews.setInt(R.id.widget_today_bg_imageView, "setImageAlpha", 255);
                }
            }




        }catch (Exception e){
            SharePreferenceLab.getInstance().setWidgetTodayBgPath(context,"");
        }


    }

}