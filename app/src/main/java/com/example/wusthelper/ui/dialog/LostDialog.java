package com.example.wusthelper.ui.dialog;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.wusthelper.bean.javabean.LostBean;
import com.example.wusthelper.bean.javabean.NoticeBean;
import com.example.wusthelper.databinding.DialogNoticeBinding;
import com.example.wusthelper.request.NewApiHelper;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;

import org.litepal.LitePal;

import java.util.List;

public class LostDialog extends BaseDialogFragment<DialogNoticeBinding> {

    private static final String TAG = "LostDialog";

    private LostBean lostBean;

    public LostDialog(LostBean lostBean) {
        this.lostBean = lostBean;
    }


    @Override
    public void onActivityCreated(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        //设置监听器，用于调用接口
        setOnClickListener();
        initView();
        super.onActivityCreated(savedInstanceState);
    }

    public void  initView(){
        getBinding().tvNoticeDialogTitle.setText(lostBean.getTitle());
        getBinding().tvNoticeDialogContent.setText(lostBean.getContext());
    }

    @Override
    public void initListener() {

    }

    private void setOnClickListener() {

        List<NoticeBean> list = LitePal.findAll(NoticeBean.class);
        Log.d(TAG, "setOnClickListener: "+list.size());
        getBinding().btnNoticeDialogCancel.setOnClickListener(v -> {
            NewApiHelper.getLostMark(new DisposeDataListener() {
                @Override
                public void onSuccess(Object responseObj) {

                }

                @Override
                public void onFailure(Object reasonObj) {

                }
            });
            dismiss();
        });

        getBinding().btnNoticeDialogConfirm.setOnClickListener(v -> {
            dismiss();
        });
    }

}
