package com.example.wusthelper.ui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.databinding.ActivityTest2Binding;

public class TestActivity extends BaseActivity<ActivityTest2Binding> {

    private static final String TAG = "TestActivity";


    @Override
    public void initView() {

        getBinding().tvTest.setText("1231231");

        getBinding().btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ");
            }
        });
    }
}