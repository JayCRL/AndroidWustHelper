package com.example.wusthelper.appwidget.service;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.example.wusthelper.appwidget.TodayScheduleWidgetProvider;

import java.util.Timer;
import java.util.TimerTask;

import kotlin.coroutines.Continuation;

public class UpdateWidgetService extends Service {

    private int continueTime;
    private Timer timer;
    private TimerTask task;
    private AppWidgetManager widgetmanager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        continueTime = intent.getIntExtra("continueTime",0);
        if(continueTime>180){
            timer = new Timer();
            task = new TimerTask() {
                @Override
                public void run() {
                    // 更新widget的界面
                    Intent intent = new Intent("android.appwidget.action.REFRESH");
                    sendBroadcast(intent);
//                ComponentName name = new ComponentName("cn.itcast.mobilesafe",
//                        "cn.itcast.mobilesafe.receiver.ProcessWidget");// 获取前面参数包下的后参数的Widget
//                RemoteViews views = new RemoteViews("cn.itcast.mobilesafe",
//                        R.layout.process_widget);// 获取Widget的布局
//                views.setTextViewText(R.id.process_count, "XXXX");//给process_count设置文本
//                views.setTextColor(R.id.process_count, Color.RED);//给process_count设置文本颜色
//                views.setTextViewText(R.id.process_memory, "XXXX");
//                views.setTextColor(R.id.process_memory, Color.RED);
//                Intent intent = new Intent(UpdateWidgetService.this, XXXX.class);
//                PendingIntent pendingIntent = PendingIntent.getBroadcast(
//                        getApplicationContext(), 0, intent, 0);
//                views.setOnClickPendingIntent(R.id.btn_clear, pendingIntent);// 给布局文件中的btn_clear设置点击事件
//                widgetmanager.updateAppWidget(name, views);//更新Widget
                }
            };
            timer.schedule(task,continueTime);
//            timer.scheduleAtFixedRate(task, 0, 2000);//延迟一秒      更新频率2秒
        }


        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        timer = new Timer();
        widgetmanager = AppWidgetManager.getInstance(getApplicationContext());
        task = new TimerTask() {
            @Override
            public void run() {
                // 更新widget的界面
//                ComponentName name = new ComponentName("cn.itcast.mobilesafe",
//                        "cn.itcast.mobilesafe.receiver.ProcessWidget");// 获取前面参数包下的后参数的Widget
//                RemoteViews views = new RemoteViews("cn.itcast.mobilesafe",
//                        R.layout.process_widget);// 获取Widget的布局
//                views.setTextViewText(R.id.process_count, "XXXX");//给process_count设置文本
//                views.setTextColor(R.id.process_count, Color.RED);//给process_count设置文本颜色
//                views.setTextViewText(R.id.process_memory, "XXXX");
//                views.setTextColor(R.id.process_memory, Color.RED);
//                Intent intent = new Intent(UpdateWidgetService.this, XXXX.class);
//                PendingIntent pendingIntent = PendingIntent.getBroadcast(
//                        getApplicationContext(), 0, intent, 0);
//                views.setOnClickPendingIntent(R.id.btn_clear, pendingIntent);// 给布局文件中的btn_clear设置点击事件
//                widgetmanager.updateAppWidget(name, views);//更新Widget
            }
        };
        timer.scheduleAtFixedRate(task, 1000, 2000);//延迟一秒      更新频率2秒
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        timer.cancel();
        timer = null;
        task = null;
        super.onDestroy();
    }
}
