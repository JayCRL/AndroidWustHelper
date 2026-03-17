package com.example.wusthelper.adapter;

import static com.xuexiang.xutil.XUtil.getResources;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.wusthelper.R;
import com.example.wusthelper.bean.javabean.CollegeBean;
import com.example.wusthelper.bean.javabean.SearchBookBean;
import com.example.wusthelper.bean.javabean.SearchCourseBean;

import java.util.List;

public class SearchCollegeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<CollegeBean> collegeBeans;
    private Context mContext;
    private OnItemClickListener mOnItemClickListener;

    public SearchCollegeAdapter(Context context, List<CollegeBean> collegeBeans) {
        this.collegeBeans = collegeBeans;
        mContext = context;
    }

    public interface OnItemClickListener {

        void onItemClick(View view, int position);

    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {

        mOnItemClickListener = onItemClickListener;

    }

    public class SearchCollegeViewHolder extends RecyclerView.ViewHolder {

        private TextView courseTextView;
        private TextView countTextView;
        private View view;

        public SearchCollegeViewHolder(View itemView) {
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
        return new SearchCollegeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        SearchCollegeViewHolder searchCollegeViewHolder = (SearchCollegeViewHolder)holder;
        searchCollegeViewHolder.courseTextView.setText(collegeBeans.get(position).getCollegeName());
        searchCollegeViewHolder.countTextView.setText(collegeBeans.get(position).getCourseTotal());
        searchCollegeViewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnItemClickListener.onItemClick(view, position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return collegeBeans.size();
    }

}
