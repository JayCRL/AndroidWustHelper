/*
 * @author yuandalai
 * @date 2018/11/14
 * @email yuanlai0611@gmail.com
 * @github https://github.com/yuanlai0611
 * @blog https://yuanlai0611.github.io/
 */

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
import com.example.wusthelper.bean.javabean.SearchBookBean;

import java.util.List;

public class SearchBookAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<SearchBookBean> searchBookBeanList;
    private Context mContext;
    private OnItemClickListener mOnItemClickListener;

    public SearchBookAdapter(Context context, List<SearchBookBean> searchBookBeanList) {
        this.searchBookBeanList = searchBookBeanList;
        mContext = context;
    }

    public interface OnItemClickListener {

        void onItemClick(View view, int position);

    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {

        mOnItemClickListener = onItemClickListener;

    }

    public class SearchBookViewHolder extends RecyclerView.ViewHolder {

        private TextView bookNameTextView;
        private TextView authorTextView;
        private TextView pressTextView;
        private TextView sumTextView;
        private TextView borrowableNumTextView;
        private View view;
        private ImageView imageView;

        public SearchBookViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            imageView = (ImageView)itemView.findViewById(R.id.iv_book_cover);
            bookNameTextView = (TextView)itemView.findViewById(R.id.tv_book_name);
            authorTextView = (TextView)itemView.findViewById(R.id.tv_author);
            pressTextView = (TextView)itemView.findViewById(R.id.tv_press);
            sumTextView = (TextView)itemView.findViewById(R.id.tv_book_sum);
            borrowableNumTextView = (TextView)itemView.findViewById(R.id.tv_book_borrowable_num);
        }

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_search_book, parent, false);
        return new SearchBookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        SearchBookViewHolder searchBookViewHolder = (SearchBookViewHolder)holder;
        if(!searchBookBeanList.get(position).getImageUrl().equals("")) {
            Glide.with(mContext)
                    .load(searchBookBeanList.get(position).getImageUrl())
                    .into(searchBookViewHolder.imageView);
        }else {
            Glide.with(mContext)
                    .load(getResources().getDrawable(R.drawable.book_default))
                    .into(searchBookViewHolder.imageView);
        }
        searchBookViewHolder.bookNameTextView.setText(searchBookBeanList.get(position).getBookName());
        searchBookViewHolder.authorTextView.setText(searchBookBeanList.get(position).getAuthor());
        searchBookViewHolder.pressTextView.setText(searchBookBeanList.get(position).getPress());
        searchBookViewHolder.sumTextView.setText(searchBookBeanList.get(position).getSum());
        searchBookViewHolder.borrowableNumTextView.setText(searchBookBeanList.get(position).getBorrowableNum());
        searchBookViewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnItemClickListener.onItemClick(view, position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return searchBookBeanList.size();
    }

}
