/*
 * @author yuandalai
 * @date 2018/11/11
 * @email yuanlai0611@gmail.com
 * @github https://github.com/yuanlai0611
 * @blog https://yuanlai0611.github.io/
 */

package com.example.wusthelper.ui.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.wusthelper.R;
import com.example.wusthelper.bindeventbus.EventBusUtils;
import com.example.wusthelper.bindeventbus.EventCode;
import com.example.wusthelper.bindeventbus.coursefunction.ClassTimeData;
import com.example.wusthelper.bindeventbus.coursefunction.ClassTimeMessage;
import com.example.wusthelper.utils.ScreenUtils;
import com.lantouzi.wheelview.WheelView;

import java.util.ArrayList;
import java.util.List;

public class ClassSelectDialogFragment extends DialogFragment implements View.OnClickListener, WheelView.OnWheelItemSelectedListener {

    private List<String> classList = new ArrayList<>();
    private List<String> showClassList = new ArrayList<>();
    private Button cancelButton;
    private Button confirmButton;
    private WheelView startClassWheelView;
    private WheelView endClassWheelView;
    private static final String TAG = "ClassSelectDialog";
    public static final String START_TIME = "startTime";
    public static final String END_TIME = "endTime";
    private int startTime;
    private int endTime;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_class_select, null, false);
        Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);
        return dialog;

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_class_select, null, false);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        initClass();
        initView(view);
        setListener();
        startClassWheelView.setItems(showClassList);
        endClassWheelView.setItems(showClassList);

        Bundle bundle = getArguments();
        if (bundle != null) {

            startTime = bundle.getInt(START_TIME, 1);
            endTime = bundle.getInt(END_TIME, 1);
            Log.d(TAG, "" + startTime);
            Log.d(TAG, "" + endTime);
            startClassWheelView.selectIndex((startTime+1)/2-1);
            endClassWheelView.selectIndex(endTime/2-1);
        }

        return view;

    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(ScreenUtils.dp2px(340), ScreenUtils.dp2px(240));
    }

    private void initView(View view) {
        confirmButton = (Button)view.findViewById(R.id.btn_confirm);
        cancelButton = (Button)view.findViewById(R.id.btn_cancel);
        startClassWheelView = (WheelView)view.findViewById(R.id.wv_start_class);
        endClassWheelView = (WheelView)view.findViewById(R.id.wv_end_class);
    }

    private void setListener() {
        confirmButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        startClassWheelView.setOnWheelItemSelectedListener(this);
    }

    private void initClass() {
        classList.add(" 1 ");
        classList.add(" 2 ");
        classList.add(" 3 ");
        classList.add(" 4 ");
        classList.add(" 5 ");
        classList.add(" 6 ");

        showClassList.add(" 1-2 ");
        showClassList.add(" 3-4 ");
        showClassList.add(" 5-6 ");
        showClassList.add(" 7-8 ");
        showClassList.add(" 9-10 ");
        showClassList.add(" 11-12 ");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_confirm:

                int startTime = Integer.parseInt(classList.get(startClassWheelView.getSelectedPosition()).substring(1, 2)) * 2 - 1;
                int endTime = Integer.parseInt(classList.get(endClassWheelView.getSelectedPosition()).substring(1, 2)) * 2;
                Log.d(TAG, "" + classList.get(startClassWheelView.getSelectedPosition()));
                Log.d(TAG, "" + classList.get(endClassWheelView.getSelectedPosition()));
                Log.d(TAG, "" + classList.get(startClassWheelView.getSelectedPosition()).substring(1, 2));
                Log.d(TAG, "" + classList.get(endClassWheelView.getSelectedPosition()).substring(1, 2));
                ClassTimeMessage classTimeMessage = new ClassTimeMessage(EventCode.CLASS_TIME,new ClassTimeData());
                classTimeMessage.getData().setStartTime(startTime);
                classTimeMessage.getData().setEndTime(endTime);
                EventBusUtils.sendEvent(classTimeMessage);
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
        switch (wheelView.getId()) {
            case R.id.wv_start_class:
                endClassWheelView.setMinSelectableIndex(position);
                break;
            default:
                break;
        }
    }

    @Override
    public void onWheelItemSelected(WheelView wheelView, int position) {

    }

}
