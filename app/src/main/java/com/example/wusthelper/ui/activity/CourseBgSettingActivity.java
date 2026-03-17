package com.example.wusthelper.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.wusthelper.R;
import com.example.wusthelper.MyApplication;
import com.example.wusthelper.base.activity.BaseMvpActivity;
import com.example.wusthelper.bean.javabean.CourseBean;
import com.example.wusthelper.databinding.ActivityCourseBgSettingBinding;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.mvp.presenter.CourseBgSettingPresenter;
import com.example.wusthelper.mvp.view.CourseBgSettingView;
import com.example.wusthelper.utils.MyBitmapTool;
import com.example.wusthelper.utils.ToastUtil;
import com.xuexiang.xutil.display.Colors;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.internal.entity.CaptureStrategy;


import org.litepal.LitePal;

import java.io.File;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;


public class CourseBgSettingActivity extends BaseMvpActivity<CourseBgSettingView, CourseBgSettingPresenter,
        ActivityCourseBgSettingBinding>
        implements CourseBgSettingView, View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "CourseBgSettingActivity";

    private int minNum = 6; //字体大小最小值

    //打开相册界面的请求码，用于确认相册界面回调以后的工作
    private static final int REQUEST_OPEN_ALBUM = 1;

    private final RequestOptions options = new RequestOptions().centerCrop();

    /*用于添加上课界面的跳转*/
    public static Intent newInstance(Context context) {
        return new Intent(context, CourseBgSettingActivity.class);
    }


    @Override
    public CourseBgSettingPresenter createPresenter() {
        return new CourseBgSettingPresenter();
    }

    @Override
    public CourseBgSettingView createView() {
        return this;
    }


    @Override
    public void initView() {
        getWindow().setStatusBarColor(Colors.WHITE);

        getPresenter().initPresenterData();

        initListener();
        refresh_image();
        refresh_image_white();
        refresh_font_size();
        getBinding().switchFontStatus.setChecked(SharePreferenceLab.getIsItalic());
        getBinding().switchCourseBgStatus.setChecked(SharePreferenceLab.getIsBackgroundFullScreen());
    }

    private void refresh_font_size() {
        CourseBean courseBean = LitePal.findFirst(CourseBean.class);
        if(courseBean!=null){
            getBinding().tvCourseClassname.setText(courseBean.getCourseName());
            getBinding().tvCourseClassroom.setText(courseBean.getClassRoom());
        }
        int fontSize = SharePreferenceLab.getFontSize();
        getBinding().courseFontWhiteSar.setProgress(fontSize-minNum);
        String msg= (int) (fontSize)+"sp";
        getBinding().tvCourseFontWhiteSize.setText(msg);
        getBinding().tvCourseClassname.setTextSize(fontSize);
        getBinding().tvCourseClassroom.setTextSize(fontSize);
    }

    private void refresh_image_white() {
        int alpha = SharePreferenceLab.getBackgroundAlpha();
        getBinding().ivCourseBgChangeWhite.setAlpha((float)alpha/255);
        getBinding().courseBgWhiteSar.setProgress(alpha);
    }

    private void initListener() {
        getBinding().ivCourseBgChange.setOnClickListener(this);
        getBinding().tvCallAlbumDelete.setOnClickListener(this);
        getBinding().switchCourseBgStatus.setOnCheckedChangeListener(this);
        getBinding().ivBack.setOnClickListener(this);
        getBinding().ivIntroduce.setOnClickListener(this);

        getBinding().courseBgWhiteSar.setMax(100);
        getBinding().courseBgWhiteSar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String msg= (int) (progress)+"%";
                getBinding().tvCourseBgWhiteAlpha.setText(msg);
                getBinding().ivCourseBgChangeWhite.setAlpha((float)progress/100);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharePreferenceLab.setBackgroundAlpha(seekBar.getProgress());
            }
        });

        getBinding().switchFontStatus.setOnCheckedChangeListener(this);
        getBinding().courseFontWhiteSar.setMax(24);
        getBinding().courseFontWhiteSar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress = progress + minNum;
                String msg= (int) (progress)+"sp";
                getBinding().tvCourseFontWhiteSize.setText(msg);
                getBinding().tvCourseClassname.setTextSize(progress);
                getBinding().tvCourseClassroom.setTextSize(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                getBinding().course.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharePreferenceLab.setFontSize(minNum+seekBar.getProgress());
                getBinding().course.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public void onClick(View v) {
        if( v.equals(getBinding().ivCourseBgChange) ||v.equals(getBinding().tvCourseCallAlbum)){
            showBgChange();
        } else if(v.equals(getBinding().tvCallAlbumDelete)){
            SharePreferenceLab.getInstance().setBackgroundPath(CourseBgSettingActivity.this, "");
            refresh_image_before_select();
        } else if(v.equals(getBinding().ivBack)){
            finish();
        } else if(v.equals(getBinding().ivIntroduce)){

            View dialog_explain = LayoutInflater.from(this)
                    .inflate(R.layout.dialog_background_explain, null);
            AlertDialog.Builder builder_explain = new AlertDialog.Builder(this);
            builder_explain.setView(dialog_explain);
            builder_explain.setPositiveButton("我知道了", null);
            builder_explain.show();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(buttonView.equals(getBinding().switchCourseBgStatus)){
            SharePreferenceLab.setIsBackgroundFullScreen(isChecked);
        }else if(buttonView.equals(getBinding().switchFontStatus)){
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

    private void showBgChange() {
        //调用相册
        if (ContextCompat.checkSelfPermission(CourseBgSettingActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "openAlbum: qzytest 相册" );
            openAlbum();
        } else {
            EasyPermissions.requestPermissions(this, "选取本地图片作为背景图片，首先需要获取您的相册权限", REQUEST_OPEN_ALBUM, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

    }

    private void openAlbum() {
        //打开相册
        Matisse.from(this)
                .choose(MimeType.ofAll())//图片类型
                .countable(false)//true:选中后显示数字;false:选中后显示对号
                .maxSelectable(1)//可选的最大数
                .capture(false)//选择照片时，是否显示拍照
                .captureStrategy(new CaptureStrategy(true, "${applicationId}.fileProvider"))
                //参数1 true表示拍照存储在共有目录，false表示存储在私有目录；参数2与 AndroidManifest中authorities值相同，用于适配7.0系统 必须设置
                .imageEngine(new GlideEngine())//图片加载引擎
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .forResult(REQUEST_OPEN_ALBUM);//
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OPEN_ALBUM) {
            if (resultCode == RESULT_OK) {

                List<Uri> backgroundPath = Matisse.obtainResult(data);
                String Path = "content://media" + backgroundPath.get(0).getPath();
                try {
                    String RealPath = MyBitmapTool.getRealPathFromString(MyApplication.getContext(), Path);
                    File file = new File(RealPath);
                    int size = (int) (file.length() / 1024);
                    if (size > 7000) {
                        ToastUtil.showShortToastCenter("图片太大，可能会卡顿");
                    }
                    SharePreferenceLab.getInstance().setBackgroundPath(CourseBgSettingActivity.this, Path);
                    refresh_image();
                } catch (Exception e) {
                    ToastUtil.showShortToastCenter("图片加载出错");
                }

            }
        }
    }

    private void refresh_image() {
        final String path=SharePreferenceLab.getInstance().getBackgroundPath(this);
        if(!path.equals("")){
            Glide.with(CourseBgSettingActivity.this)
                    .applyDefaultRequestOptions(options)
                    .load(path)
                    .into(getBinding().ivCourseBgChange);
            refresh_image_after_select();
        }else{
            refresh_image_before_select();
        }

    }

    private void refresh_image_after_select() {
        getBinding().tvCourseCallAlbum.setVisibility(View.GONE);
        getBinding().tvCallAlbumDelete.setVisibility(View.VISIBLE);
        getBinding().callAlbumIntroduce.setVisibility(View.VISIBLE);
    }

    private void refresh_image_before_select() {
        Glide.with(CourseBgSettingActivity.this)
                .applyDefaultRequestOptions(options)
                .load(R.drawable.defaultbg)
                .into(getBinding().ivCourseBgChange);

        getBinding().tvCourseCallAlbum.setVisibility(View.VISIBLE);
        getBinding().tvCallAlbumDelete.setVisibility(View.GONE);
        getBinding().callAlbumIntroduce.setVisibility(View.GONE);
    }


}