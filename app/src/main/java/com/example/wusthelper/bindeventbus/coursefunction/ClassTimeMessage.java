package com.example.wusthelper.bindeventbus.coursefunction;

import com.example.wusthelper.bindeventbus.Event;

public class ClassTimeMessage extends Event<ClassTimeData> {
    public ClassTimeMessage(int code, ClassTimeData data) {
        super(code, data);
    }
}
