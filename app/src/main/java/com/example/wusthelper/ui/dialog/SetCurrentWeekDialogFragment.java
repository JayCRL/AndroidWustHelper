package com.example.wusthelper.ui.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.wusthelper.R;
import com.example.wusthelper.bindeventbus.EventCode;
import com.example.wusthelper.bindeventbus.coursefragment.CurrentWeekData;
import com.example.wusthelper.bindeventbus.coursefragment.CurrentWeekMessage;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.helper.TimeTools;
import com.example.wusthelper.bean.javabean.DateBean;
import com.example.wusthelper.utils.ScreenUtils;
import com.lantouzi.wheelview.WheelView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

//使用不频繁，暂时不进行重构
public class SetCurrentWeekDialogFragment extends DialogFragment implements View.OnClickListener, WheelView.OnWheelItemSelectedListener {

    private WheelView weekWheelView;
    private Button confirmButton;
    private Button cancelButton;
    private List<String> weekList = new ArrayList<>();
    private int selectedIndex = 1;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_set_current_week, null, false);
        Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(STYLE_NO_TITLE);
        dialog.setContentView(view);
        return dialog;

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_set_current_week, null, false);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        initView(view);
        setListener();
        initWeekList();
        return view;

    }

    private void initView(View view) {
        weekWheelView = (WheelView)view.findViewById(R.id.wv_current_week);
        confirmButton = (Button)view.findViewById(R.id.btn_confirm);
        cancelButton = (Button)view.findViewById(R.id.btn_cancel);
    }

    private void initWeekList() {

        DateBean dateBean = SharePreferenceLab.getInstance().getDateBean(getActivity());
        int currentWeek = TimeTools.getWeek(dateBean, TimeTools.getFormatToday());
        for (int i = 0; i < 25; i ++) {
            weekList.add("" + (i+1) +"周");
        }
        selectedIndex = currentWeek;
        weekWheelView.setItems(weekList);
        weekWheelView.selectIndex(currentWeek - 1);

    }

    private void setListener() {
        confirmButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        weekWheelView.setOnWheelItemSelectedListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(ScreenUtils.dp2px(300), ScreenUtils.dp2px(200));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_confirm:
                //saveDate(selectedIndex);
                CurrentWeekMessage currentWeekMessage = new CurrentWeekMessage(EventCode.CURRENT_WEEK,new CurrentWeekData());
                currentWeekMessage.getData().setThisWeek(selectedIndex);
                EventBus.getDefault().post(currentWeekMessage);
                dismiss();
                break;
            case R.id.btn_cancel:
                dismiss();
                break;
            default:
                break;
        }
    }

    @Override
    public void onWheelItemChanged(WheelView wheelView, int position) {

    }

    @Override
    public void onWheelItemSelected(WheelView wheelView, int position) {
        selectedIndex = position + 1;
    }

    private void saveDate(int week) {

        int weekday = TimeTools.getWeekday();
        String date = TimeTools.getFormatToday();
        SharePreferenceLab.setWeek(week);
        SharePreferenceLab.setWeekday(weekday);
        SharePreferenceLab.setDate(date);
//        SharePreferenceLab.getInstance().setWeek(getActivity(), week);
//        SharePreferenceLab.getInstance().setWeekday(getActivity(), weekday);
//        SharePreferenceLab.getInstance().setDate(getActivity(), date);

    }

}
