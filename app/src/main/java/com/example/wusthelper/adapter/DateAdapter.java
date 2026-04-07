package com.example.wusthelper.adapter;

import android.util.Log;

import com.example.wusthelper.R;
import com.example.wusthelper.databinding.ItemCoursepageDateBinding;
import com.example.wusthelper.bean.itembean.DateItemForShow;
import com.example.wusthelper.utils.ResourcesUtils;


import org.jetbrains.annotations.NotNull;

public class DateAdapter extends BaseBindingQuickAdapter<DateItemForShow, ItemCoursepageDateBinding> {

    private static final String TAG = "DateAdapter";

    @Override
    protected void convert(@NotNull BaseBindingHolder holder, DateItemForShow item) {
        ItemCoursepageDateBinding binding = holder.getViewBinding();
        //禁用该item的复用
        holder.setIsRecyclable(false);
        binding.tvWeek.setText(item.weekday);
        binding.tvWeekDate.setText(item.date);

        if(item.getIsDay()){
            Log.d(TAG, "onBindViewHolder: "+item);
            //如果是当天，做特殊的显示处理
            binding.tvWeek.setTextColor(ResourcesUtils.getRealColor(R.color.white));
            binding.tvWeekDate.setTextColor(ResourcesUtils.getRealColor(R.color.white));
            binding.llItemCoursePageDate.setBackgroundColor(ResourcesUtils.getRealColor(R.color.colorClassTodayTitle));
        }
    }

}
