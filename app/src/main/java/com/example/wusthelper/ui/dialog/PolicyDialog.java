package com.example.wusthelper.ui.dialog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.wusthelper.R;
import com.example.wusthelper.MyApplication;
import com.example.wusthelper.request.WustApi;
import com.example.wusthelper.databinding.DialogPolicyConfirmBinding;
import com.example.wusthelper.ui.dialog.listener.PolicyDialogListener;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.ui.activity.OtherWebActivity;

/**
 * 用于显示用户协议*/
public class PolicyDialog extends BaseDialogFragment<DialogPolicyConfirmBinding> {

    private static final int FIRST_TIME = 1;
    private static final int SECOND_TIME = 2;
    private static final int THIRD_TIME = 3;

    private PolicyDialogListener policyDialogListener;

    /*Tag用于判断policyDialog多次点击拒绝后，有不同的逻辑处理*/
    private int mTag = FIRST_TIME;


    public DialogPolicyConfirmBinding getBinding() {
        return super.getBinding();
    }

    /**
     * 设置Dialog的接口
     * @param listener */
    public void setDialogInterface(PolicyDialogListener listener){
        policyDialogListener = listener ;
    }


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


    public void setDialogData(String content ,String textYes,String textNo){
        getBinding().loginDialogContent.setText(content);
        getBinding().btnNoticeDialogPolicyPermit.setText(textYes);
        getBinding().btnNoticeDialogPolicyNotPermit.setText(textNo);

    }
    /*移除带有跳转链接的LinearLayout*/
    public void removeLLDialogPolicy(){
        getBinding().LLDialogPolicy.setVisibility(View.GONE);
    }

    /*添加带有跳转链接的LinearLayout*/
    public void addLLDialogPolicy(){
        getBinding().LLDialogPolicy.setVisibility(View.VISIBLE);
    }


    private void setOnClickListener() {
        getBinding().btnNoticeDialogPolicyPermit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 //调用接口，然后外部实现的接口逻辑就可以实现了
                if(mTag == FIRST_TIME){
                    mTag = SECOND_TIME;
                    dismiss();
                    policyDialogListener.onPolicyClickYes();
                    //缓存已经确认过隐私政策，下一次就不显示了
                    SharePreferenceLab.getInstance().set_is_confirm_policy(MyApplication.getContext(),
                            true);

                }else if(mTag == SECOND_TIME || mTag == THIRD_TIME){
                    mTag = FIRST_TIME;
                    addLLDialogPolicy();
                    setDialogData(getString(R.string.notice_policy_content),
                            getString(R.string.make_confirm_and_make_permission),getString(R.string.make_not_confirm));
                }
            }
        });

        getBinding().btnNoticeDialogPolicyNotPermit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mTag == FIRST_TIME){
                    mTag = SECOND_TIME ;
                    removeLLDialogPolicy();
                    setDialogData(getString(R.string.notice_policy_content_after),
                            getString(R.string.make_confirm_and_goto_policy),getString(R.string.make_not_confirm_final));
                }else if(mTag == SECOND_TIME){
                    mTag = THIRD_TIME ;
                    setDialogData(getString(R.string.notice_policy_content_final),
                            getString(R.string.make_confirm_and_goto_policy),getString(R.string.make_not_confirm_final));
                }else if(mTag == THIRD_TIME){
                    dismiss();
                    policyDialogListener.onFinish();
                }
            }
        });
        getBinding().LLDialogPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OtherWebActivity.setName("用户协议与隐私政策");
                OtherWebActivity.setUrl(WustApi.PRIVACY_URL);
                Intent privacy = OtherWebActivity.getInstance(getContext());
                startActivity(privacy);
            }
        });
    }
}
