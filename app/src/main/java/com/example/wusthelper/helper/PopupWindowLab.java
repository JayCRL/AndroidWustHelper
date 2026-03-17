package com.example.wusthelper.helper;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.example.wusthelper.R;


public class PopupWindowLab implements View.OnClickListener, CompoundButton.OnCheckedChangeListener{

    private static PopupWindowLab popupWindowLab;

    public static PopupWindowLab getInstance() {

        if (popupWindowLab == null) popupWindowLab = new PopupWindowLab();
        return popupWindowLab;
    }

    public interface onPopupWindowItemClickListener {

        void onItemClick(View view);

    }

    public interface onPopupWindowItemCheckedChangeListener {

        void onItemCheckedChange(CompoundButton compoundButton, boolean b);

    }

    private static onPopupWindowItemClickListener onPopupWindowItemClickListener;

    private static onPopupWindowItemCheckedChangeListener onPopupWindowItemCheckedChangeListener;

    public PopupWindow showPopupWindow(View anchorView, final Context context, onPopupWindowItemClickListener itemClickListener, onPopupWindowItemCheckedChangeListener itemCheckedChangeListener) {

        PopupWindow popupWindow = new PopupWindow(context);
        View view = LayoutInflater.from(context).inflate(R.layout.pop_course_menu, null, false);
        popupWindow.setContentView(view);



        LinearLayout showAllClassLinearLayout = (LinearLayout) view.findViewById(R.id.ll_show_all_class);
        LinearLayout setCurrentWeekLinearLayout = (LinearLayout) view.findViewById(R.id.ll_set_current_week);
        LinearLayout changeBackgroundLinearLayout = (LinearLayout) view.findViewById(R.id.ll_change_background);
        //    LinearLayout shareMyScheduleLinearLayout = (LinearLayout) view.findViewById(R.id.ll_share_my_schedule);
        LinearLayout selectSemesterLinearLayout = (LinearLayout) view.findViewById(R.id.ll_select_semester);
        LinearLayout selectCampusLinearLayout = (LinearLayout) view.findViewById(R.id.ll_select_campus);
        CheckBox isShowAllCheckBox = (CheckBox) view.findViewById(R.id.cb_show_all);

        LinearLayout homepageLinearLayout = (LinearLayout) view.findViewById(R.id.ll_change_homepage);
        LinearLayout chooseSundayLinearLayout = (LinearLayout) view.findViewById(R.id.ll_change_setting);
        CheckBox homepageCheckBox = (CheckBox) view.findViewById(R.id.change_homepage_check);
        CheckBox ChooseSundayCheckBox = (CheckBox) view.findViewById(R.id.choose_sunday_check);
        //    LinearLayout scanLinearLayout = view.findViewById(R.id.ll_scan);

        boolean isShowAll = SharePreferenceLab.getInstance().getIsShowNotThisWeek(context);
        boolean isShowBackground = SharePreferenceLab.getInstance().getIsShowBackground(context);
        isShowAllCheckBox.setChecked(isShowAll);
        boolean isHomepage= SharePreferenceLab.getInstance().getHomepage_settings(context);
        homepageCheckBox.setChecked(isHomepage);

        ChooseSundayCheckBox.setChecked(SharePreferenceLab.getInstance().getIsChooseSundayFirst(context));

        onPopupWindowItemClickListener = itemClickListener;
        onPopupWindowItemCheckedChangeListener = itemCheckedChangeListener;

        showAllClassLinearLayout.setOnClickListener(this);
        selectSemesterLinearLayout.setOnClickListener(this);
        setCurrentWeekLinearLayout.setOnClickListener(this);
        changeBackgroundLinearLayout.setOnClickListener(this);
        chooseSundayLinearLayout.setOnClickListener(this);
        //   shareMyScheduleLinearLayout.setOnClickListener(this);
        selectCampusLinearLayout.setOnClickListener(this);
        homepageLinearLayout.setOnClickListener(this);
        //   scanLinearLayout.setOnClickListener(this);
//        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
       // popupWindow.setAnimationStyle(R.style.popup_window);
//        popupWindow.setFocusable(true);
//        popupWindow.setTouchable(true);
//        popupWindow.setOutsideTouchable(true);
        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(outMetrics);
        int width = outMetrics.widthPixels;
        popupWindow.showAsDropDown(anchorView, width, 0);

        // 持续时间太久会存在掉帧现象
        startAnimator(1, 0.7f, 10, new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                AppCompatActivity activity = (AppCompatActivity)context;
                WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
                lp.alpha = (float)valueAnimator.getAnimatedValue();
                activity.getWindow().setAttributes(lp);
            }
        });
       // backgroundAlpha(0.5f,context);

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                //backgroundAlpha(1.0f,context);
//
                startAnimator(0.7f, 1, 250, new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        AppCompatActivity activity = (AppCompatActivity)context;
                        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
                        lp.alpha = (float)valueAnimator.getAnimatedValue();
                        activity.getWindow().setAttributes(lp);
                    }
                });

            }
        });

        return popupWindow;

    }

    private Animator startAnimator(float startValue, float endValue, int duration, ValueAnimator.AnimatorUpdateListener listener) {

        ValueAnimator animator = ValueAnimator.ofFloat(startValue, endValue);
        animator.setDuration(duration);
        animator.addUpdateListener(listener);
        animator.start();
        return animator;

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton.isPressed()) {
            onPopupWindowItemCheckedChangeListener.onItemCheckedChange(compoundButton, b);
        }
    }

    @Override
    public void onClick(View view) {
        onPopupWindowItemClickListener.onItemClick(view);
    }

    private void backgroundAlpha(float f,Context mContext) {

        WindowManager.LayoutParams lp = ((Activity) mContext).getWindow().getAttributes();

        lp.alpha = f;

        ((Activity) mContext).getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//背景变暗

        ((Activity) mContext).getWindow().setAttributes(lp);

    }


}