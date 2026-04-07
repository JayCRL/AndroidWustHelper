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
import com.example.wusthelper.bean.javabean.CollectionBookBean;
import com.example.wusthelper.ui.activity.BookDetailActivity;

import java.util.List;

public class MyLibraryCollectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<CollectionBookBean> mCollectionBookBeanList;
    private Context mContext;

    public MyLibraryCollectionAdapter(List<CollectionBookBean> collectionBookBeanList, Context context) {
        mCollectionBookBeanList = collectionBookBeanList;
        mContext = context;
    }

    public class LibraryColletionViewHolder extends RecyclerView.ViewHolder{

        private View view;
        private TextView bookNameTextView;
        private TextView authorTextView;
        private TextView pressTextView;
        private TextView bookNumTextView;
//        private RatingBar gradeRatingBar;
//        private TextView gradeTextView;

        public LibraryColletionViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            bookNameTextView = (TextView)itemView.findViewById(R.id.tv_book_name);
            authorTextView = (TextView)itemView.findViewById(R.id.tv_author);
            pressTextView = (TextView)itemView.findViewById(R.id.tv_press);
//            gradeRatingBar = (RatingBar)itemView.findViewById(R.id.rb_grade);
//            gradeTextView = (TextView)itemView.findViewById(R.id.tv_grade);
        }

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_book, parent, false);
        return new LibraryColletionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
            LibraryColletionViewHolder libraryColletionViewHolder = (LibraryColletionViewHolder)holder;
            libraryColletionViewHolder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = BookDetailActivity.newInstance(mContext, mCollectionBookBeanList.get(position).getUrl(), mCollectionBookBeanList.get(position).getBookName(),mCollectionBookBeanList.get(position).getAuthor());
                    mContext.startActivity(intent);
                }
            });
            libraryColletionViewHolder.bookNameTextView.setText(mCollectionBookBeanList.get(position).getBookName());
            libraryColletionViewHolder.authorTextView.setText(mCollectionBookBeanList.get(position).getAuthor());
            libraryColletionViewHolder.pressTextView.setText(mCollectionBookBeanList.get(position).getPress());
//            libraryColletionViewHolder.gradeRatingBar.setRating(mCollectionBookBeanList.get(position).getGrade());
//            libraryColletionViewHolder.gradeTextView.setText("" + mCollectionBookBeanList.get(position).getGrade());

    }

    @Override
    public int getItemCount() {
        return mCollectionBookBeanList.size();
    }

}
