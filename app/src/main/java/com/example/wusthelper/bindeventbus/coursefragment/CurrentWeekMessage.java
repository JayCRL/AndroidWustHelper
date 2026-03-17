package com.example.wusthelper.bindeventbus.coursefragment;

import com.example.wusthelper.bindeventbus.Event;
import com.example.wusthelper.bindeventbus.coursefunction.WeekSelectData;

public class CurrentWeekMessage extends Event<CurrentWeekData> {
    public CurrentWeekMessage(int code, CurrentWeekData data) {
        super(code, data);
    }
}
