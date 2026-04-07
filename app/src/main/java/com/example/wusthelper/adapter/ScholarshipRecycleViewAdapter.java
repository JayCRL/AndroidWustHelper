package com.example.wusthelper.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import com.example.wusthelper.bean.javabean.ScholarshipBean;
import com.example.wusthelper.databinding.ItemScholarshipBinding;
import com.example.wusthelper.ui.activity.ScholarshipActivity;
import com.example.wusthelper.utils.ColorUtil;
import com.example.wusthelper.utils.DrawableUtil;
import com.example.wusthelper.utils.NumDisposeUtil;

import java.util.ArrayList;
import java.util.List;

public class ScholarshipRecycleViewAdapter extends BaseBindingQuickAdapter<ScholarshipBean, ItemScholarshipBinding> {


    private   List<Integer> colorList = new ArrayList<>();

    private static final String TAG = "ScholarshipRecycleViewA";

    @Override
    protected void convert(@NonNull BaseBindingHolder holder, ScholarshipBean item) {
        //100个    应该没有人一学年学100门课吧！！！！
        for(int i =0;i<=100;i++){
            colorList.add(ColorUtil.getRandomColor());
        }

        Context mContext = holder.itemView.getContext();
        ItemScholarshipBinding binding = holder.getViewBinding();

        final ScholarshipActivity scholarshipActivity = (ScholarshipActivity) mContext;
        binding.itemScholarshipCredit.setText("学分："+item.getCredit());
        binding.scholarshipItemText.setText(NumDisposeUtil.doubleToString(item.getWeight()));
        binding.itemScholarshipGrade.setText("绩点："+item.getGpa());
        binding.itemScholarshipName.setText(item.getCourseName());
        binding.scholarshipShowText.setText(item.getCourseName().substring(0,1));
        Log.e(TAG, "onBindViewHolder:宽和高===== "+holder.itemView.getHeight());
//            holder.showText.setBackgroundColor(colorList.get(position));
        binding.scholarshipShowTextBackground.setImageBitmap(DrawableUtil.createTextImage(colorList.get(holder.getLayoutPosition())));
        binding.scholarshipItemAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(item.getWeight()<3.0) {
                    item.setWeight(Double.parseDouble(binding.scholarshipItemText.getText().toString())+0.1);
                    binding.scholarshipItemText.setText(NumDisposeUtil.doubleToString(item.getWeight()));
                    scholarshipActivity.getPresenter().isChanged = true;
                    scholarshipActivity.getPresenter().calculateScholarship();
                }
            }
        });

        binding.scholarshipItemReduce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(item.getWeight()>0.4) {
                    item.setWeight(Double.parseDouble(binding.scholarshipItemText.getText().toString())-0.1);
                    binding.scholarshipItemText.setText(NumDisposeUtil.doubleToString(item.getWeight()));
                    scholarshipActivity.getPresenter().isChanged = true;
                    scholarshipActivity.getPresenter().calculateScholarship();
                }
            }
        });

    }
}