package com.example.wusthelper.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.wusthelper.MyApplication;
import com.example.wusthelper.R;
import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.bean.javabean.CourseBean;
import com.example.wusthelper.bean.javabean.DateBean;
import com.example.wusthelper.databinding.ActivityChangeScheduleSettingBinding;
import com.example.wusthelper.databinding.ActivityImportCalendarBinding;
import com.example.wusthelper.helper.ConfigHelper;
import com.example.wusthelper.helper.MyDialogHelper;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.helper.TimeTools;
import com.example.wusthelper.utils.CalendarReminderUtils;
import com.xuexiang.xutil.display.Colors;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.LogRecord;

import cn.pedant.SweetAlert.SweetAlertDialog;



/**
 *将课程导入日程
 * 周日首日操作整合到了日历操作
 * */
public class ImportCalendarActivity extends BaseActivity<ActivityImportCalendarBinding> implements CompoundButton.OnCheckedChangeListener,View.OnClickListener {

    private static final String TAG = "ImportCalendarActivity";

    public static Intent newInstance(Context context) {
        return new Intent(context, ImportCalendarActivity.class);
    }

    public Handler mHandler;
    //用于显示的Dialog
    private SweetAlertDialog dialog;

    //黄家湖校区上课时间
//    private long[] HuangJiaHuStartTime = {500, 615, 840, 955, 1140, 1250};
//    private long[] HuangJiaHuEndTime = {600, 715, 940, 1055, 1240, 1350};

    private long[] HuangJiaHuStartTime = {500, 620, 840, 960, 1120, 1230};
    private long[] HuangJiaHuEndTime = {600, 720, 940, 1060, 1220, 1330};

    //青山校区上课时间
    private long[] QingShangStartTime = {480, 610, 840, 960, 1120, 1230};
    private long[] QingShangEndTime = {580, 710, 940, 1060, 1220, 1330};


    //一分钟的毫秒数
    private long minMill = 60 * 1000;
    //一天的毫秒数
    private long oneDay = 24 * 60 * 60 * 1000;
    //当前日期对应的ms
    long currentDate = TimeTools.getDate(SharePreferenceLab.getDate()).getTime();
    //当前日期对应的星期
    int currentWeekday = SharePreferenceLab.getWeekday();
    //当前选择的周次
    int currentWeek = SharePreferenceLab.getWeek();
    //数据库中的课程
    List<CourseBean> courseBeanList = LitePal.findAll(CourseBean.class);

    @Override
    public void initView() {
        getWindow().setStatusBarColor(Colors.WHITE);
        getBinding().changeHomepageSwitchStatus.setChecked(SharePreferenceLab.getHomepageSettings());
        getBinding().chooseSundaySwitchStatus.setChecked(SharePreferenceLab.getIsChooseSundayFirst());
        getBinding().changeHomepageSwitchStatus.setOnCheckedChangeListener(this);
        getBinding().chooseSundaySwitchStatus.setOnCheckedChangeListener(this);
        Log.e(TAG,"TimeTools.getDate(SharePreferenceLab.getDate()).getTime(); = " + TimeTools.getDate(SharePreferenceLab.getDate()).getTime());
        Log.e(TAG,"SharePreferenceLab.getDate() = " + SharePreferenceLab.getDate());
        Log.e(TAG, "SharePreferenceLab.getWeek() = " + SharePreferenceLab.getWeek());
        Log.e(TAG, "SharePreferenceLab.getWeekday() = " + SharePreferenceLab.getWeekday());
        getBinding().importCurrentWeek.setChecked(SharePreferenceLab.getIsWeekCourseCalendar());
        if (SharePreferenceLab.getIsWeekCourseCalendar()) {
            getBinding().textCurrentWeek.setText("删除");
            getBinding().textCurrentWeek.setBackgroundColor(getResources().getColor(R.color.color_delete_calendar));
        }
        getBinding().importAllWeek.setChecked(SharePreferenceLab.getIsAllCourseCalendar());
        if (SharePreferenceLab.getIsAllCourseCalendar()) {
            getBinding().textAllWeek.setText("删除");
            getBinding().textAllWeek.setBackgroundColor(getResources().getColor(R.color.color_delete_calendar));
        }
        Log.e(TAG, "setChecked(SharePreferenceLab.getIsWeekCourseCalendar()) = " + SharePreferenceLab.getIsWeekCourseCalendar());
        Log.e(TAG, "setChecked(SharePreferenceLab.getIsAllCourseCalendar())) = " + SharePreferenceLab.getIsAllCourseCalendar());
        //检测日历权限是否开启
        setOnclickListener();
        getBinding().ivIntroduce.setOnClickListener(v -> {

            View dialog_explain = LayoutInflater.from(ImportCalendarActivity.this)
                    .inflate(R.layout.dialog_calendar_import_explain, null);
            AlertDialog.Builder builder_explain = new AlertDialog.Builder(ImportCalendarActivity.this);
            builder_explain.setView(dialog_explain);
            builder_explain.setPositiveButton("我知道了", null);
            builder_explain.show();
        });

        getBinding().ivBack.setOnClickListener(v -> finish());
    }

    private void setOnclickListener() {
        getBinding().cardCurrentWeek.setOnClickListener(this);
        getBinding().cardAllWeek.setOnClickListener(this);
    }


    /**
     * 是否导入成功或删除成功
     */
    private void importDeleteOperation() {
        mHandler = new Handler();
        if (SharePreferenceLab.getIsWeekCourseCalendar()) {
            new Thread() {
                @Override
                public void run() {
                    deleteAllWeekToCalendar();
                    importCurrentWeekCalendar();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (SharePreferenceLab.getIsImported()) {
                                dialog.cancel();
                                Log.e(TAG, "周课表导入");
                                showCommonDialog(SweetAlertDialog.SUCCESS_TYPE, "导入成功 \n",
                                        null,
                                        "确定", null);
                                getBinding().textCurrentWeek.setText("删除");
                                getBinding().textAllWeek.setText("导入");
                                getBinding().textCurrentWeek.setBackgroundColor(getResources().getColor(R.color.color_delete_calendar));
                                getBinding().textAllWeek.setBackgroundColor(getResources().getColor(R.color.color_import_calendar));
                                getBinding().importCurrentWeek.setChecked(true);
                                getBinding().importAllWeek.setChecked(false);
                            } else {
                                dialog.cancel();
                                Log.e(TAG, "导入失败");
                                showCommonDialog(SweetAlertDialog.ERROR_TYPE, "本周课都上完咯 \n",
                                        null,
                                        "确定", null);
                                SharePreferenceLab.setIsWeekCourseCalendar(false);
                                getBinding().textCurrentWeek.setText("导入");
                                getBinding().textAllWeek.setText("导入");
                                getBinding().textCurrentWeek.setBackgroundColor(getResources().getColor(R.color.color_import_calendar));
                                getBinding().textAllWeek.setBackgroundColor(getResources().getColor(R.color.color_import_calendar));
                                getBinding().importCurrentWeek.setChecked(false);
                                getBinding().importAllWeek.setChecked(false);
                            }
                        }
                    });
                }
            }.start();
        } else if (SharePreferenceLab.getIsAllCourseCalendar()) {
            new Thread() {
                @Override
                public void run() {
                    deleteAllWeekToCalendar();
                    importAllWeekToCalendar();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (SharePreferenceLab.getIsImported()) {
                                dialog.cancel();
                                Log.e(TAG, "所有课表导入");
                                showCommonDialog(SweetAlertDialog.SUCCESS_TYPE, "导入成功 \n",
                                        null,
                                        "确定", null);
                                getBinding().textCurrentWeek.setText("导入");
                                getBinding().textCurrentWeek.setBackgroundColor(getResources().getColor(R.color.color_import_calendar));
                                getBinding().textAllWeek.setText("删除");
                                getBinding().textAllWeek.setBackgroundColor(getResources().getColor(R.color.color_delete_calendar));
                                getBinding().importCurrentWeek.setChecked(false);
                                getBinding().importAllWeek.setChecked(true);
                            } else {
                                dialog.cancel();
                                Log.e(TAG, "导入失败");
                                showCommonDialog(SweetAlertDialog.ERROR_TYPE, "无课程可导入 \n",
                                        null,
                                        "确定", null);
                                SharePreferenceLab.setIsAllCourseCalendar(false);
                                getBinding().textCurrentWeek.setText("导入");
                                getBinding().textAllWeek.setText("导入");
                                getBinding().textCurrentWeek.setBackgroundColor(getResources().getColor(R.color.color_import_calendar));
                                getBinding().textAllWeek.setBackgroundColor(getResources().getColor(R.color.color_import_calendar));
                                getBinding().importCurrentWeek.setChecked(false);
                                getBinding().importAllWeek.setChecked(false);
                            }
                        }
                    });
                }
            }.start();
        } else {
            new Thread() {
                @Override
                public void run() {
                    deleteAllWeekToCalendar();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            dialog.cancel();
                            Log.e(TAG, "课表删除");
                            showCommonDialog(SweetAlertDialog.SUCCESS_TYPE, "删除成功 \n",
                                    null,
                                    "确定", null);
                        }
                    });
                }
            }.start();
        }
    }


    /**
     * 导入所有课程，该计算方法是按周一为首日计算的
     */
    private void importAllWeekToCalendar() {
        //记录是否有课程导入,默认false
        boolean isCourseImport = false;
        //当前时间
        long currentTime = System.currentTimeMillis();
        //导入日历课程的开始时间
        long startCalendarTime = 0;
        //导入日历课程的结束时间
        long endCalendarTime = 0;
        Log.e(TAG, "currentData =" + currentDate + "currentWeekday = " + currentWeekday);
        Log.e(TAG, "course: " + courseBeanList);
        //根据校区适配不同的时间
        int campus = SharePreferenceLab.getInstance().getCampus(MyApplication.getContext());
        //依次遍历数据库存储的课程
        for (int i = 0; i < courseBeanList.size(); i++) {
            Log.e(TAG, "i = " + i);
            //再遍历该课程起始周和结束周,判断如果是周日的课程并且是来自教务处的则周数减一
            int startWeek = (courseBeanList.get(i).getWeekday() == 7&& courseBeanList.get(i).getIsDefault() == 0) ? courseBeanList.get(i).getStartWeek()-1 : courseBeanList.get(i).getStartWeek();
            int endWeek = (courseBeanList.get(i).getWeekday() == 7&& courseBeanList.get(i).getIsDefault() == 0) ? courseBeanList.get(i).getStartWeek()-1 : courseBeanList.get(i).getEndWeek();
            for (int j = startWeek; j <= endWeek; j++) {
                    Log.e(TAG, "j = " + j);
                    startCalendarTime = currentDate + ((j - currentWeek) * 7 + courseBeanList.get(i).getWeekday() - currentWeekday) * oneDay;
                    endCalendarTime = currentDate + ((j - currentWeek) * 7 + courseBeanList.get(i).getWeekday() - currentWeekday) * oneDay;
                    if (campus == SharePreferenceLab.HUANGJIAHU) {
                        switch (courseBeanList.get(i).getStartTime()) {
                            case 1:
                                startCalendarTime = startCalendarTime + HuangJiaHuStartTime[0] * minMill;
                                endCalendarTime = endCalendarTime + HuangJiaHuEndTime[0] * minMill;
                                break;
                            case 2:
                                startCalendarTime = startCalendarTime + HuangJiaHuStartTime[1] * minMill;
                                endCalendarTime = endCalendarTime + HuangJiaHuEndTime[1] * minMill;
                                break;
                            case 3:
                                startCalendarTime = startCalendarTime + HuangJiaHuStartTime[2] * minMill;
                                endCalendarTime = endCalendarTime + HuangJiaHuEndTime[2] * minMill;
                                break;
                            case 4:
                                startCalendarTime = startCalendarTime + HuangJiaHuStartTime[3] * minMill;
                                endCalendarTime = endCalendarTime + HuangJiaHuEndTime[3] * minMill;
                                break;
                            case 5:
                                startCalendarTime = startCalendarTime + HuangJiaHuStartTime[4] * minMill;
                                endCalendarTime = endCalendarTime + HuangJiaHuEndTime[4] * minMill;
                                break;
                            case 6:
                                startCalendarTime = startCalendarTime + HuangJiaHuStartTime[5] * minMill;
                                endCalendarTime = endCalendarTime + HuangJiaHuEndTime[5] * minMill;
                                break;
                            default:
                                break;
                        }
                    } else {
                        switch (courseBeanList.get(i).getStartTime()) {
                            case 1:
                                startCalendarTime = startCalendarTime + QingShangStartTime[0] * minMill;
                                endCalendarTime = endCalendarTime + QingShangEndTime[0] * minMill;
                                break;
                            case 2:
                                startCalendarTime = startCalendarTime + QingShangStartTime[1] * minMill;
                                endCalendarTime = endCalendarTime + QingShangEndTime[1] * minMill;
                                break;
                            case 3:
                                startCalendarTime = startCalendarTime + QingShangStartTime[2] * minMill;
                                endCalendarTime = endCalendarTime + QingShangEndTime[2] * minMill;
                                break;
                            case 4:
                                startCalendarTime = startCalendarTime + QingShangStartTime[3] * minMill;
                                endCalendarTime = endCalendarTime + QingShangEndTime[3] * minMill;
                                break;
                            case 5:
                                startCalendarTime = startCalendarTime + QingShangStartTime[4] * minMill;
                                endCalendarTime = endCalendarTime + QingShangEndTime[4] * minMill;
                                break;
                            case 6:
                                startCalendarTime = startCalendarTime + QingShangStartTime[5] * minMill;
                                endCalendarTime = endCalendarTime + QingShangEndTime[5] * minMill;
                                break;
                            default:
                                break;
                        }
                    }
                    //导入课程的起始时间大于当前时间就存入
                    if (startCalendarTime > currentTime) {
                        isCourseImport = true;
                        Log.e(TAG, "startCalendarTime > currentTime" + "yes");
                        CalendarReminderUtils.addCalendarEvent(MyApplication.getContext(), courseBeanList.get(i).getCourseName(),
                                courseBeanList.get(i).getClassRoom(), courseBeanList.get(i).getTeacherName() + '\n' + courseBeanList.get(i).getClassNo(), startCalendarTime, endCalendarTime);
                    }
                }
        }
        if (isCourseImport) {
            SharePreferenceLab.setIsImported(true);
        }
    }

    /**
     * 导入当前周课程
     */
    private void importCurrentWeekCalendar() {
        //记录是否有课程导入,默认false
        boolean isCourseImport = false;
//        String termStartDateStr = TimeTools.getDateFromTime(startTime);
//        //      计算当前周数
//        Date currentDate = new Date();
//        String currentStr = TimeTools.getDateFromTime(currentDate.getTime());
//        DateBean termStartDateBean = new DateBean(termStartDateStr, 0, weekday);
//        int gap = TimeTools.getRealWeek(termStartDateBean, currentStr);
        long currentTime = System.currentTimeMillis();
        long startCalendarTime = 0;
        long endCalendarTime = 0;
        int campus = SharePreferenceLab.getInstance().getCampus(MyApplication.getContext());
        for (int i = 0; i < courseBeanList.size(); i++) {
            Log.e(TAG, "i = " + i);
            int startWeek = (courseBeanList.get(i).getWeekday() == 7&& courseBeanList.get(i).getIsDefault() == 0) ? courseBeanList.get(i).getStartWeek()-1 : courseBeanList.get(i).getStartWeek();
            int endWeek = (courseBeanList.get(i).getWeekday() == 7&& courseBeanList.get(i).getIsDefault() == 0) ? courseBeanList.get(i).getStartWeek()-1 : courseBeanList.get(i).getEndWeek();
            if (currentWeek >= startWeek && currentWeek <= endWeek) {
                Log.e(TAG, "currentWeek = " + currentWeek);
                startCalendarTime = currentDate + (courseBeanList.get(i).getWeekday() - currentWeekday) * oneDay;
                endCalendarTime = currentDate + (courseBeanList.get(i).getWeekday() - currentWeekday) * oneDay;
                if (campus == SharePreferenceLab.HUANGJIAHU) {
                    switch (courseBeanList.get(i).getStartTime()) {
                        case 1:
                            startCalendarTime = startCalendarTime + HuangJiaHuStartTime[0] * minMill;
                            endCalendarTime = endCalendarTime + HuangJiaHuEndTime[0] * minMill;
                            break;
                        case 2:
                            startCalendarTime = startCalendarTime + HuangJiaHuStartTime[1] * minMill;
                            endCalendarTime = endCalendarTime + HuangJiaHuEndTime[1] * minMill;
                            break;
                        case 3:
                            startCalendarTime = startCalendarTime + HuangJiaHuStartTime[2] * minMill;
                            endCalendarTime = endCalendarTime + HuangJiaHuEndTime[2] * minMill;
                            break;
                        case 4:
                            startCalendarTime = startCalendarTime + HuangJiaHuStartTime[3] * minMill;
                            endCalendarTime = endCalendarTime + HuangJiaHuEndTime[3] * minMill;
                            break;
                        case 5:
                            startCalendarTime = startCalendarTime + HuangJiaHuStartTime[4] * minMill;
                            endCalendarTime = endCalendarTime + HuangJiaHuEndTime[4] * minMill;
                            break;
                        case 6:
                            startCalendarTime = startCalendarTime + HuangJiaHuStartTime[5] * minMill;
                            endCalendarTime = endCalendarTime + HuangJiaHuEndTime[5] * minMill;
                            break;
                        default:
                            break;
                    }
                } else {
                    switch (courseBeanList.get(i).getStartTime()) {
                        case 1:
                            startCalendarTime = startCalendarTime + QingShangStartTime[0] * minMill;
                            endCalendarTime = endCalendarTime + QingShangEndTime[0] * minMill;
                            break;
                        case 2:
                            startCalendarTime = startCalendarTime + QingShangStartTime[1] * minMill;
                            endCalendarTime = endCalendarTime + QingShangEndTime[1] * minMill;
                            break;
                        case 3:
                            startCalendarTime = startCalendarTime + QingShangStartTime[2] * minMill;
                            endCalendarTime = endCalendarTime + QingShangEndTime[2] * minMill;
                            break;
                        case 4:
                            startCalendarTime = startCalendarTime + QingShangStartTime[3] * minMill;
                            endCalendarTime = endCalendarTime + QingShangEndTime[3] * minMill;
                            break;
                        case 5:
                            startCalendarTime = startCalendarTime + QingShangStartTime[4] * minMill;
                            endCalendarTime = endCalendarTime + QingShangEndTime[4] * minMill;
                            break;
                        case 6:
                            startCalendarTime = startCalendarTime + QingShangStartTime[5] * minMill;
                            endCalendarTime = endCalendarTime + QingShangEndTime[5] * minMill;
                            break;
                        default:
                            break;
                    }
                }
                if (startCalendarTime > currentTime) {
                    isCourseImport = true;
                    Log.e(TAG, "startCalendarTime > currentTime" + "yes");
                    CalendarReminderUtils.addCalendarEvent(MyApplication.getContext(), courseBeanList.get(i).getCourseName(),
                            courseBeanList.get(i).getClassRoom(), courseBeanList.get(i).getTeacherName() + '\n' + courseBeanList.get(i).getClassNo(), startCalendarTime, endCalendarTime);
                }
            }
        }
        if (isCourseImport) {
            SharePreferenceLab.setIsImported(true);
        }
    }

    /**
     * 删除日历事件
     */
    private void deleteAllWeekToCalendar() {
        for(CourseBean courseBean : courseBeanList) {
            CalendarReminderUtils.deleteCalendarEvent(MyApplication.getContext(),courseBean.getCourseName());
        }
        SharePreferenceLab.setIsImported(false);
    }



    /**
     * 导入当前周课程对话框
     */
    private void showCurrentDialog(String title, String confirmText) {
        dialog = MyDialogHelper.getCommonDialog(this, SweetAlertDialog.BUTTON_CONFIRM,
                title, confirmText, sweetAlertDialog -> {
                    dialog.changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setCancelable(false);
                    SharePreferenceLab.setIsWeekCourseCalendar(true);
                    SharePreferenceLab.setIsAllCourseCalendar(false);
                    importDeleteOperation();
                });
        dialog.show();
    }

    /**
     * 删除当前周课程对话框
     * @param title
     * @param confirmText
     */
    private void showDeleteCurrentDialog(String title, String confirmText) {
        dialog = MyDialogHelper.getCommonDialog(this, SweetAlertDialog.BUTTON_CONFIRM,
                title, confirmText, sweetAlertDialog -> {
                    dialog.changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setCancelable(false);
                    SharePreferenceLab.setIsWeekCourseCalendar(false);
                    importDeleteOperation();
                    getBinding().textCurrentWeek.setText("导入");
                    getBinding().textCurrentWeek.setBackgroundColor(getResources().getColor(R.color.color_import_calendar));
                    getBinding().importCurrentWeek.setChecked(false);
                });
        dialog.show();
    }

    /**
     * 导入所有课程对话框
     * @param title
     * @param confirmText
     */
    private void showAllDialog(String title, String confirmText) {
        dialog = MyDialogHelper.getCommonDialog(this, SweetAlertDialog.BUTTON_CONFIRM,
                title, confirmText, sweetAlertDialog -> {
                    dialog.changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setCancelable(false);
                    SharePreferenceLab.setIsWeekCourseCalendar(false);
                    SharePreferenceLab.setIsAllCourseCalendar(true);
                    importDeleteOperation();
                });
        dialog.show();
    }

    /**
     * 删除所有课程对话框
     * @param title
     * @param confirmText
     */
    private void showDeleteAllDialog(String title, String confirmText) {
        dialog = MyDialogHelper.getCommonDialog(this, SweetAlertDialog.BUTTON_CONFIRM,
                title, confirmText, sweetAlertDialog -> {
                    dialog.changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setCancelable(false);
                    SharePreferenceLab.setIsAllCourseCalendar(false);
                    importDeleteOperation();
                    getBinding().textAllWeek.setText("导入");
                    getBinding().textAllWeek.setBackgroundColor(getResources().getColor(R.color.color_import_calendar));
                    getBinding().importAllWeek.setChecked(false);
                });
        dialog.show();
    }


    /**
     * 展示一个Dialog
     */
    public void showCommonDialog(int DialogType, String title, String contentText, String confirmText,
                                 SweetAlertDialog.OnSweetClickListener listener) {
        dialog = MyDialogHelper.getCommonDialog(this, DialogType,
                title, contentText, confirmText);
        dialog.show();
    }

    /**
     * 展示一个权限提醒弹窗
     */
    private void showPermissionDialog() {
        dialog = MyDialogHelper.getCommonDialog(this, SweetAlertDialog.NORMAL_TYPE,
                "权限提醒",
                "您已选择永久拒绝”武科大助手“使用日历权限，您可以去APP设置中添加此权限。",
                "设置",
                "取消",
                sweetAlertDialog -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                    dialog.cancel();
                });
        dialog.show();
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    //应用权限的回调
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
//            setOnclickListener();
        } else {
            showPermissionDialog();
        }
    }

    @Override
    public void onClick(View v) {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR}, 1);
        } else {
            if (v.equals(getBinding().cardCurrentWeek)) {
                if (!SharePreferenceLab.getIsWeekCourseCalendar()) {
                    showCurrentDialog("将当前周课程导入日程", "确定导入");
                } else {
                    showDeleteCurrentDialog("从日程中删除当前周课程", "确认删除");
                }
            } else if (v.equals(getBinding().cardAllWeek)) {
                if (!SharePreferenceLab.getIsAllCourseCalendar()) {
                    showAllDialog("将所有周课程导入日程", "确定导入");
                } else {
                    showDeleteAllDialog("从日程中删除所有周课程", "确认删除");
                }
            }
        }

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(buttonView.equals(getBinding().changeHomepageSwitchStatus)){
            SharePreferenceLab.setHomepageSettings(isChecked);
        }else if(buttonView.equals(getBinding().chooseSundaySwitchStatus)){
            SharePreferenceLab.setIsChooseSundayFirst(isChecked);
        }
    }

}
