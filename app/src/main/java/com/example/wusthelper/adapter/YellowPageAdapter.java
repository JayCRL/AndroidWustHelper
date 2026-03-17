package com.example.wusthelper.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wusthelper.R;
import com.example.wusthelper.bean.javabean.YellowPageData;
import com.example.wusthelper.databinding.ItemParentYellowPageBinding;

import java.util.List;

public class YellowPageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private Context mContext;
    private List<YellowPageData> mDataBeanList;
    private final String TAG = "YellowPageAdapter";

    public YellowPageAdapter(Context context, List<YellowPageData> dataBeanList) {
        mContext = context;
        mDataBeanList = dataBeanList;
    }

    public class ParentViewHolder extends RecyclerView.ViewHolder {

        private ImageView parentIconImageView;
        private TextView parentTitleTextView;
        private ImageView expandImageView;
        private View parentView;

        public ParentViewHolder(View itemView) {
            super(itemView);
            expandImageView = (ImageView)itemView.findViewById(R.id.iv_expand);
            parentIconImageView = (ImageView)itemView.findViewById(R.id.iv_parent_icon);
            parentTitleTextView = (TextView)itemView.findViewById(R.id.tv_parent_title);
            parentView = itemView;
        }

    }

    public class ChildViewHolder extends RecyclerView.ViewHolder {

        private TextView departNameTextView;
        private TextView telephoneTextView;
        private View childView;

        public ChildViewHolder(View itemView) {
            super(itemView);
            departNameTextView = (TextView)itemView.findViewById(R.id.tv_department_name);
            telephoneTextView = (TextView)itemView.findViewById(R.id.tv_telephone);
            childView = itemView;
        }

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == YellowPageData.TYPE_PARENT) {
            return new ParentViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_parent_yellow_page, parent, false));
        } else {
            return new ChildViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_child_yellow_page, parent, false));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mDataBeanList.get(position).getType() == YellowPageData.TYPE_PARENT)
            return YellowPageData.TYPE_PARENT;
        else
            return YellowPageData.TYPE_CHILD;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        if (holder instanceof ParentViewHolder) {

            final ParentViewHolder parentViewHolder = (ParentViewHolder)holder;
            parentViewHolder.parentIconImageView.setImageDrawable(mDataBeanList.get(position).getParentIcon());
            parentViewHolder.parentTitleTextView.setText(mDataBeanList.get(position).getParentTitle());
            parentViewHolder.parentIconImageView.setBackground(mDataBeanList.get(position).getParentBackground());
            final YellowPageData dataBean = mDataBeanList.get(position);

            if (dataBean.isExpanded()) {
                parentViewHolder.expandImageView.setRotation(180);
            } else {
                parentViewHolder.expandImageView.setRotation(0);
            }

            parentViewHolder.parentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //Log.d(TAG, "点击了");
                    int position1 = getCurrentPosition(dataBean);
                    //Log.d(TAG, "" + position);
                    if (mDataBeanList.get(position1).isExpanded()) {
                        mDataBeanList.get(position1).setExpanded(false);
                        parentViewHolder.expandImageView.setRotation(0);
                        Log.d(TAG, "调用了setExpanded(false)");
                        removeItem(dataBean.getDataBeanList(), position1);
                    } else {
                        mDataBeanList.get(position1).setExpanded(true);
                        parentViewHolder.expandImageView.setRotation(180);
                        Log.d(TAG, "调用了setExpanded(true)");
                        addItem(dataBean.getDataBeanList(), position1);
                    }
                }
            });


        } else if (holder instanceof ChildViewHolder){

            ChildViewHolder childViewHolder = (ChildViewHolder)holder;
            childViewHolder.departNameTextView.setText(mDataBeanList.get(position).getDepartmentName());
            childViewHolder.telephoneTextView.setText(mDataBeanList.get(position).getTelephoneNumber());
            childViewHolder.childView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_DIAL,Uri.parse("tel:"+mDataBeanList.get(position).getTelephoneNumber()));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                    Log.d(TAG, "" + position);
                }
            });

        }

    }

    private int getCurrentPosition(final YellowPageData dataBean) {

        for (int i=0; i<mDataBeanList.size(); i++) {
            if (mDataBeanList.get(i).getId() == dataBean.getId())
                return i;
        }
        return -1;
    }

    private void addItem(List<YellowPageData> dataBeanList, int position) {

        mDataBeanList.addAll(position+1, dataBeanList);
        notifyItemRangeInserted(position+1, dataBeanList.size());

    }

    private void removeItem(List<YellowPageData> dataBeanList, int position) {

        int count = dataBeanList.size();
        while(count--!=0) {
            mDataBeanList.remove(position+1);
        }
        notifyItemRangeRemoved(position+1, dataBeanList.size());

    }

    @Override
    public int getItemCount() {
        return mDataBeanList.size();
    }

}
