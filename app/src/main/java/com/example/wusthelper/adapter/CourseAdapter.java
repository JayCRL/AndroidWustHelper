package com.example.wusthelper.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;

import com.example.wusthelper.R;
import com.example.wusthelper.MyApplication;
import com.example.wusthelper.databinding.ItemCoursepageCourseBinding;
import com.example.wusthelper.helper.DrawableLab;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.helper.VibrateLab;
import com.example.wusthelper.bean.itembean.CourseListForShow;
import com.example.wusthelper.bean.javabean.CourseBean;
import com.example.wusthelper.ui.activity.EditActivity;
import com.example.wusthelper.ui.dialog.CourseDialogNew;

import org.jetbrains.annotations.NotNull;

public class CourseAdapter extends BaseBindingQuickAdapter<CourseListForShow, ItemCoursepageCourseBinding> {

    private static final String TAG = "CourseAdapter";

    @Override
    protected void convert(@NotNull BaseBindingHolder holder, CourseListForShow item) {

        holder.setIsRecyclable(false);

        ItemCoursepageCourseBinding binding = holder.getViewBinding();
      //  Log.d(TAG, "convert: "+holder.getAdapterPosition());

//        Animation animation = AnimationUtils.loadAnimation(binding.getRoot().getContext(),R.anim.my_anim);
//        holder.itemView.setAnimation(animation);

        if (item.type!=CourseListForShow.EMPTY){
            //显示课程表
            binding.rvCourse.setVisibility(View.VISIBLE);

            //课程数据加载
            CourseBean courseBean = item.itemList.get(0);
            binding.tvCourseClassname.setText(courseBean.getCourseName());
            binding.tvCourseClassroom.setText(courseBean.getClassRoom());
            binding.tvCourseClassname.setTextSize(SharePreferenceLab.getFontSize());
            binding.tvCourseClassroom.setTextSize(SharePreferenceLab.getFontSize());
            if (SharePreferenceLab.getIsItalic()){
                binding.tvCourseClassname.setTypeface(null, Typeface.ITALIC);
                binding.tvCourseClassroom.setTypeface(null, Typeface.ITALIC);
            }

            if(item.type==CourseListForShow.REPEAT){
                //有两节课在同一个时间，做特殊显示(右下角下三角)
                binding.ivMultipleClass.setVisibility(View.VISIBLE);
            }

            //这周有课
            if(courseBean.isInClass()){
                //字体设置白色，背景颜色按照预设的ID进行设置
                binding.tvCourseClassroom.setTextColor(Color.parseColor("#ffffff"));
                binding.tvCourseClassname.setTextColor(Color.parseColor("#ffffff"));
                binding.rvCourse.setBackgroundResource(DrawableLab.getResourceID
                        (courseBean.getColor()));
            }else{
                //字体设置灰色，背景设置深灰色
                binding.tvCourseClassroom.setTextColor(Color.parseColor("#91969B"));
                binding.tvCourseClassname.setTextColor(Color.parseColor("#91969B"));
                binding.rvCourse.setBackgroundResource(R.drawable.layer_color_class_no_class);
            }
            //设置长按添加课程逻辑
            binding.rvCourse.setOnLongClickListener(v -> {
                //震动
                VibrateLab.vibrate(50);
                String semester = SharePreferenceLab.getSelectSemester();
                Intent intent = EditActivity.newInstance(binding.getRoot().getContext(),
                        getWeekDay(holder.getLayoutPosition()),
                        getStartTime(holder.getLayoutPosition()),
                        semester,0
                );
                binding.getRoot().getContext().startActivity(intent);
                return false;
            });
            binding.rvCourse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e(TAG,"item.itemList + " + item.itemList);
                    new CourseDialogNew.Builder(binding.getRoot().getContext())
                            .setList(item.itemList)
                            .create();
//                    Intent intent = EditActivity.newInstance(ActivityManager.getActivityManager().currentActivity(),
//                            courseBean,1
//                    );
//                    ActivityManager.getActivityManager().currentActivity().startActivity(intent);
                }
            });

        }else{
            //设置长按添加课程逻辑
            binding.ivCourseItemAdd.setOnLongClickListener(v -> {
                //震动
                VibrateLab.vibrate(50);
                String semester = SharePreferenceLab.getSelectSemester();
                Intent intent = EditActivity.newInstance(binding.getRoot().getContext(),
                        getWeekDay(holder.getLayoutPosition()),
                        getStartTime(holder.getLayoutPosition()),
                        semester,0
                );
                binding.getRoot().getContext().startActivity(intent);
                Log.e(TAG,"holder.getLayoutPosition() = "+holder.getLayoutPosition());

                return false;
            });

        }
    }

    private int getWeekDay(int position) {
        if(SharePreferenceLab.getIsChooseSundayFirst()) {
            if(position%7 == 0) return 7;
            return position%7;
        }else {
            if(position == 0) return 1;
            //如果position为0不能进行取余运算
            return position%7+1;
        }
    }

    private int getStartTime(int position) {
        return position/7+1;
    }

}
