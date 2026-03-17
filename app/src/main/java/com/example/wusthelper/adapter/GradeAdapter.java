package com.example.wusthelper.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.example.wusthelper.R;
import com.example.wusthelper.bean.javabean.GradeBean;
import com.example.wusthelper.databinding.ItemGradeActivityBinding;
import com.example.wusthelper.ui.dialog.CourseDialogNew;
import com.example.wusthelper.ui.dialog.GradeDialogNew;

import org.jetbrains.annotations.NotNull;

public class GradeAdapter extends BaseBindingQuickAdapter<GradeBean, ItemGradeActivityBinding> {

    private static final String TAG = "DateAdapter";

    @Override
    protected void convert(@NotNull BaseBindingHolder holder, GradeBean item) {
      //  Log.d(TAG, "convert: "+holder.getAdapterPosition());

        Context mContext = holder.itemView.getContext();
        ItemGradeActivityBinding binding = holder.getViewBinding();

        binding.courseName.setText(item.getCourseName());
        binding.grade.setText(item.getGrade());
        binding.courseCredit.setText(item.getCourseCredit()+"");
        binding.gradePoint.setText(item.getGradePoint()+"");


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GradeDialogNew.Builder(binding.getRoot().getContext())
                        .setData(item)
                        .create();
            }
        });



        if(item.getMissingTag()==0){

        }else if(item.getMissingTag()==1) {
            binding.tvGradeItemScoreTagNumber1.setVisibility(View.VISIBLE);
            binding.tvGradeItemScoreTagNumber1.setText("缺");
            binding.tvGradeItemScoreTagNumber1.setTextColor(mContext.getResources().getColor(R.color.color_grades_coreTag_rebuild));
        }else if(item.getMissingTag()==2){
            binding.tvGradeItemScoreTagNumber1.setVisibility(View.VISIBLE);
            binding.tvGradeItemScoreTagNumber1.setText("缓");
            binding.tvGradeItemScoreTagNumber1.setTextColor(mContext.getResources().getColor(R.color.color_grades_coreTag_missing));
        }else {
            binding.tvGradeItemScoreTagNumber1.setVisibility(View.GONE);
        }


        if(item.getReExam()==1){
            Log.d(TAG, "convert: "+item);
            binding.tvGradeItemScoreTagNumber2.setVisibility(View.VISIBLE);
            binding.tvGradeItemScoreTagNumber2.setText("补");
            binding.tvGradeItemScoreTagNumber2.setTextColor(mContext.getResources().getColor(R.color.color_grades_coreTag_rebuild));
        }else if (item.getRebuildTag()==1){
            binding.tvGradeItemScoreTagNumber2.setVisibility(View.VISIBLE);
            binding.tvGradeItemScoreTagNumber2.setText("重");
            binding.tvGradeItemScoreTagNumber2.setTextColor(mContext.getResources().getColor(R.color.color_grades_coreTag_resit));
        }else {
            binding.tvGradeItemScoreTagNumber2.setVisibility(View.GONE);
        }

        if (3.9 < Double.parseDouble(item.getGradePoint())) {
            //view_yellow_view.setBackground(mContext.getResources().getDrawable(R.drawable.shape_perfect_grade_yellow));
            binding.ivGradeItemGood.setVisibility(View.VISIBLE);
            binding.gradePoint.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
            binding.grade.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
            binding.courseCredit.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
        } else if (Double.parseDouble(item.getGradePoint()) < 0.5){
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

