package com.example.wusthelper.ui.dialog;

import android.Manifest;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.example.wusthelper.databinding.DialogPermissionBinding;

import org.jetbrains.annotations.NotNull;


public class PermissionDialog extends BaseDialogFragment<DialogPermissionBinding>{


    @Override
    public void onActivityCreated(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        //设置监听器，用于调用接口
        setOnClickListener();
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void initView() {

    }

    @Override
    public void initListener() {

    }

    private void setOnClickListener(){
        getBinding().btnNoticeDialogUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        });
    }
}
