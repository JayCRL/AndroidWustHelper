package com.example.wusthelper.ui.activity;

import androidx.appcompat.app.AlertDialog;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import com.example.wusthelper.R;
import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.databinding.ActivityFontSizeSettingBinding;
import com.example.wusthelper.helper.SharePreferenceLab;

/**
 *课程表字体大小设置
 * */

public class FontSizeSettingActivity extends BaseActivity<ActivityFontSizeSettingBinding> implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener{

    private static final String TAG = "CourseBgSettingActivity";

    private int minNum = 6; //字体大小最小值

    /*用于添加上课界面的跳转*/
    public static Intent newInstance(Context context) {
        return new Intent(context, FontSizeSettingActivity.class);
    }



    @Override
    public void initView() {


        initListener();
        refresh_font_size();
        getBinding().switchFontStatus.setChecked(SharePreferenceLab.getIsItalic());
    }

    private void refresh_font_size() {
        int fontSize = SharePreferenceLab.getFontSize();
        getBinding().courseFontWhiteSar.setProgress(fontSize-minNum);
        String msg= (int) (fontSize)+"dp";
        getBinding().tvCourseFontWhiteSize.setText(msg);
        getBinding().tvCourseClassname.setTextSize(fontSize);
        getBinding().tvCourseClassroom.setTextSize(fontSize);
    }

    private void initListener() {
        getBinding().ivBack.setOnClickListener(this);
        getBinding().ivIntroduce.setOnClickListener(this);
        getBinding().switchFontStatus.setOnCheckedChangeListener(this);

        getBinding().courseFontWhiteSar.setMax(24);
        getBinding().courseFontWhiteSar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress = progress + minNum;
                String msg= (int) (progress)+"dp";
                getBinding().tvCourseFontWhiteSize.setText(msg);
                getBinding().tvCourseClassname.setTextSize(progress);
                getBinding().tvCourseClassroom.setTextSize(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharePreferenceLab.setFontSize(minNum+seekBar.getProgress());
            }
        });

    }

    @Override
    public void onClick(View v) {
        if(v.equals(getBinding().ivBack)){
            finish();
        } else if(v.equals(getBinding().ivIntroduce)){

            View dialog_explain = LayoutInflater.from(this)
                    .inflate(R.layout.dialog_font_size_explain, null);
            AlertDialog.Builder builder_explain = new AlertDialog.Builder(this);
            builder_explain.setView(dialog_explain);
            builder_explain.setPositiveButton("我知道了", null);
            builder_explain.show();
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(buttonView.equals(getBinding().switchFontStatus)){
            SharePreferenceLab.setIsItalic(isChecked);
            if (SharePreferenceLab.getIsItalic()){
                getBinding().tvCourseClassname.setTypeface(null, Typeface.ITALIC);
                getBinding().tvCourseClassroom.setTypeface(null, Typeface.ITALIC);
            }else{
                getBinding().tvCourseClassname.setTypeface(null, Typeface.NORMAL);
                getBinding().tvCourseClassroom.setTypeface(null, Typeface.NORMAL);
            }
        }
    }
}