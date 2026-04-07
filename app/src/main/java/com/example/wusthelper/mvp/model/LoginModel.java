package com.example.wusthelper.mvp.model;

import android.util.Log;

import com.example.wusthelper.MyApplication;
import com.example.wusthelper.bean.javabean.data.ConfigData;
import com.example.wusthelper.bean.javabean.CourseBean;
import com.example.wusthelper.bean.javabean.DateBean;
import com.example.wusthelper.bean.javabean.data.GraduateData;
import com.example.wusthelper.bean.javabean.data.StudentData;
import com.example.wusthelper.bean.javabean.data.TokenData;
import com.example.wusthelper.dbhelper.CourseDB;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.helper.TimeTools;
import com.example.wusthelper.request.NewApiHelper;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;
import com.example.wusthelper.helper.ConfigHelper;

import java.util.Date;
import java.util.List;

public class LoginModel {
    private static final String TAG = "LoginModel";

    public void login(String studentId, String password, DisposeDataListener listener){
        NewApiHelper.loginUndergraduateV2(studentId,password,listener);
    }

    //研究生登录
    public void loginGraduate(String studentId, String password, DisposeDataListener listener){
        NewApiHelper.loginGraduate(studentId,password,listener);
    }

    public void getUserInfo(DisposeDataListener listener){
        NewApiHelper.getUserInfo(listener);
    }

    public void getGraduateInfo(DisposeDataListener listener){
        NewApiHelper.getGraduateInfo(listener);
    }

    public void getConfig(DisposeDataListener listener) {
        NewApiHelper.getConfig(listener);
    }

    public void getTermStartDate(DisposeDataListener listener) {
        NewApiHelper.getTermStartDate(listener);
    }

    public void getCourse(String semester, DisposeDataListener listener) {
        NewApiHelper.getCourse(semester,listener);
    }

    public void getGraduateCourse(DisposeDataListener listener) {
        NewApiHelper.getGraduateCourse(listener);
    }

    public void saveAllCourseToDB(List<CourseBean> data, String semester) {
        String studentId = SharePreferenceLab.getInstance().getStudentId(MyApplication.getContext());
        Log.d(TAG, "saveAllCourse: "+studentId);
        //添加之前把原本的数据删除
        CourseDB.deleteCourse(studentId,semester,CourseBean.IS_DEFAULT);
        CourseDB.addAllCourseData(data,studentId,semester,CourseBean.TYPE_COMMON);
    }

    public void saveLoginData(TokenData tokenData, String studentId,String password ,String semester) {
        NewApiHelper.setToken(tokenData.getData());
        NewApiHelper.setMessage(tokenData.getMsg());
        SharePreferenceLab.setToken(tokenData.getData());
        SharePreferenceLab.getInstance().setMessage(MyApplication.getContext(), tokenData.getMsg());

        // 统一写入静态 SPTool（课程表/主页等模块读取的是静态 getter）
        SharePreferenceLab.setIsLogin(true);
        SharePreferenceLab.setStudentId(studentId);
        SharePreferenceLab.setPassword(password);
        SharePreferenceLab.setSelectSemester(semester);
        SharePreferenceLab.setSemester(semester);

        String anchorDate = SharePreferenceLab.getDate();
        int anchorWeek = SharePreferenceLab.getWeek();
        int anchorWeekday = SharePreferenceLab.getWeekday();

        String startDateStr = ConfigHelper.getTermStartDate();
        if (startDateStr != null && !startDateStr.trim().isEmpty()) {
            String termStartDateStr = null;
            try {
                long startTime = Long.parseLong(startDateStr.trim());
                termStartDateStr = TimeTools.getDateFromTime(startTime);
            } catch (NumberFormatException e) {
                Date termStartDate = TimeTools.getDate(startDateStr.trim());
                if (termStartDate != null) {
                    termStartDateStr = TimeTools.getDateFromTime(termStartDate.getTime());
                }
            }

            if (termStartDateStr != null) {
                Date termStartDate = TimeTools.getDate(termStartDateStr);
                int termStartWeekday = TimeTools.getWeekday(termStartDate);
                anchorDate = termStartDateStr;
                anchorWeek = 1;
                anchorWeekday = termStartWeekday;
            }
        }

        SharePreferenceLab.setDate(anchorDate);
        SharePreferenceLab.setWeek(anchorWeek);
        SharePreferenceLab.setWeekday(anchorWeekday);

        // 旧 sharedPreferences("Course") 仍保留写入，兼容历史读取路径
        SharePreferenceLab.getInstance().setData(MyApplication.getContext(), true,
                studentId, anchorDate, anchorWeek, anchorWeekday,
                password, semester, true);

    }

    public void saveStudentInfo(StudentData studentData) {
        SharePreferenceLab.getInstance().setCollege(MyApplication.getContext(),studentData.data.getCollege());
        SharePreferenceLab.getInstance().setMajor(MyApplication.getContext(), studentData.data.getMajor());

        String realName = studentData.data.getStuName();
        SharePreferenceLab.setRealName(realName);

        // 昵称策略：仅首次自动填充（昵称为空/默认值时才初始化为真实姓名）
        String currentNick = SharePreferenceLab.getUserName();
        if (currentNick == null || currentNick.trim().isEmpty() || "木有设置".equals(currentNick.trim())) {
            SharePreferenceLab.setUserName(realName);
        }
    }

    public void saveGraduateInfo(GraduateData graduateData) {
        SharePreferenceLab.getInstance().setCollege(MyApplication.getContext(),graduateData.data.getAcademy());
        SharePreferenceLab.getInstance().setMajor(MyApplication.getContext(), graduateData.data.getSpecialty());

        String realName = graduateData.data.getName();
        SharePreferenceLab.setRealName(realName);

        String currentNick = SharePreferenceLab.getUserName();
        if (currentNick == null || currentNick.trim().isEmpty() || "木有设置".equals(currentNick.trim())) {
            SharePreferenceLab.setUserName(realName);
        }

        SharePreferenceLab.setGrade(graduateData.data.getGrade());
        SharePreferenceLab.setDegree(graduateData.data.getDegree());
        SharePreferenceLab.setTutorName(graduateData.data.getTutorName());
    }


    public void saveConfig(ConfigData configData) {
        //接下来储存学期信息
        SharePreferenceLab.setSemester(configData.getData().getCurrentTerm());
        //设置配置信息
        ConfigHelper.setConfigBean(configData);
    }

    private int getCurrentWeek ( long startTermTime){
        String termStartDateStr = TimeTools.getDateFromTime(startTermTime);

        Log.e(TAG, "InitCurrentWeek: str : " + termStartDateStr);
        Date termStartDate = TimeTools.getDate(termStartDateStr);
        Log.d(TAG, "getCurrentWeek: termStartDate = "+termStartDate);
        int weekday = TimeTools.getWeekday(termStartDate);
        Log.e(TAG, "getCurrentWeek: weekday "+weekday );

        Date currentDate = new Date();
        String currentStr = TimeTools.getDateFromTime(currentDate.getTime());
        DateBean dateBean = new DateBean(termStartDateStr, 0, weekday);
        Log.d(TAG, "getCurrentWeek: dateBean="+dateBean);
        Log.d(TAG, "getCurrentWeek: currentStr="+currentStr);
        int gap = TimeTools.getRealWeek(dateBean, currentStr);
        Log.e(TAG, "getCurrentWeek: gap =" + gap );
        return gap;
//        int week = 1 + gap;
//
//        return week;
    }
}
