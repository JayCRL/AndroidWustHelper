package com.example.wusthelper.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.wusthelper.R;
import com.example.wusthelper.bean.javabean.LibraryHistoryBean;
import com.example.wusthelper.ui.activity.BookDetailActivity;

import java.util.List;

public class LibraryHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<LibraryHistoryBean> mLibraryHistoryBeanList;
    private Context mContext;

    public LibraryHistoryAdapter(List<LibraryHistoryBean> libraryHistoryBeanList, Context context) {
        mLibraryHistoryBeanList = libraryHistoryBeanList;
        mContext = context;
    }

    public class LibraryHistoryTitleViewHolder extends RecyclerView.ViewHolder {

        private ImageView titleSignImageView;
        private TextView titleNameTextView;
        private TextView numTextView;

        public LibraryHistoryTitleViewHolder(View itemView) {
            super(itemView);
            titleSignImageView = (ImageView)itemView.findViewById(R.id.iv_book_title_sign);
            titleNameTextView = (TextView)itemView.findViewById(R.id.tv_book_title_name);
            numTextView = (TextView)itemView.findViewById(R.id.tv_book_num);
        }

    }

    public class LibraryHistoryContentViewHolder extends RecyclerView.ViewHolder {

        private View view;
        private TextView callNumTextView;
        private TextView authorTextView;
        private TextView borrowDateTextView;
        private TextView returnDateTextView;
        private TextView returnPlaceTextView;
        private Button functionButton;

        public LibraryHistoryContentViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            callNumTextView = (TextView)itemView.findViewById(R.id.tv_book_call_num);
            authorTextView = (TextView)itemView.findViewById(R.id.tv_book_author);
            borrowDateTextView = (TextView)itemView.findViewById(R.id.tv_book_borrow_date);
            returnDateTextView = (TextView)itemView.findViewById(R.id.tv_book_return_date);
            returnPlaceTextView = (TextView)itemView.findViewById(R.id.tv_book_return_place);
            functionButton = (Button)itemView.findViewById(R.id.btn_book_function);
        }

    }

    @Override
    public int getItemViewType(int position) {

        if (mLibraryHistoryBeanList.get(position).getType() == LibraryHistoryBean.TITLE)
            return 0;
        else
            return 1;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == 0) {

            View view = LayoutInflater.from(mContext).inflate(R.layout.item_book_borrow_title, parent, false);
            LibraryHistoryTitleViewHolder libraryHistoryTitleViewHolder = new LibraryHistoryTitleViewHolder(view);
            return libraryHistoryTitleViewHolder;

        } else {

            View view = LayoutInflater.from(mContext).inflate(R.layout.item_book_borrow, parent, false);
            LibraryHistoryContentViewHolder libraryHistoryContentViewHolder = new LibraryHistoryContentViewHolder(view);
            return libraryHistoryContentViewHolder;

        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        if (holder instanceof LibraryHistoryTitleViewHolder) {

            LibraryHistoryTitleViewHolder libraryHistoryTitleViewHolder = (LibraryHistoryTitleViewHolder)holder;
            libraryHistoryTitleViewHolder.titleSignImageView.setImageDrawable(mLibraryHistoryBeanList.get(position).getTitleSign());
            libraryHistoryTitleViewHolder.titleNameTextView.setText(mLibraryHistoryBeanList.get(position).getTitleName());
            libraryHistoryTitleViewHolder.numTextView.setText("" + mLibraryHistoryBeanList.get(position).getNum());
            if (mLibraryHistoryBeanList.get(position).getTitle() == LibraryHistoryBean.BEYOND_TIME) {
                libraryHistoryTitleViewHolder.numTextView.setBackground(mContext.getDrawable(R.drawable.shape_beyond));
            } else if (mLibraryHistoryBeanList.get(position).getTitle() == LibraryHistoryBean.BORROWING) {
                libraryHistoryTitleViewHolder.numTextView.setBackground(mContext.getDrawable(R.drawable.shape_borrowing));
            } else {
                libraryHistoryTitleViewHolder.numTextView.setBackground(mContext.getDrawable(R.drawable.shape_history_book));
            }

        } else {

            LibraryHistoryContentViewHolder libraryHistoryContentViewHolder = (LibraryHistoryContentViewHolder)holder;
            libraryHistoryContentViewHolder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String[] strs = mLibraryHistoryBeanList.get(position).getAuthor().split("/");
                    String author;
                    String title = strs[0];
                    if (strs.length < 2) {
                        author = "无法获取";
                    } else {
                        author = strs[1];
                    }
                    Intent intent = BookDetailActivity.newInstance(mContext, mLibraryHistoryBeanList.get(position).getHref(),title,author);
                    mContext.startActivity(intent);
                }
            });
            libraryHistoryContentViewHolder.callNumTextView.setText(mLibraryHistoryBeanList.get(position).getCallNumber());
            libraryHistoryContentViewHolder.authorTextView.setText(mLibraryHistoryBeanList.get(position).getAuthor());
            libraryHistoryContentViewHolder.borrowDateTextView.setText(mLibraryHistoryBeanList.get(position).getBorrowDate());
            libraryHistoryContentViewHolder.returnDateTextView.setText(mLibraryHistoryBeanList.get(position).getReturnDate());
            libraryHistoryContentViewHolder.returnPlaceTextView.setText(mLibraryHistoryBeanList.get(position).getReturnPlace());
            libraryHistoryContentViewHolder.functionButton.setText(mLibraryHistoryBeanList.get(position).getFunction());
            if (mLibraryHistoryBeanList.get(position).getTitle() == LibraryHistoryBean.BEYOND_TIME) {
                libraryHistoryContentViewHolder.functionButton.setVisibility(View.GONE);
            } else if (mLibraryHistoryBeanList.get(position).getTitle() == LibraryHistoryBean.BORROWING) {
                libraryHistoryContentViewHolder.functionButton.setBackground(mContext.getDrawable(R.drawable.shape_borrowing));
            } else {
                libraryHistoryContentViewHolder.functionButton.setVisibility(View.GONE);
            }
        }

    }

    @Override
    public int getItemCount() {
        return mLibraryHistoryBeanList.size();
    }

}
