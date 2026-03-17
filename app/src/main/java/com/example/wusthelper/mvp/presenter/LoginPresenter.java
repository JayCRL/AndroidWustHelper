package com.example.wusthelper.mvp.presenter;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.wusthelper.MyApplication;
import com.example.wusthelper.R;
import com.example.wusthelper.base.BasePresenter;
import com.example.wusthelper.bean.javabean.data.ConfigData;
import com.example.wusthelper.bean.javabean.data.CourseData;
import com.example.wusthelper.bean.javabean.data.GraduateData;
import com.example.wusthelper.bean.javabean.data.StudentData;
import com.example.wusthelper.bean.javabean.data.TokenData;
import com.example.wusthelper.helper.ConfigHelper;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.helper.TimeTools;
import com.example.wusthelper.bean.javabean.data.TermStartDateData;
import com.example.wusthelper.mvp.model.LoginModel;
import com.example.wusthelper.mvp.view.LoginView;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;
import com.example.wusthelper.ui.dialog.ErrorLoginDialog;
import com.example.wusthelper.utils.ToastUtil;

import java.util.HashMap;
import java.util.Map;

public class LoginPresenter extends BasePresenter<LoginView> {

    private static final String TAG = "LoginPresenter";

    private String semester;
    private final LoginModel loginModel;
    private final Map<String, Integer> errorStudentMap = new HashMap<>();
    private boolean isGetLogin = false;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingConfigTimeout;
    private Runnable pendingLoginTimeout;

    public LoginPresenter(){
        this.loginModel = new LoginModel();
    }

    @Override
    public void initPresenterData() {
    }

    public void getConfig(String studentId, String password){
        loginModel.getConfig(new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                ConfigData configData = (ConfigData) responseObj;
                if (isGetLogin) {
                    clearConfigTimeout();
                }
                if(configData.isSuccess()){
                    loginModel.saveConfig(configData);
                    if(isGetLogin && studentId != null && password != null){
                        isGetLogin = false;
                        login(studentId, password);
                    }else if(ConfigHelper.getIfHasNewVersion()){
                        getView().showUpdateDialog(ConfigHelper.getConfigBean().getData());
                    }
                } else if (isGetLogin) {
                    isGetLogin = false;
                    getView().onLoadingCancel();
                    ToastUtil.show(getMessage(configData.getMsg(), "初始化配置失败，请稍后重试"));
                }
            }

            @Override
            public void onFailure(Object reasonObj) {
                Log.e(TAG, "getConfig onFailure: " + reasonObj);
                if (isGetLogin) {
                    clearConfigTimeout();
                }
                getView().onLoadingCancel();
                if(isGetLogin){
                    isGetLogin = false;
                    ToastUtil.show("请求失败，可能是网络未链接或请求超时");
                }
            }
        });
    }

    public void login(String studentId, String password){
        if(!errorStudentMap.containsKey(studentId)) {
            errorStudentMap.put(studentId, 0);
        }
        // 学期统一由客户端按当前时间计算（避免依赖后台下发 currentTerm）
        semester = TimeTools.getLatestSemester();
        // 同步写入“真实最新学期”，用于课表页新学期提示等逻辑
        SharePreferenceLab.setSemester(semester);
        if(semester == null){
            semester = "";
        }
        getView().onLoadingShow("登录中...",false);
        startLoginTimeout();
        if(SharePreferenceLab.getIsGraduate()){
            loginModel.loginGraduate(studentId, password, new DisposeDataListener() {
                @Override
                public void onSuccess(Object responseObj) {
                    clearLoginTimeout();
                    TokenData tokenData = (TokenData) responseObj;
                    if(tokenData.isSuccess()){
                        loginModel.saveLoginData(tokenData, studentId, password, semester);
                        getUserInfo();
                        getTermStartDate();
                        getCourse();
                    }else{
                        getView().onLoadingCancel();
                        getView().onToastShow(getMessage(tokenData.getMsg(), "账号或密码错误"));
                    }
                }

                @Override
                public void onFailure(Object reasonObj) {
                    clearLoginTimeout();
                    ToastUtil.show("请求失败，可能是网络未链接或请求超时");
                    getView().onLoadingCancel();
                }
            });
        }else {
            loginModel.login(studentId, password, new DisposeDataListener() {
                @Override
                public void onSuccess(Object responseObj) {
                    clearLoginTimeout();
                    TokenData tokenData = (TokenData) responseObj;
                    if(tokenData.isSuccess()){
                        loginModel.saveLoginData(tokenData, studentId, password, semester);
                        getUserInfo();
                        getTermStartDate();
                        getCourse();
                    }else{
                        errorStudentMap.put(studentId, errorStudentMap.get(studentId) + 1);
                        getView().onLoadingCancel();
                        getView().onToastShow(getMessage(tokenData.getMsg(), "登录失败"));
                        showErrorDialogIfNeeded(studentId);
                    }
                }

                @Override
                public void onFailure(Object reasonObj) {
                    clearLoginTimeout();
                    getView().onLoadingCancel();
                    ToastUtil.show("登录请求失败：" + (reasonObj == null ? "" : reasonObj.toString()));
                }
            });
        }
    }

    private void showErrorDialogIfNeeded(String studentId) {
        int count = errorStudentMap.get(studentId);
        if (count == 3) {
            ErrorLoginDialog.errorTitle = MyApplication.getContext().getString(R.string.notice_errorTitle_01);
            ErrorLoginDialog.errorContent = MyApplication.getContext().getString(R.string.notice_errorContent_01);
            getView().showErrorDialog();
        } else if (count > 4) {
            ErrorLoginDialog.errorTitle = MyApplication.getContext().getString(R.string.notice_errorTitle_02);
            ErrorLoginDialog.errorContent = MyApplication.getContext().getString(R.string.notice_errorContent_02);
            getView().showErrorDialog();
        }
    }

    private void startConfigTimeout() {
        clearConfigTimeout();
        pendingConfigTimeout = () -> {
            if (!isGetLogin) {
                return;
            }
            isGetLogin = false;
            getView().onLoadingCancel();
            ToastUtil.show("初始化配置超时，请检查网络后重试");
        };
        mainHandler.postDelayed(pendingConfigTimeout, 15000);
    }

    private void clearConfigTimeout() {
        if (pendingConfigTimeout != null) {
            mainHandler.removeCallbacks(pendingConfigTimeout);
            pendingConfigTimeout = null;
        }
    }

    private void startLoginTimeout() {
        clearLoginTimeout();
        pendingLoginTimeout = () -> {
            getView().onLoadingCancel();
            ToastUtil.show("登录超时，请检查网络或稍后再试");
        };
        mainHandler.postDelayed(pendingLoginTimeout, 20000);
    }

    private void clearLoginTimeout() {
        if (pendingLoginTimeout != null) {
            mainHandler.removeCallbacks(pendingLoginTimeout);
            pendingLoginTimeout = null;
        }
    }

    public void getUserInfo(){
        if(SharePreferenceLab.getIsGraduate()){
            loginModel.getGraduateInfo(new DisposeDataListener() {
                @Override
                public void onSuccess(Object responseObj) {
                    GraduateData graduateData = (GraduateData) responseObj;
                    if(graduateData.isSuccess()){
                        loginModel.saveGraduateInfo(graduateData);
                    }
                }

                @Override
                public void onFailure(Object reasonObj) {
                    Log.e(TAG, "获取研究生信息失败: " + reasonObj);
                }
            });
        }else{
            loginModel.getUserInfo(new DisposeDataListener() {
                @Override
                public void onSuccess(Object responseObj) {
                    StudentData studentData = (StudentData) responseObj;
                    if(studentData.isSuccess()){
                        loginModel.saveStudentInfo(studentData);
                    }
                }

                @Override
                public void onFailure(Object reasonObj) {
                    Log.d(TAG, "获取本科生信息失败: " + reasonObj);
                }
            });
        }
    }

    public void getCourse(){
        getView().onLoadingCancel();
        getView().onLoadingShow("登录成功，正在请求课表...",false);
        if(SharePreferenceLab.getIsGraduate()) {
            loginModel.getGraduateCourse(new DisposeDataListener() {
                @Override
                public void onSuccess(Object responseObj) {
                    CourseData courseData = (CourseData) responseObj;
                    if(courseData.isSuccess()){
                        loginModel.saveAllCourseToDB(courseData.data, semester);
                    }else{
                        getView().onToastShow("登录成功，但是课表获取失败，可能是教务处不稳定");
                    }
                    getView().onLoadingCancel();
                    getView().openMainActivity();
                }

                @Override
                public void onFailure(Object reasonObj) {
                    getView().onToastShow("登录成功，但是课表获取失败，可能是教务处不稳定或者请求超时");
                    getView().onLoadingCancel();
                    getView().openMainActivity();
                }
            });
        }else {
            loginModel.getCourse(semester,new DisposeDataListener() {
                @Override
                public void onSuccess(Object responseObj) {
                    CourseData courseData = (CourseData) responseObj;
                    if(courseData.isSuccess()){
                        loginModel.saveAllCourseToDB(courseData.data, semester);
                    }else{
                        getView().onToastShow("登录成功，但是课表获取失败，可能是教务处不稳定");
                    }
                    getView().onLoadingCancel();
                    getView().openMainActivity();
                }

                @Override
                public void onFailure(Object reasonObj) {
                    getView().onToastShow("登录成功，但是课表获取失败，可能是教务处不稳定或者请求超时");
                    getView().onLoadingCancel();
                    getView().openMainActivity();
                }
            });
        }
    }

    /**
     * 拉取 basic 后端配置的“本学期起始日”，用于周次计算。
     * 不阻塞登录流程：失败直接忽略，后续仍可用旧 config 或兜底。
     */
    private void getTermStartDate() {
        loginModel.getTermStartDate(new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                TermStartDateData data = (TermStartDateData) responseObj;
                if (data != null && data.isSuccess() && data.data != null
                        && data.data.getYear() != null && data.data.getMonth() != null && data.data.getDay() != null) {
                    ConfigHelper.setTermStartDate(data.data.getYear(), data.data.getMonth(), data.data.getDay());
                }
            }

            @Override
            public void onFailure(Object reasonObj) {
                Log.w(TAG, "getTermStartDate onFailure: " + reasonObj);
            }
        });
    }

    private String getMessage(String msg, String fallback) {
        return msg == null || msg.trim().isEmpty() ? fallback : msg;
    }

    public boolean getIsConfirmPolicy() {
        return SharePreferenceLab.getInstance().get_is_confirm_policy(MyApplication.getContext());
    }
}
