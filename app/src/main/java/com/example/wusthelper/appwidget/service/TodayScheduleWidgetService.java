package com.example.wusthelper.appwidget.service;

import android.content.Intent;
import android.widget.RemoteViewsService;

import com.example.wusthelper.appwidget.factory.TodayScheduleWidgetFactory;
import com.example.wusthelper.dbhelper.WidgetCourseDB;

public class TodayScheduleWidgetService extends RemoteViewsService {
    private static final String TAG = "TodayScheduleWidgetServ";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new TodayScheduleWidgetFactory(getApplicationContext(), WidgetCourseDB.getTodayCourseListForWidget());
    }

}
