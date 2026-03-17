package com.example.wusthelper.widget;

import android.view.MotionEvent;
import android.view.View;

public interface OverScroll {
    /**
     *
     * @param startY
     * @param deltaY
     * @param contentView
     * @param motionEvent
     * @return 是否消耗事件
     */
    boolean overScrolling(float startY, float deltaY , View contentView, MotionEvent motionEvent);
    boolean overScrolledActionUp(float startY,View contentView, MotionEvent motionEvent);
}
