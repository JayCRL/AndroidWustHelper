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
import com.example.wusthelper.bean.javabean.CollegeBean;
import com.example.wusthelper.bean.javabean.CourseNameBean;

import java.util.List;

public class CourseListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<CourseNameBean> courseNameBeans;
    private Context mContext;
    private OnItemClickListener mOnItemClickListener;

    public CourseListAdapter(Context context, List<CourseNameBean> courseNameBeans) {
        this.courseNameBeans = courseNameBeans;
        mContext = context;
    }

    public interface OnItemClickListener {

        void onItemClick(View view, int position);

    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {

        mOnItemClickListener = onItemClickListener;

    }

    public class CourseListViewHolder extends RecyclerView.ViewHolder {

        private TextView courseTextView;
        private TextView countTextView;
        private View view;

        public CourseListViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            countTextView = (TextView) itemView.findViewById(R.id.count);
            courseTextView = (TextView) itemView.findViewById(R.id.course);
        }

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_search_course, parent, false);
        return new CourseListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        CourseListViewHolder courseListViewHolder = (CourseListViewHolder)holder;
        courseListViewHolder.courseTextView.setText(courseNameBeans.get(position).getCourseName());
        courseListViewHolder.countTextView.setVisibility(View.GONE);
        courseListViewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnItemClickListener.onItemClick(view, position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return courseNameBeans.size();
    }

}
