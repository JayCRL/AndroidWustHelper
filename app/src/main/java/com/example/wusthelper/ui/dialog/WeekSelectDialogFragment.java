package com.example.wusthelper.ui.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.wusthelper.R;
import com.example.wusthelper.bindeventbus.EventBusUtils;
import com.example.wusthelper.bindeventbus.EventCode;
import com.example.wusthelper.bindeventbus.coursefunction.WeekSelectData;
import com.example.wusthelper.bindeventbus.coursefunction.WeekSelectMessage;
import com.example.wusthelper.databinding.DialogWeekSelectBinding;
import com.example.wusthelper.ui.dialog.BaseDialogFragment;
import com.example.wusthelper.utils.ScreenUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
/**
 * 周数选择界面dialog
 * 从添加课程界面跳转显示，没有进行很多代码重构，可能存在着问题
 * */
public class WeekSelectDialogFragment extends BaseDialogFragment<DialogWeekSelectBinding>
        implements View.OnClickListener, View.OnTouchListener {

    private Button cancelButton;
    private Button confirmButton;
    private GridLayout classContainerGridLayout;
    private List<TextView> weekTextViewList = new ArrayList<>();
    private float CLASS_LENGTH;
    private float tx;
    private float ty;
    // 表示是第几列
    private int dx;
    // 表示是第几行
    private int dy;
    // 表示第几个
    private int di;
    private int lastDi;
    private final String TAG = "WeekDialogFragment";
    public static String SELECT_WEEK = "selectWeek";
    private List<Integer> selectedWeeks;



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_week_select, null, false);
        Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);

        return dialog;
    }

    private List<Integer> getSelectedWeek() {

        List<Integer> weekList = new ArrayList<>();
        for (int i = 0; i < weekTextViewList.size(); i++) {
            if (weekTextViewList.get(i).isSelected()) {
                weekList.add(i + 1);
            }
        }

        return weekList;

    }

    @SuppressLint("NewApi")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_week_select, null, false);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        initView(view);
        setListener();
        CLASS_LENGTH = ScreenUtils.dp2px(50);

        Bundle bundle = getArguments();
        if (bundle != null) {
            selectedWeeks = bundle.getIntegerArrayList(SELECT_WEEK);
            Log.d(TAG, "" + selectedWeeks.size());
            for (int i=0; i<selectedWeeks.size(); i+=2) {
                int startWeek = selectedWeeks.get(i);
                int endWeek = selectedWeeks.get(i+1);
                Log.d(TAG, "" + startWeek);
                Log.d(TAG, "" + endWeek);
                for (int j=startWeek-1; j<=(endWeek-1); j++) {
                    weekTextViewList.get(j).setSelected(true);
                    weekTextViewList.get(j).setBackgroundColor(getActivity().getColor(R.color.theme_color));
                    weekTextViewList.get(j).setTextColor(getActivity().getColor(R.color.colorText));
                }
            }
        }

        return view;
    }

    private void setListener() {
        cancelButton.setOnClickListener(this);
        confirmButton.setOnClickListener(this);
        classContainerGridLayout.setOnTouchListener(this);
    }

    @SuppressLint("NewApi")
    private void initView(View view) {
        classContainerGridLayout = (GridLayout)view.findViewById(R.id.gl_week_container);
        cancelButton = (Button)view.findViewById(R.id.btn_cancel);
        confirmButton = (Button)view.findViewById(R.id.btn_confirm);
        weekTextViewList.add((TextView)view.findViewById(R.id.tv_week_1));
        weekTextViewList.add((TextView)view.findViewById(R.id.tv_week_2));
        weekTextViewList.add((TextView)view.findViewById(R.id.tv_week_3));
        weekTextViewList.add((TextView)view.findViewById(R.id.tv_week_4));
        weekTextViewList.add((TextView)view.findViewById(R.id.tv_week_5));
        weekTextViewList.add((TextView)view.findViewById(R.id.tv_week_6));
        weekTextViewList.add((TextView)view.findViewById(R.id.tv_week_7));
        weekTextViewList.add((TextView)view.findViewById(R.id.tv_week_8));
        weekTextViewList.add((TextView)view.findViewById(R.id.tv_week_9));
        weekTextViewList.add((TextView)view.findViewById(R.id.tv_week_10));
        weekTextViewList.add((TextView)view.findViewById(R.id.tv_week_11));
        weekTextViewList.add((TextView)view.findViewById(R.id.tv_week_12));
        weekTextViewList.add((TextView)view.findViewById(R.id.tv_week_13));
        weekTextViewList.add((TextView)view.findViewById(R.id.tv_week_14));
        weekTextViewList.add((TextView)view.findViewById(R.id.tv_week_15));
        weekTextViewList.add((TextView)view.findViewById(R.id.tv_week_16));
        weekTextViewList.add((TextView)view.findViewById(R.id.tv_week_17));
        weekTextViewList.add((TextView)view.findViewById(R.id.tv_week_18));
        weekTextViewList.add((TextView)view.findViewById(R.id.tv_week_19));
        weekTextViewList.add((TextView)view.findViewById(R.id.tv_week_20));
        weekTextViewList.add((TextView)view.findViewById(R.id.tv_week_21));
        weekTextViewList.add((TextView)view.findViewById(R.id.tv_week_22));
        weekTextViewList.add((TextView)view.findViewById(R.id.tv_week_23));
        weekTextViewList.add((TextView)view.findViewById(R.id.tv_week_24));
        weekTextViewList.add((TextView)view.findViewById(R.id.tv_week_25));



    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(ScreenUtils.dp2px(280), ScreenUtils.dp2px(330));
    }

    @Override
    public void initView() {

    }

    @Override
    public void initListener() {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_confirm:
                Log.d(TAG, "onClick: 1213");
                List<Integer> weekList = getSelectedWeek();
                WeekSelectMessage weekSelectMessage = new WeekSelectMessage(EventCode.COURSE_WEEK_SELECT,new WeekSelectData());
                weekSelectMessage.getData().setWeekList(weekList);
                EventBusUtils.sendEvent(weekSelectMessage);
                dismiss();
                break;
            case R.id.btn_cancel:
                dismiss();
                break;
            default:
                break;
        }
    }

    @SuppressLint("NewApi")
    private void reverseTextView() {
        try{
            if (weekTextViewList.get(dx+5*dy).isSelected()) {
                weekTextViewList.get(dx + 5 * dy).setSelected(false);
                weekTextViewList.get(dx + 5 * dy).setBackgroundColor(getActivity().getColor(R.color.colorText));
                weekTextViewList.get(dx + 5 * dy).setTextColor(getActivity().getColor(R.color.color_black));
            } else {
                weekTextViewList.get(dx+5*dy).setSelected(true);
                weekTextViewList.get(dx+5*dy).setBackgroundColor(getActivity().getResources().getColor(R.color.theme_color));
                weekTextViewList.get(dx+5*dy).setTextColor(getActivity().getResources().getColor(R.color.colorText));
            }
        }catch (Exception e){
            Log.d(TAG, "reverseTextView: ");
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        float x = motionEvent.getX();
        float y = motionEvent.getY();

        switch (motionEvent.getAction()) {

            case MotionEvent.ACTION_DOWN:
                dx = (int)(x / CLASS_LENGTH);
                dy = (int)(y / CLASS_LENGTH);
                Log.d(TAG, "到了第" + (dx+5*dy+1));
                reverseTextView();
                lastDi = dx+5*dy+1;
                //Log.d(TAG, "ACTION_DOWN  ==  " + x + " : " + y);
                break;
            case MotionEvent.ACTION_MOVE:
                // 如果超出MOVE超出范围就会不消费
                if (x >= 5*CLASS_LENGTH || x <= 0)  return false;
                if (y >= 5*CLASS_LENGTH || y <= 0)  return false;

                dx = (int)(x / CLASS_LENGTH);
                dy = (int)(y / CLASS_LENGTH);
                Log.d(TAG, "到了第" + (dx+5*dy+1));
                di = dx+5*dy+1;
                if (di != lastDi) {
                    reverseTextView();
                    lastDi = di;
                } else {
                    lastDi = di;
                }

                Log.d(TAG, "ACTION_MOVE  ==  " + x + " : " + y);
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }

}