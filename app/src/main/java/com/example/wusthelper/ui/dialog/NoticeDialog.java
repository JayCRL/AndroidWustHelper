package com.example.wusthelper.ui.dialog;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.wusthelper.databinding.DialogNoticeBinding;
import com.example.wusthelper.bean.javabean.NoticeBean;

import org.jetbrains.annotations.NotNull;
import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class NoticeDialog extends BaseDialogFragment<DialogNoticeBinding> {

    private static final String TAG = "NoticeDialog";

    //记录显示到第几个NoticeData,从0开始,作为下角标获取数据
    private int i;
    private List<NoticeBean> listForShow = new ArrayList<>();

    public NoticeDialog(List<NoticeBean> listForShow) {
        this.listForShow = listForShow;
    }


    @Override
    public void onActivityCreated(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        //设置监听器，用于调用接口
        setOnClickListener();
        initView();
        super.onActivityCreated(savedInstanceState);
    }

    public void  initView(){
        if(listForShow.size()>0){
            i = 0;
            showNoticeBean();
        }
    }

    @Override
    public void initListener() {

    }

    private void setOnClickListener() {

        List<NoticeBean> list = LitePal.findAll(NoticeBean.class);
        Log.d(TAG, "setOnClickListener: "+list.size());
        getBinding().btnNoticeDialogCancel.setOnClickListener(v -> {

            //确认过，对该数据Bean设置 确认If_confirm（true），并且保存
            listForShow.get(i).setIf_confirm(true);
            listForShow.get(i).updateAll("newsId = ?", listForShow.get(i).getNewsId()+"");
            //如果还有要显示的公告，则进行显示，如果没有就关闭弹窗
            if(i+1<listForShow.size()){
                i = i+1 ;
                showNoticeBean();
            }else{
                Log.d(TAG, "setOnClickListener:  dismiss();");
                dismiss();
            }
        });

        getBinding().btnNoticeDialogConfirm.setOnClickListener(v -> {
            Log.d(TAG, "setOnClickListener: "+i);
            //如果还有要显示的公告，则进行显示，如果没有就关闭弹窗
            if(i+1<listForShow.size()){
                i = i+1 ;
                showNoticeBean();
            }else{
                dismiss();
            }
        });
    }

    private void showNoticeBean() {
        getBinding().tvNoticeDialogTitle.setText(listForShow.get(i).getTitle());
        getBinding().tvNoticeDialogContent.setText(listForShow.get(i).getContent());
    }
}
