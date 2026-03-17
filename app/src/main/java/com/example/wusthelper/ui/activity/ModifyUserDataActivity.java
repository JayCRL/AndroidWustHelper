package com.example.wusthelper.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.wusthelper.R;
import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.databinding.ActivityModifyUserDataBinding;
import com.example.wusthelper.helper.SharePreferenceLab;

import java.util.Timer;
import java.util.TimerTask;

public class ModifyUserDataActivity extends BaseActivity<ActivityModifyUserDataBinding> implements View.OnClickListener{

    public static final int USER_NAME = 1;
    public static final int MAJOR = 2;
    private static int type;
    public static final String TYPE = "type";

    @Override
    public void initView() {
        setListener();
        showKeyboard();
        initType();
    }

    private void initType() {
//        type = getIntent().getIntExtra(TYPE, 1);
        if (type == USER_NAME) {
            getBinding().tvHint.setText("修改你的名字，便于分享的时候让别人知道你是谁");
            getBinding().tbModify.setTitle("修改名字");
            String userName = SharePreferenceLab.getDisplayName();
            getBinding().etUserData.setText(userName);
            getBinding().etUserData.setSelection(userName.length());
        } else if (type == MAJOR){
            getBinding().tbModify.setTitle("修改专业");
            getBinding().tvHint.setText("修改你的专业，便于分享的时候让别人知道你是学什么的");
            String major = SharePreferenceLab.getInstance().getMajor(this);
            getBinding().etUserData.setText(major);
            getBinding().etUserData.setSelection(major.length());
        }
    }

    private void setListener() {
        getBinding().btnConfirmChange.setOnClickListener(this);
        getBinding().tbModify.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public static Intent newInstance(Context context,int type) {
        ModifyUserDataActivity.type = type;
        return new Intent(context, ModifyUserDataActivity.class);
    }

    private void showKeyboard() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask()   {
            public void run() {
                InputMethodManager inputManager = (InputMethodManager)getBinding().etUserData.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                assert inputManager != null;
                inputManager.showSoftInput(getBinding().etUserData, 0);
            }
        }, 300);
    }

    private void closeKeyboard() {
        View view = getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            assert inputMethodManager != null;
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_confirm_change:
                if (type == USER_NAME) {
                    SharePreferenceLab.setUserName(getBinding().etUserData.getText().toString());
                } else if (type == MAJOR) {
                    SharePreferenceLab.getInstance().setMajor(this, getBinding().etUserData.getText().toString());
                }
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeKeyboard();
    }
}