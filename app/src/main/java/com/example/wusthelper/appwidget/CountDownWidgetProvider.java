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
import android.widget.RemoteViews;

import com.example.wusthelper.MyApplication;
import com.example.wusthelper.appwidget.factory.CountDownWidgetFactory;
import com.example.wusthelper.appwidget.service.*;
import com.example.wusthelper.R;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.ui.activity.CountdownActivity;
import com.example.wusthelper.ui.activity.WidgetSettingsActivity;
import com.example.wusthelper.utils.MyBitmapTool;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

/**
 * Implementation of App Widget functionality.
 */
public class CountDownWidgetProvider extends AppWidgetProvider {

    public static final String REFRESH="android.appwidget.action.REFRESH";
    public static final String UPDATEALL="android.appwidget.action.UPDATE_ALL";
    private static final String SETTINGS_ACTION = "android.appwidget.action.SETTINGS";
    private static final String TAG = "CountDownWidgetProvider";

    //小组件当前的宽高
    private static int widgetWidth = 308;

    private static int widgetHeight = 184;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_count_down_layout);

        widgetWidth = SharePreferenceLab.getWidgetCountdownWidth();
        widgetHeight = SharePreferenceLab.getWidgetCountdownHeight();
//        widgetWidth = getWidgetWidth(context,appWidgetManager,appWidgetId);
//        widgetHeight = getWidgetHeight(context,appWidgetManager,appWidgetId);

        initView(views,context);
        initData(appWidgetManager,views,context);

        setBackground(views,context);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }



    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        //sendRefreshData(context);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        //  Intent intent =new Intent(context,CountDownWidgetUpdateService.class);
        // context.stopService(intent);
        // Log.e(TAG, "onDisabled: UpDateService is stop" );
    }

    public static void sendRefreshBroadcast(Context context){
        Intent intent = new Intent(UPDATEALL);
        intent.setComponent(new ComponentName(context,CountDownWidgetProvider.class));
        context.sendBroadcast(intent);
    }

    private static void sendRefreshData(Context context){
        Intent intent = new Intent(REFRESH);
        intent.setComponent(new ComponentName(context,TodayScheduleWidgetProvider.class));
        context.sendBroadcast(intent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        //   int i= AppWidgetManager.getInstance(context).getAppWidgetOptions(appWidgetManager.getAppWidgetIds(new ComponentName(context,CountDownWidgetProvider.class)));
        String action = intent.getAction();

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_count_down_layout);
        initView(views,context);
        initData(appWidgetManager,views,context);

        widgetWidth = SharePreferenceLab.getWidgetCountdownWidth();
        widgetHeight = SharePreferenceLab.getWidgetCountdownHeight();

        setBackground(views,context);

        appWidgetManager.updateAppWidget(new ComponentName(context,CountDownWidgetProvider.class),views);

//
//        if(action.equals(REFRESH)){
//
//            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_count_down_layout);
//
//            appWidgetManager.updateAppWidget(new ComponentName(context,CountDownWidgetProvider.class),remoteViews);
//            //Toast.makeText(context,"我刷新了",Toast.LENGTH_SHORT).show();
//        }else if(action.equals(UPDATEALL)){
////            Toast.makeText(context,"更新",Toast.LENGTH_SHORT).show();
//            Log.d(TAG, "onReceive: UPDATEALL");
//            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_count_down_layout);
//          //  CountDownWidgetFactory.ReFresh();
//            setTextColor(remoteViews,context);
//            //setBackground(remoteViews,context);
//            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetManager.getAppWidgetIds(new ComponentName(context,CountDownWidgetProvider.class)),
//                    R.id.lv_countdown_widget);
//            appWidgetManager.updateAppWidget(new ComponentName(context,CountDownWidgetProvider.class),remoteViews);
//        }
    }




    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);

        widgetWidth = getWidgetWidth(context,appWidgetManager,appWidgetId);
        widgetHeight = getWidgetHeight(context,appWidgetManager,appWidgetId);

        SharePreferenceLab.setWidgetCountdownWidth(widgetWidth);
        SharePreferenceLab.setWidgetCountdownHeight(widgetHeight);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_count_down_layout);
        setBackground(remoteViews,context);

        appWidgetManager.updateAppWidget(new ComponentName(context,CountDownWidgetProvider.class),remoteViews);

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


    private static void initData(AppWidgetManager appWidgetManager, RemoteViews views, Context context) {
        CountDownWidgetFactory.ReFresh();
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetManager.getAppWidgetIds(new ComponentName(context,CountDownWidgetProvider.class)),
                R.id.lv_countdown_widget);
    }

    private static void initView(RemoteViews views, Context context) {
        Intent intent = CountdownActivity.newInstance(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("kind",CountdownActivity.WIDGET);
        PendingIntent btn_countdown_add = PendingIntent.getActivity(context,0,intent,0);
        views.setOnClickPendingIntent(R.id.btn_countdown_add_widget,btn_countdown_add);


        Intent CountDownSettingsIntent = new Intent(context, WidgetSettingsActivity.class);

        CountDownSettingsIntent.putExtra("widgetType",0);
        CountDownSettingsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent  CountDownSettings = PendingIntent.getActivity(context,0,CountDownSettingsIntent,PendingIntent.FLAG_CANCEL_CURRENT);
        views.setOnClickPendingIntent(R.id.btn_countdown_settings,CountDownSettings);



        Intent serviceintent= new Intent(context,CountDownWidgetService.class);
        views.setRemoteAdapter(R.id.lv_countdown_widget,serviceintent);
        Intent intentrefresh = new Intent();
        intentrefresh.setAction(REFRESH);
        intentrefresh.setComponent(new ComponentName(context,CountDownWidgetProvider.class));
        PendingIntent intent1 = PendingIntent.getBroadcast(context,0,intentrefresh,0);
        views.setOnClickPendingIntent(R.id.btn_countdown_refresh_widget,intent1);
        Intent detailintent = CountdownActivity.newInstance(context);
        PendingIntent detailPending = PendingIntent.getActivity(context,0,detailintent,0);
        //小组件背景设置：
//        Intent SettingsIntent = new Intent(context, Widget_schedule_countdown_bg_Settings.class);
//        SettingsIntent.setAction(SETTINGS_ACTION);
//        PendingIntent SettingsPendingIntent = PendingIntent.getActivity(context, 0, SettingsIntent, 0);
//        views.setOnClickPendingIntent(R.id.btn_settings,SettingsPendingIntent);
        views.setPendingIntentTemplate(R.id.lv_countdown_widget,detailPending);
        views.setEmptyView(R.id.lv_countdown_widget,R.id.empty_view_countdown_widget);
        // Instruct the widget manager to update the widget
        setTextColor(views,context);
    }





    private static void setBackground(RemoteViews parentRemoteViews, Context context) {
        //方法笨，而且不好用，希望以后能修改
        try{
            String stringAlpha= SharePreferenceLab.getInstance().getWidgetCountdownBgAlpha(context);
            int alpha=java.lang.Integer.parseInt(stringAlpha);
            String path=SharePreferenceLab.getInstance().getWidgetCountdownBgPath(context);
            if(path.equals("")){
                if(SharePreferenceLab.getInstance().getWidgetCountdownBgRefreshType(context).equals("white")){
                    parentRemoteViews.setImageViewResource(R.id.widget_countdown_bg_imageView,R.drawable.shape_widget_bg_round_corners_100);
                    parentRemoteViews.setInt(R.id.widget_countdown_bg_imageView, "setImageAlpha", alpha);
                }else {
                    parentRemoteViews.setImageViewResource(R.id.widget_countdown_bg_imageView,R.drawable.shape_widget_bg_round_corners_100_black);
                    parentRemoteViews.setInt(R.id.widget_countdown_bg_imageView, "setImageAlpha", alpha);
                }
            }else{

                Bitmap bitmap = MyBitmapTool.getRoundedCornerBitmap_from_PathFromString(context,path,alpha,widgetWidth,widgetHeight);
                if(bitmap==null){
                    parentRemoteViews.setImageViewResource(R.id.widget_countdown_bg_imageView,R.drawable.shape_widget_bg_round_corners_100);
                    parentRemoteViews.setInt(R.id.widget_countdown_bg_imageView, "setImageAlpha", alpha);
                }else{
                    parentRemoteViews.setImageViewBitmap(R.id.widget_countdown_bg_imageView,bitmap);
                }
            }

        }catch (Exception e){
            SharePreferenceLab.getInstance().setWidgetCountdownBgPath(MyApplication.getContext(),"");
        }

    }


    private static void setTextColor(RemoteViews views, Context context) {
        String textColor= SharePreferenceLab.getInstance().getWidgetCountdownTextColor(context);
        if(!textColor.equals("")){
            int Color=java.lang.Integer.parseInt(textColor);
            views.setTextColor(R.id.widget_title_text,Color);
        }else{
            views.setTextColor(R.id.widget_title_text, Color.BLACK);
        }
    }
}