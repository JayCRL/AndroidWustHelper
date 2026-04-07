package com.example.wusthelper.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.wusthelper.MyApplication;
import com.example.wusthelper.R;
import com.example.wusthelper.appwidget.CountDownWidgetProvider;
import com.example.wusthelper.appwidget.TodayScheduleWidgetProvider;
import com.example.wusthelper.appwidget.WeekScheduleWidgetProvider;
import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.databinding.ActivityWidgetSettingsBinding;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.ui.dialog.ColorSelectorDialog;
import com.example.wusthelper.utils.GlideRoundTransform;
import com.example.wusthelper.utils.MyBitmapTool;
import com.example.wusthelper.utils.ToastUtil;
import com.google.android.material.tabs.TabLayout;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class WidgetSettingsActivity extends BaseActivity<ActivityWidgetSettingsBinding> implements View.OnClickListener,
        ColorSelectorDialog.OnColorChangeListener, ColorSelectorDialog.OnColorOverListener {

    private static final String TAG = "WidgetSettingsActivity";

    public static final int COUNTDOWN=0;
    public static final int TODAY=1;
    public static final int WEEK=2;


    private static final int WHITE=0x1;
    private static final int BLACK=0x2;
    private static final int REQUEST_OPEN_ALBUM = 1;

    private int widgetType;

    private String[] titleList = {"倒计时","每日课表","每周课表"};

    private int widgetColor;

    private int alpha;

    private Bitmap widgetBackground;

    private boolean isDefaultBackground = false;
    private int defaultBackgroundId;

    private OptionsPickerView<String> refreshOptions;
    private ColorSelectorDialog mDialog;
    //默认主题
    private int defaultOption;

    private final RequestOptions options = new RequestOptions()
            .centerCrop().transform(new GlideRoundTransform(10));;


//    public static Intent newInstance(Context context) {
//        return new Intent(context, WidgetSettingsActivity.class);
//    }

    @Override
    public void initView() {

        initData();

        widgetType = getIntent().getIntExtra("widgetType",1);
        Log.d(TAG, "initView: "+widgetType);
        getIntent().removeExtra("widgetType");


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            initWallpaper();
        }
        setListener();
        setWidgetStyle();
        setTitle();

        mDialog = new ColorSelectorDialog(this,this,this);
    }

    private void setTitle() {
        for(int i=0;i<titleList.length;i++){
            if(i==widgetType){
                getBinding().tabWidget.addTab(getBinding().tabWidget.newTab().setText(titleList[i]),true);
            }else{
                getBinding().tabWidget.addTab(getBinding().tabWidget.newTab().setText(titleList[i]),false);
            }

        }

    }

    private void setListener() {
        getBinding().ivBack.setOnClickListener(this);
        getBinding().ivWidgetSettingsIntroduce.setOnClickListener(this);
        getBinding().rlWidgetBgSelect.setOnClickListener(this);
        getBinding().rlWidgetReSettings.setOnClickListener(this);
        getBinding().rlWidgetTextColorSelect.setOnClickListener(this);

        getBinding().sar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String msg= (int) (progress/2.5)+"%";
                getBinding().tvWidgetBgAlpha.setText(msg);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                alpha= seekBar.getProgress();
                saveAlpha();
                initData();
                setWidgetStyle();

            }
        });


        getBinding().tabWidget.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d(TAG, "onTabSelected: "+tab.getPosition());
                widgetType = tab.getPosition();
                initData();
                setWidgetStyle();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                Log.d(TAG, "onTabUnselected: "+tab.getPosition());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Log.d(TAG, "onTabReselected: "+tab.getPosition());
            }
        });
    }


    private void setWidgetStyle() {

        Log.d(TAG, "setWidgetStyle: ");
        if(isDefaultBackground){
            //如果是默认的背景主题
            Glide.with(this)
                    .applyDefaultRequestOptions(options)
                    .load(defaultBackgroundId)
                    .into(getBinding().ivWidgetSettingBg);
        }else{
//            getBinding().ivWidgetSettingBg.setImageBitmap(widgetBackground);
            Glide.with(this)
                    .applyDefaultRequestOptions(options)
                    .load(widgetBackground)
                    .into(getBinding().ivWidgetSettingBg);
        }

        setTextColor();
        setAlpha();
    }

    private void setAlpha() {
        getBinding().sar.setProgress(alpha);
        getBinding().tvWidgetBgAlpha.setText((int) (alpha/2.5)+"%");

    }

    private void setTextColor() {

        getBinding().tvWidgetSettingTitle.setTextColor(widgetColor);
        getBinding().tvWidgetSettingSubtitle.setTextColor(widgetColor);
        getBinding().cardWidgetTextColorSelectShow.setBackgroundColor(widgetColor);

        Log.d(TAG, "setTextColor: Color1="+Color.parseColor("#FFFFFF"));
        Log.d(TAG, "setTextColor: Color2="+Color.parseColor("#ADB4BE"));
        Log.d(TAG, "setTextColor: Color3="+Color.parseColor("#323232"));
        Log.d(TAG, "setTextColor: Color3="+Color.parseColor("#000000"));
    }

    private void initData() {


        alpha = getAlpha();

        widgetColor = getWidgetColor();

        String path = getBackPath();

        if(path.equals("")){
            isDefaultBackground = true;
            defaultBackgroundId = getDefaultBackground();
        }else {
            Bitmap bitmap = MyBitmapTool.getRoundedCornerBitmap_from_PathFromString(this,path,alpha);
            if(bitmap==null){
                isDefaultBackground = true;
                defaultBackgroundId = getDefaultBackground();
            }else{
                isDefaultBackground = false;
                widgetBackground = bitmap;
            }
        }

    }


    private void initWallpaper() {

        // 获取壁纸管理器
        WallpaperManager wallpaperManager = WallpaperManager
                .getInstance(this);
        // 获取当前壁纸
        Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        // 设置 背景
        getBinding().bg.setBackground(wallpaperDrawable);
    }

    @Override
    public void onClick(View v) {
        if(v.equals(getBinding().ivBack)){
            finish();
        }else if(v.equals(getBinding().ivWidgetSettingsIntroduce)){
            View v_explain = LayoutInflater.from(this)
                    .inflate(R.layout.dialog_widget_explain, null);
            AlertDialog.Builder builder_explain = new AlertDialog.Builder(this);
            builder_explain.setView(v_explain);
            builder_explain.setPositiveButton("我知道了", null);
            builder_explain.show();
        }else if(v.equals(getBinding().rlWidgetBgSelect)){
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openAlbum();
            } else {
                ToastUtil.show("没有相机权限");
                EasyPermissions.requestPermissions(this, "选取本地图片作为头像，首先需要获取您的相册权限", REQUEST_OPEN_ALBUM, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }else if(v.equals(getBinding().rlWidgetReSettings)){
            initOptionPicker();
            refreshOptions.show();
        }else if(v.equals(getBinding().rlWidgetTextColorSelect)){
            mDialog.showDialog();
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
        switch (requestCode) {

            case REQUEST_OPEN_ALBUM:

                if (resultCode == RESULT_OK) {
                    List<Uri> backgroundPath = Matisse.obtainResult(data);
                    String path ="content://media" + backgroundPath.get(0).getPath();
                    Log.d(TAG, "onActivityResult: "+path);
                    try{
                        Bitmap bitmap = MyBitmapTool.getRoundedCornerBitmap_from_PathFromString_before_press(this,path);
                        Log.d(TAG, "onActivityResult: "+(bitmap==null));
                        if(MyBitmapTool.compare_Bitmap(bitmap,6000)){
                            saveWidgetBackgroundPath(path);

                            initData();
                            isDefaultBackground = false;
                            setWidgetStyle();
                            CountDownWidgetProvider.sendRefreshBroadcast(getApplicationContext());
                        }else{
                            ToastUtil.show("图片过大,超过了9mb,设置失败");
                        }
                    }catch (Exception e){
                        ToastUtil.show("图片加载错误,可能是没有权限");
                    }

                }
                break;
        }
    }




    private void initOptionPicker()
    {
        if(getDefault()){
            defaultOption =WHITE;
        }else{
            defaultOption =BLACK;
        }

        final List<String> options = new ArrayList<>();
        options.add("简洁白");
        options.add("主题黑");
        refreshOptions = new OptionsPickerBuilder(this, new OnOptionsSelectListener() {

            @Override
            public void onOptionsSelect(int options1, int options2, int options3, View v) {
                String s1 =options.get(options1);
                if(s1.equals("简洁白")){
                    saveDefaultSettings(""+ Color.parseColor("#000000"),"180","","white");
                    initData();

                    isDefaultBackground = true;
                    setWidgetStyle();
                    getBinding().sar.setProgress(180);

                }else{
                    saveDefaultSettings(""+ Color.parseColor("#FFFFFF"),"250","","black");
                    initData();

                    isDefaultBackground = true;
                    setWidgetStyle();
                    getBinding().sar.setProgress(250);
                }


            }

        }).setTitleText("选择主题")
                .setContentTextSize(20)
                .setDividerColor(Color.LTGRAY)
                .setBgColor(Color.WHITE)
                .setTitleBgColor(Color.WHITE)
                .setTitleColor(Color.GRAY)
                .setCancelColor(Color.GRAY)
                .setOutSideCancelable(true)
                .setSubmitColor(getResources().getColor(R.color.green_ok))
                .setTextColorCenter(getResources().getColor(R.color.green_ok))
                .build();
        refreshOptions.setPicker(options);
        refreshOptions.setSelectOptions(defaultOption-1);
    }



    @Override
    public void onColorChange(int color) {

    }

    @Override
    public void onColorOver(int color) {
        Log.d(TAG, "onColorOver: "+color);
        widgetColor = color;
        setTextColor();
        saveTextColor(color);
    }

    private String getBackPath() {
        if(widgetType==TODAY){
            return SharePreferenceLab.getInstance().getWidgetTodayBgPath(this);
        }else if(widgetType == COUNTDOWN){
            return SharePreferenceLab.getInstance().getWidgetCountdownBgPath(this);
        }else {
            return SharePreferenceLab.getInstance().getWidgetWeekBgPath(this);
        }
    }

    private int getWidgetColor() {
        if(widgetType==TODAY){
            String textColor= SharePreferenceLab.getInstance().getWidgetTodayTextColor(this);
            return Integer.parseInt(textColor);
        }else if(widgetType == COUNTDOWN){
            String textColor= SharePreferenceLab.getInstance().getWidgetCountdownTextColor(this);
            return Integer.parseInt(textColor);
        }else {
            String textColor= SharePreferenceLab.getInstance().getWidgetWeekTextColor(this);
            return Integer.parseInt(textColor);
        }
    }

    private int getAlpha() {
        if(widgetType==TODAY){
            String stringAlpha= SharePreferenceLab.getInstance().getWidgetTodayBgAlpha(this);
            return Integer.parseInt(stringAlpha);

        }else if(widgetType == COUNTDOWN){
            String stringAlpha= SharePreferenceLab.getInstance().getWidgetCountdownBgAlpha(this);
            return Integer.parseInt(stringAlpha);
        }else {
            String stringAlpha= SharePreferenceLab.getInstance().getWidgetWeekBgAlpha(this);
            return Integer.parseInt(stringAlpha);
        }
    }

    private int getDefaultBackground() {
        if(getDefault()){
            return R.drawable.shape_widget_bg_round_corners_100;
        }else {
            return R.drawable.shape_widget_bg_round_corners_100_black;
        }
    }


    private boolean getDefault() {

        if(widgetType==TODAY){
            return  SharePreferenceLab.getInstance().getWidgetTodayBgRefreshType(MyApplication.getContext()).equals("white");

        }else if(widgetType == COUNTDOWN){
            return  SharePreferenceLab.getInstance().getWidgetCountdownBgRefreshType(MyApplication.getContext()).equals("white");

        }else {
            return  SharePreferenceLab.getInstance().getWidgetWeekBgRefreshType(MyApplication.getContext()).equals("white");
        }

    }


    private void saveDefaultSettings(String s, String s1, String s2, String refreshType) {
        Log.d(TAG, "saveDefaultSettings: "+s);
        if(widgetType==TODAY){
            SharePreferenceLab.getInstance().setWidgetTodayTextColor(MyApplication.getContext(),s);
            SharePreferenceLab.getInstance().setWidgetTodayBgAlpha(MyApplication.getContext(),s1);
            SharePreferenceLab.getInstance().setWidgetTodayBgPath(MyApplication.getContext(),s2);
            SharePreferenceLab.getInstance().setWidgetTodayBgRefreshType(MyApplication.getContext(),refreshType);

            TodayScheduleWidgetProvider.sendRefreshBroadcast(getApplicationContext());

        }else if(widgetType == COUNTDOWN){
            SharePreferenceLab.getInstance().setWidgetCountdownTextColor(MyApplication.getContext(),s);
            SharePreferenceLab.getInstance().setWidgetCountdownBgAlpha(MyApplication.getContext(),s1);
            SharePreferenceLab.getInstance().setWidgetCountdownBgPath(MyApplication.getContext(),s2);
            SharePreferenceLab.getInstance().setWidgetCountdownBgRefreshType(MyApplication.getContext(),refreshType);

            CountDownWidgetProvider.sendRefreshBroadcast(getApplicationContext());

        }else if(widgetType == WEEK){
            SharePreferenceLab.getInstance().setWidgetWeekTextColor(MyApplication.getContext(),s);
            SharePreferenceLab.getInstance().setWidgetWeekBgAlpha(MyApplication.getContext(),s1);
            SharePreferenceLab.getInstance().setWidgetWeekBgPath(MyApplication.getContext(),s2);
            SharePreferenceLab.getInstance().setWidgetWeekBgRefreshType(MyApplication.getContext(),refreshType);

            WeekScheduleWidgetProvider.sendRefreshBroadcast(getApplicationContext());
        }
    }

    private void saveWidgetBackgroundPath(String path) {

        if(widgetType==TODAY){
            SharePreferenceLab.getInstance().setWidgetTodayBgPath(this,path);

            TodayScheduleWidgetProvider.sendRefreshBroadcast(getApplicationContext());

        }else if(widgetType == COUNTDOWN){
            SharePreferenceLab.getInstance().setWidgetCountdownBgPath(this,path);

            CountDownWidgetProvider.sendRefreshBroadcast(getApplicationContext());

        }else if(widgetType == WEEK){
            SharePreferenceLab.getInstance().setWidgetWeekBgPath(this,path);

            WeekScheduleWidgetProvider.sendRefreshBroadcast(getApplicationContext());
        }


    }

    private void saveAlpha() {
        if(widgetType==TODAY){
            SharePreferenceLab.getInstance().setWidgetTodayBgAlpha(MyApplication.getContext(),alpha+"");

            TodayScheduleWidgetProvider.sendRefreshBroadcast(getApplicationContext());

        }else if(widgetType == COUNTDOWN){
            SharePreferenceLab.getInstance().setWidgetCountdownBgAlpha(MyApplication.getContext(),alpha+"");

            CountDownWidgetProvider.sendRefreshBroadcast(getApplicationContext());

        }else if(widgetType == WEEK){
            SharePreferenceLab.getInstance().setWidgetWeekBgAlpha(MyApplication.getContext(),alpha+"");

            WeekScheduleWidgetProvider.sendRefreshBroadcast(getApplicationContext());
        }
    }

    private void saveTextColor(int color) {
        if(widgetType==TODAY){
            SharePreferenceLab.getInstance().setWidgetTodayTextColor(this,color+"");

            TodayScheduleWidgetProvider.sendRefreshBroadcast(getApplicationContext());

        }else if(widgetType == COUNTDOWN){
            SharePreferenceLab.getInstance().setWidgetCountdownTextColor(this,color+"");

            CountDownWidgetProvider.sendRefreshBroadcast(getApplicationContext());

        }else if(widgetType == WEEK){
            SharePreferenceLab.getInstance().setWidgetWeekTextColor(this,color+"");

            WeekScheduleWidgetProvider.sendRefreshBroadcast(getApplicationContext());
        }

    }
}
