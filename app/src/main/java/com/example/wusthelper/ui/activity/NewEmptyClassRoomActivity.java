package com.example.wusthelper.ui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.wusthelper.R;
import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.databinding.ActivityNewEmptyClassRoomBinding;

public class NewEmptyClassRoomActivity extends BaseActivity<ActivityNewEmptyClassRoomBinding> implements View.OnClickListener{

    public static Intent newInstance(Context context){
        return new Intent(context,NewEmptyClassRoomActivity.class);
    }

    @Override
    public void initView() {
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        getBinding().ivBack.setOnClickListener(this);
        getBinding().searchRoom.setOnClickListener(this);
        getBinding().searchCourse.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.equals(getBinding().ivBack)){
            finish();
        }else if(v.equals(getBinding().searchRoom)){
            startActivity(SearchClassRoomActivity.newInstance(this));
        }else if(v.equals(getBinding().searchCourse)){
            startActivity(SearchCourseActivity.newInstance(this));
        }
    }
}