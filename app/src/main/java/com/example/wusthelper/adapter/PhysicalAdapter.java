package com.example.wusthelper.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wusthelper.R;
import com.example.wusthelper.bean.javabean.CourseBean;

import java.util.List;

public class PhysicalAdapter extends RecyclerView.Adapter<PhysicalAdapter.PhysicalViewHolder>{

    private static final String TAG = "PhysicalAdapter";
    private List<CourseBean> mDataBeanList;
    private Context mContext;
    private OnItemClickListener mOnItemClickListener;


    public PhysicalAdapter(List<CourseBean> DataBeanList, Context context) {
        mDataBeanList = DataBeanList;
        mContext = context;
    }

    public interface OnItemClickListener{
        void onClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public int getItemCount() {
        return mDataBeanList.size();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull PhysicalViewHolder holder, final int position) {
        CourseBean courseBean = mDataBeanList.get(position);
        holder.className.setText(courseBean.getCourseName()+"");
        holder.startWeek.setText("第"+courseBean.getStartWeek()+"周");
        holder.weekDay.setText("星期"+courseBean.getWeekday());
        int classSection = courseBean.getStartTime()*2-1;
        holder.section.setText(classSection+""+(classSection+1)+"节课");
        holder.classroom.setText(courseBean.getClassRoom()+"");
        holder.teacher.setText(courseBean.getTeacherName()+"");
    }

    public class PhysicalViewHolder extends RecyclerView.ViewHolder{

        private View mItemView;
        private TextView className;
        private TextView startWeek;
        private TextView weekDay;
        private TextView section;
        private TextView classroom;
        private TextView teacher;


        public PhysicalViewHolder(View itemView) {
            super(itemView);
            className   = itemView.findViewById(R.id.item_physical_className);
            startWeek   = itemView.findViewById(R.id.item_physical_startWeek);
            weekDay     = itemView.findViewById(R.id.item_physical_weekDay);
            section     = itemView.findViewById(R.id.item_physical_section);
            classroom   = itemView.findViewById(R.id.item_physical_classroom);
            teacher     = itemView.findViewById(R.id.item_physical_teacher);
            mItemView = itemView;
        }
    }

    @NonNull
    @Override
    public PhysicalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_physical_course, parent, false);
        return new PhysicalViewHolder(view);
    }

}
