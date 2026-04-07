package com.example.wusthelper.bindeventbus.mainactivity;

import com.example.wusthelper.bindeventbus.Event;

public class jumpActivityMessage extends Event<jumpActivityData> {
    public jumpActivityMessage(int code, jumpActivityData data) {
        super(code, data);
    }
}
