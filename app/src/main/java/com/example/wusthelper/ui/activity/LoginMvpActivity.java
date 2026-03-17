package com.example.wusthelper.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.app.AlertDialog;


import com.example.wusthelper.R;
import com.example.wusthelper.appwidget.WeekScheduleWidgetProvider;
import com.example.wusthelper.bean.javabean.ConfigBean;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.request.WustApi;
import com.example.wusthelper.base.activity.BaseMvpActivity;
import com.example.wusthelper.databinding.ActivityLoginBinding;
import com.example.wusthelper.helper.MyDialogHelper;
import com.example.wusthelper.ui.dialog.ErrorLoginDialog;
import com.example.wusthelper.ui.dialog.PermissionDialog;
import com.example.wusthelper.ui.dialog.UpdateDialog;
import com.example.wusthelper.ui.dialog.listener.PolicyDialogListener;
import com.example.wusthelper.ui.dialog.PolicyDialog;
import com.example.wusthelper.mvp.presenter.LoginPresenter;
import com.example.wusthelper.mvp.view.LoginView;
import com.example.wusthelper.utils.NetWorkUtils;
import com.example.wusthelper.utils.ToastUtil;
import com.example.wusthelper.widget.SwitchView;
import com.xuexiang.xutil.display.Colors;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class LoginMvpActivity extends BaseMvpActivity<LoginView, LoginPresenter,ActivityLoginBinding>
        implements LoginView, View.OnClickListener , PolicyDialogListener {

    private static final String TAG = "LoginMvpActivity";
    private boolean isHidePwd = true;// 输入框密码是否是隐藏的，默认为true

    /**
     * UI
     * */
    private AlertDialog loadingDialog;
    private final PolicyDialog policyDialog = new PolicyDialog();

    public static Intent newInstance(Context context) {
        return new Intent(context, LoginMvpActivity.class);
    }

    @Override
    public LoginPresenter createPresenter() {
        return new LoginPresenter();
    }

    @Override
    public LoginView createView() {
        return this;
    }

    @Override
    public void initView() {
        getWindow().setStatusBarColor(Colors.WHITE);
        getBinding().btnLoginLogin.setOnClickListener(this);
        getBinding().tvLoginGetHelp.setOnClickListener(this);
        getBinding().tvVolunteer.setOnClickListener(this);
        //查看是否点击过隐私政策，如果没有确认过，则显示隐私政策弹窗
        if (!getPresenter().getIsConfirmPolicy()){
            show_confirm_dialog();
        }
        // 旧版本会在这里预拉配置（用于学期/更新弹窗）。
        // 目前已完全切到新服务端接口，登录流程不再依赖该配置接口，避免旧域名网络不可达导致卡死。
        // getPresenter().getConfig(null,null);

        SharePreferenceLab.setIsGraduate(false);
        getBinding().switchView.setOnClickCheckedListener(new SwitchView.onClickCheckedListener() {
            @Override
            public void onClick() {
                //工时系统
                SharePreferenceLab.setIsGraduate(getBinding().switchView.isChecked());
                if(getBinding().switchView.isChecked()) {
                    getBinding().tvVolunteer.setVisibility(View.GONE);
                } else {
                    getBinding().tvVolunteer.setVisibility(View.VISIBLE);
                }
            }
        });

        setPasswordEdit();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setPasswordEdit() {
        final Drawable[] drawables = getBinding().etPassword.getCompoundDrawables() ;
        final int eyeWidth = drawables[2].getBounds().width() ;// 眼睛图标的宽度
        final Drawable drawableEyeOpen = getResources().getDrawable(R.drawable.ic_baseline_visibility_24) ;
        drawableEyeOpen.setBounds(drawables[2].getBounds());//这一步不能省略
//        //如果输入密码内容是空，眼睛隐藏
//        if(!getBinding().etPassword.getText().toString().equals("")) {
//            getBinding().etPassword.setCompoundDrawables(drawables[0] ,
//                    drawables[1] ,
//                    drawables[2] ,
//                    drawables[3]);
//        } else {
//            getBinding().etPassword.setCompoundDrawables(drawables[0] ,
//                    drawables[0] ,
//                    drawables[0] ,
//                    drawables[0]);
//        }
//        //随时监听输入密码的内容
//        getBinding().etPassword.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                //有密码输入就显示 眼睛按钮
//                if(s.length() > 0) {
//                    getBinding().etPassword.setCompoundDrawables(drawables[0] ,
//                            drawables[1] ,
//                            drawables[2] ,
//                            drawables[3]);
//                } else {
//                    getBinding().etPassword.setCompoundDrawables(drawables[0] ,
//                            drawables[0] ,
//                            drawables[0] ,
//                            drawables[0]);
//                }
//            }
//        });

        getBinding().etPassword.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP){
                    // getWidth,getHeight必须在这里处理
                    float et_pwdMinX = v.getWidth() - eyeWidth - getBinding().etPassword.getPaddingRight() ;
                    float et_pwdMaxX = v.getWidth() ;
                    float et_pwdMinY = 0 ;
                    float et_pwdMaxY = v.getHeight();
                    float x = event.getX() ;
                    float y = event.getY() ;
                    if(x < et_pwdMaxX && x > et_pwdMinX && y > et_pwdMinY && y < et_pwdMaxY){
                        // 点击了眼睛图标的位置
                        isHidePwd = !isHidePwd ;
                        if(isHidePwd){
                            getBinding().etPassword.setCompoundDrawables(drawables[0] ,
                                    drawables[1] ,
                                    drawables[2] ,
                                    drawables[3]);

                            getBinding().etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        }
                        else {
                            getBinding().etPassword.setCompoundDrawables(drawables[0] ,
                                    drawables[1] ,
                                    drawableEyeOpen ,
                                    drawables[3]);
                            getBinding().etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        }
                    }
                }
                return false;
            }
        });
    }



    @Override
    public void onClick(View v) {

        if(getBinding().btnLoginLogin.equals(v)){
            if(NetWorkUtils.isConnected(getApplicationContext())){
                //去空格
                final String studentId = getBinding().etStudentId.getText().toString().trim();
                final String password = getBinding().etPassword.getText().toString().trim();
                //登录，输入不为空
                if(studentId.length() > 10 && password.length() > 0) {
                    getPresenter().login(studentId,password);
                }else {
                    ToastUtil.show("请将学号或密码填写完整");
                }
            }else{
                ToastUtil.show("请连接网络");
            }

        }else if (getBinding().tvLoginGetHelp.equals(v)){
            OtherWebActivity.setName("帮助");
            OtherWebActivity.setUrl(WustApi.GET_HELP_LOGIN_URL);
            Intent privacy = OtherWebActivity.getInstance(this);
            startActivity(privacy);

        }else if(getBinding().tvVolunteer.equals(v)){
            OtherWebActivity.setName("工时系统");
            OtherWebActivity.setUrl(WustApi.VOLUNTEER_URL);
            Intent volunteer = OtherWebActivity.getInstance(this);
            startActivity(volunteer);
//            Uri uri = Uri.parse(WustApi.VOLUNTEER_URL);
//            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//            startActivity(intent);
        }
    }

    /**
     * 隐私政策点击确认以后，就会调用这个方法
     * */
    @Override
    public void onPolicyClickYes() {
        show_permission_dialog();
    }

    @Override
    public void onToastShow(String msg) {
        ToastUtil.show(msg);
    }

    @Override
    public void onLoadingShow(String msg,boolean cancelable) {
        loadingDialog = MyDialogHelper.createLoginLoadingDialog(this,msg,cancelable);
        loadingDialog.show();
    }

    @Override
    public void onLoadingCancel() {
        if(loadingDialog!=null){
            loadingDialog.cancel();
        }
    }

    @Override
    public void onFinish() {
        finish();
    }

    @Override
    public void openMainActivity() {
        Intent intent = MainMvpActivity.newInstance(this);
        startActivity(intent);
        sendBroadcast();
        finish();
    }

    @Override
    public void showUpdateDialog(ConfigBean bean) {
        UpdateDialog updateDialog = new UpdateDialog();
        updateDialog.show(getSupportFragmentManager(), "Update");
    }

    @Override
    public void showErrorDialog() {
        ErrorLoginDialog errorLoginDialog = new ErrorLoginDialog();
        errorLoginDialog.show(getSupportFragmentManager(),"error_login");
    }

    /**
     * 展示获取权限弹窗
     * */
    private void show_permission_dialog(){
        PermissionDialog permissionDialog = new PermissionDialog();
        permissionDialog.show(getSupportFragmentManager(), "notice_permission");
    }
    /**
     * 展示隐私政策弹窗
     * */
    private void show_confirm_dialog() {
        policyDialog.setDialogInterface(this);
        policyDialog.show(getSupportFragmentManager(), "policyDialog");
    }

    private void sendBroadcast () {
        Intent intent = new Intent(this, WeekScheduleWidgetProvider.class);
        intent.setAction("com.android.linghang.wustcampus.AppWidget.REFRESH");
        sendBroadcast(intent, "com.android.linghang.wustcampus.AppWidget.REFRESH");
    }

}