package com.example.wusthelper.adapter;

import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;

import com.example.wusthelper.bean.javabean.ScholarshipBean;
import com.example.wusthelper.databinding.ItemChooseShcolarshipBinding;
import com.example.wusthelper.ui.activity.ScholarShipChooseActivity;


public class ScholarshipChooseAdapter extends BaseBindingQuickAdapter<ScholarshipBean, ItemChooseShcolarshipBinding> {


    @Override
    protected void convert(@NonNull BaseBindingHolder holder, ScholarshipBean item) {
        Context mContext = holder.itemView.getContext();
        ItemChooseShcolarshipBinding binding = holder.getViewBinding();

        final ScholarShipChooseActivity scholarShipChooseActivity = (ScholarShipChooseActivity) mContext;
        binding.checkBox.setChecked(item.isChecked());
        binding.checkBox.setTextDirection(android.view.View.TEXT_DIRECTION_LTR);
        binding.checkBox.setText(item.getCourseName());
        binding.gradeText.setText("绩点："+item.getGpa());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scholarShipChooseActivity.getPresenter().isChanged = true;
                item.setChecked(!item.isChecked());
                binding.checkBox.setChecked(item.isChecked());
                if(item.isChecked()){
                    holder.itemView.setAlpha(0.8f);
                }else{
                    holder.itemView.setAlpha(1.0f);
                }
                item.setWeight(1.0);
            }
        });
    }
}