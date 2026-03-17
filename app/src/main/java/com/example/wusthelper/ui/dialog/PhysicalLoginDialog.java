package com.example.wusthelper.ui.dialog;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AlertDialog;

import com.example.wusthelper.MyApplication;
import com.example.wusthelper.bean.javabean.CourseBean;
import com.example.wusthelper.bean.javabean.data.BaseData;
import com.example.wusthelper.bean.javabean.data.CourseData;
import com.example.wusthelper.databinding.DialogPhysicalLoginBinding;
import com.example.wusthelper.dbhelper.CourseDB;
import com.example.wusthelper.helper.MyDialogHelper;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.mvp.model.HomePageModel;
import com.example.wusthelper.request.okhttp.exception.OkHttpException;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;
import com.example.wusthelper.ui.activity.PhysicalDetailActivity;
import com.example.wusthelper.ui.dialog.listener.PhysicalLoginDialogListener;
import com.example.wusthelper.utils.ToastUtil;

public class PhysicalLoginDialog extends BaseDialogFragment<DialogPhysicalLoginBinding> {


    private static final String TAG = "PhysicalLoginDialog";

    /**
     * UI
     * */
    private AlertDialog loadingView;

    private HomePageModel model;

    private PhysicalLoginDialogListener listener;

    public PhysicalLoginDialog(PhysicalLoginDialogListener listener) {
        this.listener = listener;
    }

    @Override
    public void initView() {
        model = new HomePageModel();
        getBinding().editPhysicalLoginStudentId.setText(SharePreferenceLab.getStudentId());
        setCancelable(true);
    }

    @Override
    public void initListener() {
        getBinding().btnPhysicalLogin.setOnClickListener(v -> {
            String password = getBinding().editPhysicalLoginPassword.getText().toString();
            makeLogin(password);
        });
    }

    public void makeLogin(String password) {

        if(password.equals("")){
            ToastUtil.show("密码不能为空");
            return;
        }
        listener.loginPhysical(password);
        dismiss();

//        loadingView = MyDialogHelper.createLoadingDialog(getContext(),"登录中...", false);
//        loadingView.show();
//        model.postLoginPhysical(password, new DisposeDataListener() {
//            @Override
//            public void onSuccess(Object responseObj) {
//                BaseData data = (BaseData) responseObj;
//                Log.d(TAG, "onSuccess: "+data);
//                if(data.getCode().equals("10000")){
//                    listener.loginPhysical(true);
//                }else{
//                    ToastUtil.showShortToastCenter(data.getMsg());
//                    listener.loginPhysical(false);
//                }
//                loadingView.cancel();
//                dismiss();
//            }
//
//            @Override
//            public void onFailure(Object reasonObj) {
//                ToastUtil.show("登录失败，可能是网络状态不佳或者物理实验官网崩溃");
//                loadingView.cancel();
//            }
//        });
    }

    private void getPhysicalCourse() {
        model.getPhysicalCourse(new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                loadingView.cancel();
                CourseData data = (CourseData) responseObj;
                Log.d(TAG, "onSuccess: "+data);
                if(data.getCode().equals("10000")){
                    CourseDB.addAllCourseData(data.getData(),
                            SharePreferenceLab.getStudentId(),SharePreferenceLab.getSemester(),
                            CourseBean.TYPE_PHYSICAL);

                    SharePreferenceLab.getInstance().setIsPhysicalLogin(MyApplication.getContext(),
                            true);

                }else {
                    ToastUtil.showShortToastCenter(data.getMsg());
                }

            }

            @Override
            public void onFailure(Object reasonObj) {
                loadingView.cancel();
                Log.d(TAG, "onFailure: "+ ((OkHttpException)reasonObj).getEmsg());
                ToastUtil.show("登录成功,但是物理实验课表请求失败");
            }
        });
    }

}
