package com.example.wusthelper.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wusthelper.R;
import com.example.wusthelper.bean.javabean.LibraryCollectionBean;

import java.util.List;

public class LibraryCollectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<LibraryCollectionBean> mCollectionBookBeanList;

    public LibraryCollectionAdapter(List<LibraryCollectionBean> collectionBookBeanList) {
        mCollectionBookBeanList = collectionBookBeanList;
    }

    public class LibraryCollectionViewHolder extends RecyclerView.ViewHolder{

        private View view;
        private TextView callNoTextView;
        private TextView barCodeTextView;
        private TextView locationTextView;
        private TextView statusTextView;


        public LibraryCollectionViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            callNoTextView = itemView.findViewById(R.id.tv_call_number);
            barCodeTextView = itemView.findViewById(R.id.tv_bar_code);
            locationTextView = itemView.findViewById(R.id.tv_library);
            statusTextView = itemView.findViewById(R.id.tv_borrowable);
        }

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_library_collection, parent, false);
        return new LibraryCollectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        LibraryCollectionViewHolder libraryCollectionViewHolder = (LibraryCollectionViewHolder)holder;
        libraryCollectionViewHolder.callNoTextView.setText(mCollectionBookBeanList.get(position).getCallNumber());
        libraryCollectionViewHolder.barCodeTextView.setText(mCollectionBookBeanList.get(position).getBarCode());
        libraryCollectionViewHolder.locationTextView.setText(mCollectionBookBeanList.get(position).getLocation());
        libraryCollectionViewHolder.statusTextView.setText(mCollectionBookBeanList.get(position).getStatus());

    }

    @Override
    public int getItemCount() {
        return mCollectionBookBeanList.size();
    }

}
