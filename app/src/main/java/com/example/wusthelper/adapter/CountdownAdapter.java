package com.example.wusthelper.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;

import com.example.wusthelper.databinding.ItemCountdownBinding;
import com.example.wusthelper.bean.javabean.CountDownBean;
import com.example.wusthelper.ui.activity.CountdownActivity;
import com.example.wusthelper.utils.CountDownUtils;

import org.jetbrains.annotations.NotNull;

public class CountdownAdapter extends BaseBindingQuickAdapter<CountDownBean, ItemCountdownBinding> {

    private static final String TAG = "DateAdapter";

    private Context mContext;
    private boolean isLongClick = false;
    public CountdownAdapter(Context context){
        this.mContext = context;
    }


    @SuppressLint("SetTextI18n")
    @Override
    protected void convert(@NotNull BaseBindingHolder holder, CountDownBean item) {
        ItemCountdownBinding binding = holder.getViewBinding();
        
        if (CountDownUtils.checkState(item.getTargetTime())) {
            holder.itemView.setAlpha(1.0f);
            //倒计时天数设置
           binding.itemCountdownDaysText.setVisibility(View.VISIBLE);
            //倒计时状态
           binding.itemCountdownStateText.setText("进行中");
            //倒计时天数
           binding.itemCountdownDaysText.setText(CountDownUtils.getRemainDays(item.getTargetTime()) + "");
        } else {
            holder.itemView.setAlpha(0.8f);
           binding.itemCountdownStateText.setText("已结束");
           binding.itemCountdownDaysText.setText("0");

           binding.itemCountdownDayResidue.setVisibility(View.GONE);

        }
       binding.itemCountdownNameText.setText(item.getName());
        //设置结束时间
       binding.itemCountdownTimeText.setText(CountDownUtils.getShowTime(item.getTargetTime()));
        if (item.getNote().equals("")){
           binding.itemCountdownNoteText.setText("这家伙很懒，什么也没有留下");
        }else{
           binding.itemCountdownNoteText.setText(item.getNote());
        }
       binding.itemCountdownDaysStartText.setText(CountDownUtils.getStartDays(item.getCreateTime()) + "");

        holder.itemView.setOnClickListener(v -> {
//            //修改倒计时逻辑
//            if(mContext instanceof CountdownActivity){
//                CountdownActivity activity = (CountdownActivity) mContext;
//                activity.showDialog(item);
//
//            }
            CountdownActivity activity = (CountdownActivity) mContext;
            int position = activity.getPresenter().getCountDownData().countDownList.indexOf(item);
            if(isLongClick) {
                if(binding.deleteBox.isChecked()){
                    binding.deleteBox.setChecked(false);
                    activity.getPresenter().getCountDownData().countDownList.get(position).setCheck(false);
                }else {
                    binding.deleteBox.setChecked(true);
                    activity.getPresenter().getCountDownData().countDownList.get(position).setCheck(true);
                }
            }else {
                activity.showDialog(item);
            }
        });

        if(isLongClick) {
            binding.deleteBox.setVisibility(View.VISIBLE);
            binding.deleteBox.setChecked(item.isCheck());
            CountdownActivity activity = (CountdownActivity) mContext;
            int position = activity.getPresenter().getCountDownData().countDownList.indexOf(item);
            binding.deleteBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked) {
                        activity.getPresenter().getCountDownData().countDownList.get(position).setCheck(true);
                    }else {
                        activity.getPresenter().getCountDownData().countDownList.get(position).setCheck(false);
                    }
                }
            });
        }else {
            binding.deleteBox.setVisibility(View.GONE);
        }

    }

    public void setIsLongClick(boolean value) {
        isLongClick = value;
    }

    public boolean getIsLongClick() {
        return isLongClick;
    }
}