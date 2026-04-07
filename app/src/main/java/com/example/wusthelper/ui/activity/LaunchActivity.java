package com.example.wusthelper.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wusthelper.R;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.request.NewApiHelper;


/**
 * @author gongyunhao
 */
public class LaunchActivity extends AppCompatActivity {
    private String TAG = "LaunchActivity";
    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState );
        setContentView(R.layout.activity_launch);
//        竖屏
        setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //activity转场动画，淡入淡出
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onCreate: =-0=======================");
        if (isLogin()) {

            restoreToken();
            Intent intent = MainMvpActivity.newInstance(LaunchActivity.this);
            startActivity(intent);

        } else {
            Intent intent = LoginMvpActivity.newInstance(LaunchActivity.this);
            startActivity(intent);
        }
        LaunchActivity.this.finish();
    }

    @Override
    public void onBackPressed() {}
    private boolean isLogin() {
        return SharePreferenceLab.getInstance( ).getIsLogin( LaunchActivity.this );
    }

    private void restoreToken(){
        Log.e(TAG, "restoreToken: " );
        String token = SharePreferenceLab.getInstance().getToken(LaunchActivity.this);
        String msg = SharePreferenceLab.getInstance().getMessage(LaunchActivity.this);
        NewApiHelper.setToken(token);
        NewApiHelper.setMessage(msg);
    }
}