package com.example.wusthelper.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.nfc.Tag;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wusthelper.R;
import com.example.wusthelper.bean.itembean.BusItemForShow;
import com.example.wusthelper.databinding.ItemSchoolBusTimeBinding;
import com.example.wusthelper.helper.TimeTools;

import java.util.Date;
import java.util.List;

/**
 * @author: Gong Yunhao
 * @version: V1.0
 * @date: 2018/10/22
 * @github https://github.com/Roman-Gong
 * @blog https://www.jianshu.com/u/52a8fa1f29fb
 */
public class SchoolBusAdapter extends BaseBindingQuickAdapter<BusItemForShow, ItemSchoolBusTimeBinding> {

    private static final String TAG = "SchoolBusAdapter";
    @Override
    protected void convert(@NonNull BaseBindingHolder holder, BusItemForShow item) {
        Context mContext = holder.itemView.getContext();
        int currentHour = Integer.parseInt(TimeTools.getFormatHours());
        int currentMinute = Integer.parseInt(TimeTools.getFormatMinutes());
        ItemSchoolBusTimeBinding binding = holder.getViewBinding();
        binding.tvItemHour.setText(String.valueOf(item.getHour()));
        if (item.getMinute() == 0) {
            binding.tvItemMinute.setText("00");
        } else {
            binding.tvItemMinute.setText(String.valueOf(item.getMinute()));
        }

        int gapTime = (item.getHour() - currentHour) * 60 + item.getMinute() - currentMinute;
        Log.d("Schooladapter", "------->" + gapTime);
        Log.e(TAG,"gapTime="+gapTime);
        Log.e(TAG,"gapHour="+item.getHour());
        Log.e(TAG,"currentHour="+currentHour);

        if (item.getHour() < currentHour) {
            binding.tvBusItemTag.setText("已发车");
            binding.tvBusItemTag.setTextColor(mContext.getResources().getColor(R.color.colorGray));
            binding.viewItemBusLine.setBackgroundColor(mContext.getResources().getColor(R.color.colorGray));
            binding.viewItemBusCycle.setBackground(mContext.getResources().getDrawable(R.drawable.shape_gray_cycle));
        } else if (item.getHour() == currentHour && item.getMinute() <= currentMinute) {
            binding.tvBusItemTag.setText("已发车");
            binding.tvBusItemTag.setTextColor(mContext.getResources().getColor(R.color.colorGray));
            binding.viewItemBusLine.setBackgroundColor(mContext.getResources().getColor(R.color.colorGray));
            binding.viewItemBusCycle.setBackground(mContext.getResources().getDrawable(R.drawable.shape_gray_cycle));
        } else if (item.getHour() == currentHour && item.getMinute() > currentMinute) {
            binding.tvBusItemTag.setText("候车");
            binding.tvBusItemTag.setTextColor(mContext.getResources().getColor(R.color.color_yellow_orange));
            binding.viewItemBusLine.setBackgroundColor(mContext.getResources().getColor(R.color.color_yellow_orange));
            binding.viewItemBusCycle.setBackground(mContext.getResources().getDrawable(R.drawable.shape_orange_cycle));
        } else if (item.getHour() > currentHour) {
            binding.tvBusItemTag.setText("候车");
            binding.tvBusItemTag.setTextColor(mContext.getResources().getColor(R.color.color_yellow_orange));
            binding.viewItemBusLine.setBackgroundColor(mContext.getResources().getColor(R.color.color_yellow_orange));
            binding.viewItemBusCycle.setBackground(mContext.getResources().getDrawable(R.drawable.shape_orange_cycle));
        }

        if (gapTime > 0 && gapTime < 26) {
            binding.tvBusItemTag.setText("即将出发");
            binding.tvBusItemTag.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
            binding.viewItemBusLine.setBackgroundColor(mContext.getResources().getColor(R.color.colorPrimary));
            binding.viewItemBusCycle.setBackground(mContext.getResources().getDrawable(R.drawable.shape_green_cycle));
        }
    }
}
