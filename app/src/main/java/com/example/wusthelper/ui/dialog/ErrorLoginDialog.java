package com.example.wusthelper.ui.dialog;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.wusthelper.MyApplication;
import com.example.wusthelper.R;
import com.example.wusthelper.databinding.DialogLoginErrorBinding;
import com.example.wusthelper.request.WustApi;
import com.example.wusthelper.ui.activity.OtherWebActivity;

public class ErrorLoginDialog extends BaseDialogFragment<DialogLoginErrorBinding>{

    public static String errorTitle;
    public static String errorContent;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getBinding().tvNoticeDialogTitle.setText(errorTitle);
        getBinding().tvNoticeDialogContent.setText(errorContent);
        if(errorTitle.equals(getString(R.string.notice_errorTitle_02))) {
            getBinding().tvNoticeDialogTitle.setTextSize(22);
            getBinding().tvNoticeDialogTitle.setTextColor(Color.parseColor("#C9C14239"));
        }
        setOnClickListener();
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void initView() {

    }

    @Override
    public void initListener() {

    }

    private void setOnClickListener() {
        getBinding().btnErrorDialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        getBinding().btnErrorDialogConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OtherWebActivity.setName("帮助");
                OtherWebActivity.setUrl(WustApi.GET_HELP_LOGIN_URL);
                Intent privacy = OtherWebActivity.getInstance(MyApplication.getContext());
                startActivity(privacy);
                dismiss();
            }
        });
    }
}
