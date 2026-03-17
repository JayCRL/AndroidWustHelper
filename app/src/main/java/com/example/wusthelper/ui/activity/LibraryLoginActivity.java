package com.example.wusthelper.ui.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;

import com.example.wusthelper.R;
import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.base.activity.BaseMvpActivity;
import com.example.wusthelper.databinding.ActivityLibraryLoginBinding;
import com.example.wusthelper.helper.MyDialogHelper;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.mvp.presenter.LibraryLoginPresenter;
import com.example.wusthelper.mvp.view.LibraryLoginView;
import com.example.wusthelper.request.WustApi;
import com.example.wusthelper.utils.NetWorkUtils;
import com.example.wusthelper.utils.ToastUtil;

public class LibraryLoginActivity extends BaseMvpActivity<LibraryLoginView, LibraryLoginPresenter, ActivityLibraryLoginBinding>
        implements LibraryLoginView, View.OnClickListener{

    private static final String TAG = "LibraryLoginActivity";
    private boolean isHidePwd = true;// 输入框密码是否是隐藏的，默认为true

    public static Intent newInstance(Context context) {
        return new Intent(context, LibraryLoginActivity.class);
    }

    /**
     * UI
     * */
    private AlertDialog loadingDialog;

    @Override
    public LibraryLoginPresenter createPresenter() {
        return new LibraryLoginPresenter();
    }

    @Override
    public LibraryLoginView createView() {
        return this;
    }

    @Override
    public void initView() {
        getWindow().setStatusBarColor(Color.WHITE);
        getBinding().btnLoginLogin.setOnClickListener(this);
        getBinding().tvLibraryLoginGetHelp.setOnClickListener(this);
        getBinding().etStudentId.setText(SharePreferenceLab.getStudentId());
        getBinding().etStudentId.setEnabled(false);

        setPasswordEdit();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setPasswordEdit() {
        final Drawable[] drawables = getBinding().etLibraryPassword.getCompoundDrawables() ;
        final int eyeWidth = drawables[2].getBounds().width() ;// 眼睛图标的宽度
        final Drawable drawableEyeOpen = getResources().getDrawable(R.drawable.ic_baseline_visibility_24) ;
        drawableEyeOpen.setBounds(drawables[2].getBounds());//这一步不能省略

        getBinding().etLibraryPassword.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP){
                    // getWidth,getHeight必须在这里处理
                    float et_pwdMinX = v.getWidth() - eyeWidth - getBinding().etLibraryPassword.getPaddingRight() ;
                    float et_pwdMaxX = v.getWidth() ;
                    float et_pwdMinY = 0 ;
                    float et_pwdMaxY = v.getHeight();
                    float x = event.getX() ;
                    float y = event.getY() ;
                    if(x < et_pwdMaxX && x > et_pwdMinX && y > et_pwdMinY && y < et_pwdMaxY){
                        // 点击了眼睛图标的位置
                        isHidePwd = !isHidePwd ;
                        if(isHidePwd){
                            getBinding().etLibraryPassword.setCompoundDrawables(drawables[0] ,
                                    drawables[1] ,
                                    drawables[2] ,
                                    drawables[3]);

                            getBinding().etLibraryPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        }
                        else {
                            getBinding().etLibraryPassword.setCompoundDrawables(drawables[0] ,
                                    drawables[1] ,
                                    drawableEyeOpen ,
                                    drawables[3]);
                            getBinding().etLibraryPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
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
                final String password = getBinding().etLibraryPassword.getText().toString().trim();
                //登录，输入不为空
                if(password.length() > 0) {
                    getPresenter().login(password);
                }else {
                    ToastUtil.show("请将密码填写完整");
                }
            }else{
                ToastUtil.show("请连接网络");
            }

        }else if (getBinding().tvLibraryLoginGetHelp.equals(v)){
            OtherWebActivity.setName("帮助");
            OtherWebActivity.setUrl(WustApi.GET_HELP_LOGIN_URL);
            Intent privacy = OtherWebActivity.getInstance(this);
            startActivity(privacy);

        }
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
    public void openMainActivity() {
        Intent intent = LibraryActivity.newInstance(this);
        startActivity(intent);
        finish();
    }
}