package com.example.wusthelper.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.example.wusthelper.R;
import com.example.wusthelper.bean.javabean.GradeBean;
import com.example.wusthelper.bean.javabean.GraduateGradeBean;
import com.example.wusthelper.databinding.ItemGradeActivityBinding;
import com.example.wusthelper.ui.dialog.GradeDialogNew;

import org.jetbrains.annotations.NotNull;

/**
 * 显示研究生成绩
 */
public class GraduateGradeAdapter extends BaseBindingQuickAdapter<GraduateGradeBean, ItemGradeActivityBinding> {

    private static final String TAG = "DateAdapter";

    @Override
    protected void convert(@NotNull BaseBindingHolder holder, GraduateGradeBean item) {
        //  Log.d(TAG, "convert: "+holder.getAdapterPosition());

        Context mContext = holder.itemView.getContext();
        ItemGradeActivityBinding binding = holder.getViewBinding();

        binding.courseName.setText(item.getName());
        binding.grade.setText(item.getPoint());
        binding.courseCredit.setText(item.getCredit());
        binding.gradePointText.setText("学期: ");
        binding.gradePoint.setText(item.getTerm());


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        if (89 < Double.parseDouble(item.getPoint())) {
            //view_yellow_view.setBackground(mContext.getResources().getDrawable(R.drawable.shape_perfect_grade_yellow));
            binding.ivGradeItemGood.setVisibility(View.VISIBLE);
            binding.gradePoint.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
            binding.grade.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
            binding.courseCredit.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
        } else if (Double.parseDouble(item.getPoint()) < 60){
            //view_yellow_view.setBackground(mContext.getResources().getDrawable(R.drawable.shape_bad_grade_red));
            binding.ivGradeItemGood.setVisibility(View.GONE);
            binding.gradePoint.setTextColor(mContext.getResources().getColor(R.color.red_bad_grade_text));
            binding.grade.setTextColor(mContext.getResources().getColor(R.color.red_bad_grade_text));
            binding.courseCredit.setTextColor(mContext.getResources().getColor(R.color.red_bad_grade_text));
        } else {
            //view_yellow_view.setBackground(mContext.getResources().getDrawable(R.drawable.shape_normal_grade_transparent));
            binding.ivGradeItemGood.setVisibility(View.GONE);
            binding.gradePoint.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
            binding.grade.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
            binding.courseCredit.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
        }

    }

}
