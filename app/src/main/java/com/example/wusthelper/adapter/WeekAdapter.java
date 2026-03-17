package com.example.wusthelper.adapter;

import android.view.View;

import com.example.wusthelper.R;
import com.example.wusthelper.databinding.ItemCoursepageWeekBinding;
import com.example.wusthelper.bean.itembean.WeekItemForShow;

import org.jetbrains.annotations.NotNull;

import static com.example.wusthelper.utils.ResourcesUtils.getRealDrawable;

public class WeekAdapter extends BaseBindingQuickAdapter<WeekItemForShow, ItemCoursepageWeekBinding> {

    private static final String TAG = "WeekAdapter";
    
    @Override
    protected void convert(@NotNull BaseBindingHolder holder, WeekItemForShow item) {
        ItemCoursepageWeekBinding binding = holder.getViewBinding();

        holder.setIsRecyclable(false);
        String weekText = "第" + item.getWeek() + '周';
        binding.tvWeek.setText(weekText);
        binding.tabView.setColorPoint(item.list);

        if(item.isSelectWeek()){
            binding.llTabBackground.setBackground(getRealDrawable(R.drawable.shape_item_selected));
        }

        if(item.isRealWeek()){
            binding.tvThisWeek.setVisibility(View.VISIBLE);
        }
        
    }
}