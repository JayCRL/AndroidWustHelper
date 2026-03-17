package com.example.wusthelper.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.wusthelper.MyApplication;
import com.example.wusthelper.base.activity.BaseMvpActivity;
import com.example.wusthelper.base.BaseMvpView;
import com.example.wusthelper.bean.javabean.CourseBean;
import com.example.wusthelper.bindeventbus.BindEventBus;
import com.example.wusthelper.bindeventbus.coursefunction.ClassTimeMessage;
import com.example.wusthelper.bindeventbus.coursefunction.WeekSelectMessage;
import com.example.wusthelper.databinding.ActivityEditBinding;
import com.example.wusthelper.dbhelper.CourseDB;
import com.example.wusthelper.helper.MyDialogHelper;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.mvp.presenter.EditPresenter;
import com.example.wusthelper.ui.dialog.ClassSelectDialogFragment;
import com.example.wusthelper.ui.dialog.CourseDeleteDialog;
import com.example.wusthelper.ui.dialog.ErrorLoginDialog;
import com.example.wusthelper.ui.dialog.WeekSelectDialogFragment;
import com.example.wusthelper.utils.ToastUtil;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cn.pedant.SweetAlert.SweetAlertDialog;

@BindEventBus
public class EditActivity extends BaseMvpActivity<BaseMvpView, EditPresenter, ActivityEditBinding>
        implements BaseMvpView, View.OnClickListener {

    private static final String TAG = "EditActivity";
    public static EditActivity instance;
    /*界面为添加界面*/
    private static final int TYPE_ADD = 0;
    /*界面为编辑课程界面*/
    private static final int TYPE_EDIT = 1;
    //界面的类型，有两种，分别是TYPE_ADD 和 TYPE_EDIT
    private static int mType;
    //用于显示的CourseBean
    private static CourseBean mCourseBean;
    //用于删除课程，默认为false表示删除当前查看课程
    public static Boolean deleteCourse = false;

    /*下面两个变量，分别是上课时间和下课时间*/
    private static int startTime;
    private static int endTime;
    /*课程所在的学期*/
    private static String mSemester;

    private static int mWeekday;
    private String[]   weekdays = {"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期天"};

    /*下面两个变量，分别是开始周和结束周*/
    private int startWeek;
    private int endWeek;
    /*选择的周数列表*/
    private List<Integer> resultWeekList = new ArrayList<>();
    private List<Integer> weekList = new ArrayList<>();

    /*用于添加上课界面的跳转*/
    public static Intent newInstance(Context context,int weekday,int classTime,String semester, int type) {
        mType       = type ;
        mWeekday    = weekday ;
        startTime   = classTime*2 -1 ;
        endTime     = classTime*2 ;
        mSemester    = semester;
        return new Intent(context, EditActivity.class);
    }
    /*用于编辑界面的跳转*/
    public static Intent newInstance(Context context, CourseBean courseBean, int type) {
        mType       = type ;
        mCourseBean = courseBean ;
        mWeekday    = courseBean.getWeekday();
        startTime   = courseBean.getStartTime()*2-1;
        endTime     = startTime+1 ;
        mSemester   = courseBean.getSemester();
        return new Intent(context, EditActivity.class);
    }

    @Override
    public EditPresenter createPresenter() {
        return new EditPresenter();
    }

    @Override
    public BaseMvpView createView() {
        return this;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void initView() {
        getWindow().setStatusBarColor(Color.WHITE);
        instance = this;
        setListener();
//        getBinding().tvWeekday.setText(weekdays[mWeekday - 1]);
        getBinding().tvClassTime.setText(weekdays[mWeekday - 1]+" "+startTime+"-"+endTime+"节");

        Log.d(TAG, "initView: "+mType);
        if(mType == TYPE_ADD){
            initAddView();
        }else{
            initEditView();
        }

        getPresenter().initPresenterData();

    }

    @SuppressLint("SetTextI18n")
    private void initEditView() {
        int i;
        for (i = mCourseBean.getStartWeek(); i <= mCourseBean.getEndWeek(); i++){
            weekList.add(i);
        }
        if(weekList.size()%2 != 0) {
            weekList.add(i-1);
        }
        resultWeekList.add(mCourseBean.getStartWeek());
        resultWeekList.add(mCourseBean.getEndWeek());
        getBinding().etClassName.setText(mCourseBean.getCourseName());
        getBinding().etClassNo.setText(mCourseBean.getClassNo());
        getBinding().etClassRoom.setText(mCourseBean.getClassRoom());
        getBinding().etTeacher.setText(mCourseBean.getTeacherName());
        if (mCourseBean.getStartWeek() == mCourseBean.getEndWeek()){
            getBinding().tvWeek.setText(mCourseBean.getStartWeek() +"周");
        }else {
            getBinding().tvWeek.setText(mCourseBean.getStartWeek() + "-" + mCourseBean.getEndWeek()+"周");
        }


//        if(mCourseBean.getIsDefault()==CourseBean.IS_MYSELF){
            getBinding().rlDelete.setVisibility(View.VISIBLE);
//        }
//        else {
//            getBinding().etClassName.setFocusable(false);
//            getBinding().etClassNo.setFocusable(false);
//            getBinding().etClassRoom.setFocusable(false);
//            getBinding().etTeacher.setFocusable(false);
//        }
    }

    @SuppressLint("SetTextI18n")
    private void initAddView() {
        getBinding().titleCourseEdit.ClassEditTextTitle.setText("添加课程");
        getBinding().tvClassTime.setText(startTime+"-"+endTime+"节");
    }

    /*设置监听器*/
    private void setListener() {
        getBinding().rlDelete.setOnClickListener(this);
        getBinding().titleCourseEdit.tvFinish.setOnClickListener(this);
        getBinding().titleCourseEdit.ivBack.setOnClickListener(this);
        getBinding().llEditWeek.setOnClickListener(this);
        getBinding().llEditClassTime.setOnClickListener(this);
    }



    @Override
    public void onClick(View v) {
        if(v.equals(getBinding().titleCourseEdit.tvFinish)){
            //执行添加或者修改逻辑
            editFinish();
        }else if(v.equals(getBinding().titleCourseEdit.ivBack)){
            finish();
        }else if(v.equals(getBinding().llEditWeek)){
            //如果编辑课程有0周，让设置周日为首日再对其进行修改
            if(mType == TYPE_EDIT && mCourseBean.getWeekday() == 7 && weekList.get(0) == 0){
                MyDialogHelper.getSweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE, null,
                        "修改此课程需要设置周日为首日才能修改，确认将周日设为首日再修改吗？", "确认", "取消", new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                SharePreferenceLab.setIsChooseSundayFirst(true);
                                finish();
                            }
                        }).show();
            }else {
                WeekSelectDialogFragment weekSelectDialogFragment = new WeekSelectDialogFragment();
                Bundle bundle = new Bundle();
                Log.e(TAG,"(ArrayList<Integer>) weekList = " + (ArrayList<Integer>) weekList);
                bundle.putIntegerArrayList(WeekSelectDialogFragment.SELECT_WEEK, (ArrayList<Integer>) checkRightWeek(weekList));
                weekSelectDialogFragment.setArguments(bundle);
                weekSelectDialogFragment.show(getSupportFragmentManager(), "");
            }

        }else if(v.equals(getBinding().llEditClassTime)){
                ClassSelectDialogFragment classSelectDialogFragment = new ClassSelectDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(ClassSelectDialogFragment.START_TIME, startTime);
                bundle.putInt(ClassSelectDialogFragment.END_TIME, endTime);
                classSelectDialogFragment.setArguments(bundle);
                classSelectDialogFragment.show(getSupportFragmentManager(), "");
        }else if(v.equals(getBinding().rlDelete)){
            if(mCourseBean.getIsDefault()==CourseBean.IS_DEFAULT){
                MyDialogHelper.getSweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE, null,
                    "该课程为教务处课程，确认删除吗？", "删除", "取消", new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            CourseDB.deleteOneCourse(mCourseBean.getId());
                            finish();
                        }
                    }).show();
            }else {
                CourseDeleteDialog courseDeleteDialog = new CourseDeleteDialog(mCourseBean);
                courseDeleteDialog.show(getSupportFragmentManager(),"delete_course");
            }
        }
    }

    private void editFinish() {

        String studentId = SharePreferenceLab.getInstance().getStudentId(MyApplication.getContext());
        String className = getBinding().etClassName.getText().toString();
        String teacher = getBinding().etTeacher.getText().toString();
        String classRoom = getBinding().etClassRoom.getText().toString();
        String classNo = getBinding().etClassNo.getText().toString();
        Log.e(TAG,"resultWeekList" + resultWeekList);

        if(mType == TYPE_EDIT){
            if(mCourseBean.getIsDefault() == CourseBean.IS_MYSELF){
                if(mCourseBean.getStudentId().equals(studentId) && mCourseBean.getCourseName().equals(className)
                        && mCourseBean.getTeacherName().equals(teacher) && mCourseBean.getClassRoom().equals(classRoom)
                        && mCourseBean.getClassNo().equals(classNo) && ((mCourseBean.getStartTime() * 2 - 1) == startTime)
                        && ((mCourseBean.getStartTime() * 2 == endTime) && (mCourseBean.getStartWeek() == resultWeekList.get(0))
                        && (mCourseBean.getEndWeek() == resultWeekList.get(resultWeekList.size()-1)) )){
                    ToastUtil.showShortToastCenter("没有对该课程信息进行任何修改");
                } else {
                    MyDialogHelper.getSweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE, null,
                            "您的此次编辑可能会修改之前添加过该课程信息的内容，确认继续吗？", "确认", "取消", new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    Random random = new Random();
                                    int color = random.nextInt(10);
                                    boolean TimesNotEmpty = startTime!=0&&endTime!=0;
                                    boolean WeeksNotEmpty = !resultWeekList.isEmpty();

                                    if (TimesNotEmpty && WeeksNotEmpty) {
                                        LitePal.deleteAll(CourseBean.class,"courseName = ? and isDefault = ?",mCourseBean.getCourseName(),"1");
                                        for (int i = 0; i < resultWeekList.size(); i += 2) {
                                            //这里time指具体在课程表的哪一号位置
                                            Log.d(TAG, "editFinish: "+studentId);
                                            Log.d(TAG, "editFinish: "+mSemester);
                                            Log.d(TAG, "editFinish: "+mWeekday);
                                            Log.d(TAG, "editFinish: "+startTime);
                                            //如果是周日为首日，自己添加星期天的课程，周数存入数据库时减一
                                            for (int j = startTime; j < endTime; j += 2){
                                                CourseBean courseBean = new CourseBean(1,0,
                                                        studentId,className,classNo,classRoom,teacher,
                                                        (SharePreferenceLab.getIsChooseSundayFirst()&&mWeekday==7) ? resultWeekList.get(i)-1 :resultWeekList.get(i),
                                                        (SharePreferenceLab.getIsChooseSundayFirst()&&mWeekday==7) ? resultWeekList.get(i+1)-1 :resultWeekList.get(i+1),
                                                        mWeekday,(j+1)/2,mSemester,color);

                                                Log.d(TAG, "editFinish: "+courseBean.toString());
                                                courseBean.save();
                                            }
                                        }

                                        finish();

                                    }
                                    else {
                                        ToastUtil.showShortToastCenter("请将信息填写完整");
                                    }
                                }
                            }).show();
                }
            } else if(mCourseBean.getIsDefault() == CourseBean.IS_DEFAULT){
                if(mCourseBean.getStudentId().equals(studentId) && mCourseBean.getCourseName().equals(className)
                        && mCourseBean.getTeacherName().equals(teacher) && mCourseBean.getClassRoom().equals(classRoom)
                        && mCourseBean.getClassNo().equals(classNo) && ((mCourseBean.getStartTime() * 2 - 1) == startTime)
                        && ((mCourseBean.getStartTime() * 2 == endTime) && (mCourseBean.getStartWeek() == resultWeekList.get(0))
                        && (mCourseBean.getEndWeek() == resultWeekList.get(resultWeekList.size()-1)) )){
                    ToastUtil.showShortToastCenter("没有对该课程信息进行任何修改");
                } else {
                    Random random = new Random();
                    int color = random.nextInt(10);
                    boolean TimesNotEmpty = startTime!=0&&endTime!=0;
                    boolean WeeksNotEmpty = !resultWeekList.isEmpty();

                    if (TimesNotEmpty && WeeksNotEmpty) {
                        MyDialogHelper.getSweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE, null,
                                "该课程为教务处课程，确认修改吗？", "确认", "取消", new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        for (int i = 0; i < resultWeekList.size(); i += 2) {
                                            LitePal.deleteAll(CourseBean.class,"courseName = ? and isDefault = ?",mCourseBean.getCourseName(),"0");
                                            //这里time指具体在课程表的哪一号位置
                                            Log.d(TAG, "editFinish: "+studentId);
                                            Log.d(TAG, "editFinish: "+mSemester);
                                            Log.d(TAG, "editFinish: "+mWeekday);
                                            Log.d(TAG, "editFinish: "+startTime);
                                            //如果是周日为首日，自己添加的课程，周数存入数据库时减一
                                            for (int j = startTime; j < endTime; j += 2){
                                                CourseBean courseBean = new CourseBean(1,0,
                                                        studentId,className,classNo,classRoom,teacher,
                                                        (SharePreferenceLab.getIsChooseSundayFirst()&&mWeekday==7) ? resultWeekList.get(i)-1 :resultWeekList.get(i),
                                                        (SharePreferenceLab.getIsChooseSundayFirst()&&mWeekday==7) ? resultWeekList.get(i+1)-1 :resultWeekList.get(i+1),
                                                        mWeekday,(j+1)/2,mSemester,color);

                                                Log.d(TAG, "editFinish: "+courseBean.toString());
                                                courseBean.save();
                                            }

                                        }

                                        finish();
                                    }
                                }).show();


                    }
                    else {
                        ToastUtil.showShortToastCenter("请将信息填写完整");
                    }
                }

            }
        }else {

            Random random = new Random();
            int color = random.nextInt(10);
            boolean TimesNotEmpty = startTime!=0&&endTime!=0;
            boolean WeeksNotEmpty = !resultWeekList.isEmpty();

            if (TimesNotEmpty && WeeksNotEmpty) {
                for (int i = 0; i < resultWeekList.size(); i += 2) {
                     //这里time指具体在课程表的哪一号位置
                    Log.d(TAG, "editFinish: "+studentId);
                    Log.d(TAG, "editFinish: "+mSemester);
                    Log.d(TAG, "editFinish: "+mWeekday);
                    Log.d(TAG, "editFinish: "+startTime);
                    //如果是周日为首日，自己添加的课程，周数存入数据库时减一
                    for (int j = startTime; j < endTime; j += 2){
                        CourseBean courseBean = new CourseBean(1,0,
                                studentId,className,classNo,classRoom,teacher,
                                (SharePreferenceLab.getIsChooseSundayFirst()&&mWeekday==7) ? resultWeekList.get(i)-1 :resultWeekList.get(i),
                                (SharePreferenceLab.getIsChooseSundayFirst()&&mWeekday==7) ? resultWeekList.get(i+1)-1 :resultWeekList.get(i+1),
                                mWeekday,(j+1)/2,mSemester,color);

                        Log.d(TAG, "editFinish: "+courseBean.toString());
                        courseBean.save();
                    }
                }

                finish();

            }
            else {
                ToastUtil.showShortToastCenter("请将信息填写完整");
            }
        }

    }


    private List<Integer> checkRightWeek(List<Integer> weekList) {
        //如果weekList个数不为偶数或者选择的选择的周数大于2
        if((weekList.size()%2 != 0) || (resultWeekList.size() > 2)) {
            weekList.clear();
            for (int i = 0; i < resultWeekList.size(); i+=2) {
                Log.e(TAG,"checkRightWeek = " + weekList);
                int k;
                for(k = resultWeekList.get(i); k <= resultWeekList.get(i+1); k++) {
                    Log.e(TAG,"checkRightWeek k= " + k);
                    weekList.add(k);
                }
                if(weekList.size()%2 != 0) {
                    weekList.add(k-1);
                }
            }
        }
        Log.e(TAG,"checkRightWeek = " + weekList);
        return weekList;
    }

    //    选择日期调用的函数
//    首先获取选择序列
//    之后添加到result数组里 从0开始 偶数为开始周次，奇数为结束周次
    //EventBus的使用，不可删除
    @Subscribe(threadMode =ThreadMode.MAIN)
    public void onReceiveWeekSelectMessage(WeekSelectMessage weekSelectMessage){

        weekList = weekSelectMessage.getData().getWeekList();
        Log.e(TAG,"weekList =" + weekList );
        for (int i : weekList) {
            Log.d(TAG, "" + i);
        }

        resultWeekList.clear();

        if (weekList.size() == 1) {
            int startIndex = 0;
            int endIndex = 0;
            resultWeekList.add(weekList.get(startIndex));
            resultWeekList.add(weekList.get(endIndex));
        } else {

            int startIndex = 0;
            int endIndex = 1;
            while (endIndex <= weekList.size()) {
                if (endIndex < weekList.size() && (weekList.get(endIndex) - weekList.get(endIndex-1)) <= 1) {
                    endIndex ++;
                } else {
                    resultWeekList.add(weekList.get(startIndex));
                    resultWeekList.add(weekList.get(endIndex - 1));
                    Log.d(TAG, "" + weekList.get(startIndex));
                    Log.d(TAG, "" + weekList.get(endIndex - 1));
                    startIndex = endIndex;
                    endIndex ++;
                }
            }
        }

        getBinding().tvWeek.setText("");
        getBinding().tvWeek.append("" + resultWeekList.get(0));
        getBinding().tvWeek.append("-");
        getBinding().tvWeek.append("" + resultWeekList.get(1));

        for (int i = 2; i < resultWeekList.size(); i+=2) {
            getBinding().tvWeek.append("，");
            getBinding().tvWeek.append("" + resultWeekList.get(i));
            getBinding().tvWeek.append("-");
            getBinding().tvWeek.append("" + resultWeekList.get(i+1));
        }
        Log.e(TAG,"resultWeekList = " + resultWeekList);
    }

    @Subscribe(threadMode =ThreadMode.MAIN)
    public void onReceiveClassTimeMessage(ClassTimeMessage classTimeMessage) {
        startTime = classTimeMessage.getData().getStartTime();
        endTime = classTimeMessage.getData().getEndTime();
        getBinding().tvClassTime.setText("");
        getBinding().tvClassTime.append("" + startTime);
        getBinding().tvClassTime.append("-");
        getBinding().tvClassTime.append("" + endTime);
        getBinding().tvClassTime.append("节");

    }

}
