package com.example.wusthelper.mvp.view;

import android.view.View;

import com.example.wusthelper.base.BaseMvpView;
import com.example.wusthelper.bean.itembean.CourseListForShow;
import com.example.wusthelper.bean.itembean.DateItemForShow;
import com.example.wusthelper.bean.itembean.WeekItemForShow;

import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public interface CourseFragmentView extends BaseMvpView {

    /**
     * @param list
     * 显示课程表
     * */
    void showCourseList(List<CourseListForShow> list);
    /**
     * @param list
     * 显示周数
     * */
    void showWeekList(List<WeekItemForShow> list);
    /**
     * @param list
     * 显示日期
     * */
    void showDataList(List<DateItemForShow> list);
    /**
     * @param monthText
     * 显示月份
     * */
    void setMonthText(String monthText);
    /**
     * @param weeksText
     * 显示周数
     * */
    void setWeeksText(String weeksText);

    /**
     * 显示非本周标识符
     * */
    void showNotThisWeek();

    void removeNotThisWeek();
    /**
     * @param DialogType
     * @param title
     * @param contentText
     * @param confirmText
     * @param listener
     * 显示确认弹窗
     * */
    void showCommonDialog(int DialogType,String title, String contentText, String confirmText,
                         SweetAlertDialog.OnSweetClickListener listener);
    /**
     * 关闭确认弹窗
     * */
    void cancelDialog();

    /**
     * @param realSemester
     * @param masterSemester
     * @param options
     *  显示学期选择器  */
    void showSemesterOptionPicker(String realSemester, String masterSemester, List<String> options);

    /**
     * @param campus
     * @param options
     * 显示校区选择
     * */
    void showCampusOptionPicker(int campus,List<String> options);
    /**
     * @param view
     * 显示上课时间提示
     * */
    void setCampusTime(View view);

    /**
     * 设置悬浮按钮的可见状态
     * */
    void showFloatingActionButton(int visibility);

    /**
     * 显示 提示用户有新的学期
     * */
    void showHaveNewSemester();

    /**
     * 关闭 提示用户有新的学期
     * */
    void cancelHaveNewSemester();
}
