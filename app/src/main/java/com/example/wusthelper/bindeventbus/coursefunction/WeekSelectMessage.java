package com.example.wusthelper.bindeventbus.coursefunction;

import com.example.wusthelper.bindeventbus.Event;

import java.util.List;

public class WeekSelectMessage extends Event<WeekSelectData> {

    public WeekSelectMessage(int code, WeekSelectData data) {
        super(code, data);
    }
}
