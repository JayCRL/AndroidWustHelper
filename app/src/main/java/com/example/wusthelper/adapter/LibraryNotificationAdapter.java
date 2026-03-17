package com.example.wusthelper.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wusthelper.R;
import com.example.wusthelper.bean.javabean.AnnouncementBean;
import com.example.wusthelper.ui.activity.LibraryNotificationActivity;

import java.util.ArrayList;
import java.util.List;

public class LibraryNotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<AnnouncementBean> announcementBeanList = new ArrayList<>();
    private Context mContext;

    private int normalType = 0;     // 第一种ViewType，正常的item
    private int footType = 1;       // 第二种ViewType，底部的提示View

    private boolean hasMore = true;   // 变量，是否有更多数据
    private boolean fadeTips = false; // 变量，是否隐藏了底部的提示


    // 正常item的ViewHolder，用以缓存findView操作


    // // 底部footView的ViewHolder，用以缓存findView操作
    class FootHolder extends RecyclerView.ViewHolder {
        private TextView tips;

        public FootHolder(View itemView) {
            super(itemView);
        }
    }


    // 获取条目数量，之所以要加1是因为增加了一条footView
    @Override
    public int getItemCount() {
        return announcementBeanList.size() + 1;
    }

    // 自定义方法，获取列表中数据源的最后一个位置，比getItemCount少1，因为不计上footView
    public int getRealLastPosition() {
        return announcementBeanList.size();
    }


    // 根据条目位置返回ViewType，以供onCreateViewHolder方法内获取不同的Holder
    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) {
            return footType;
        } else {
            return normalType;
        }
    }


    public LibraryNotificationAdapter(List<AnnouncementBean> announcementBeanList, Context context) {
        this.announcementBeanList = announcementBeanList;
        mContext = context;
    }

    public class NormalHolder extends RecyclerView.ViewHolder {

        private TextView libraryNotificationContentTextView;
        private TextView libraryNotificationDateTextView;
        private View view;

        public NormalHolder(View itemView) {
            super(itemView);
            view = itemView;
            libraryNotificationContentTextView = (TextView)itemView.findViewById(R.id.tv_library_notification_content);
            libraryNotificationDateTextView = (TextView)itemView.findViewById(R.id._tv_library_notification_date);
        }

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == normalType) {
            final View view =LayoutInflater.from(mContext).inflate(R.layout.item_library_notification, null);
            final NormalHolder normalHolder=new NormalHolder(view);
            return normalHolder;
        } else {
            return new FootHolder(LayoutInflater.from(mContext).inflate(R.layout.recyclerview_footview, null));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        // 如果是正常的imte，直接设置TextView的值
        if (holder instanceof NormalHolder) {
            NormalHolder libraryNotificationViewHolder = (NormalHolder)holder;
            libraryNotificationViewHolder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = LibraryNotificationActivity.newInstance(mContext, announcementBeanList.get(position).getAnnouncementId());
                    mContext.startActivity(intent);
                }
            });
            String libraryNotificationContent = announcementBeanList.get(position).getAnnouncementTitle();
            String libraryNotificationDate = announcementBeanList.get(position).getAnnouncementCreatetime();
            libraryNotificationViewHolder.libraryNotificationContentTextView.setText(libraryNotificationContent);
            libraryNotificationViewHolder.libraryNotificationDateTextView.setText(libraryNotificationDate);
        }

    }


    // 暴露接口，改变fadeTips的方法
    public boolean isFadeTips() {
        return fadeTips;
    }

    // 暴露接口，下拉刷新时，通过暴露方法将数据源置为空
    public void resetDatas() {
        announcementBeanList = new ArrayList<>();
    }

    // 暴露接口，更新数据源，并修改hasMore的值，如果有增加数据，hasMore为true，否则为false
    public void updateList(List<AnnouncementBean> newDataList, boolean hasMore) {
        // 在原有的数据之上增加新数据
        if (newDataList != null) {
            announcementBeanList.addAll(newDataList);
        }
        this.hasMore = hasMore;
        notifyDataSetChanged();
    }
}
