package com.example.wusthelper.ui.dialog;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.List;

public class BasePagerAdapter extends PagerAdapter {
    private List<View> mListView;
    @Override
    public int getCount() {
        return mListView.size();
    }

    public BasePagerAdapter(List<View> list){
        this.mListView = list;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ((ViewPager) container).addView(mListView.get(position));
        return mListView.get(position);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        ((ViewPager) container).removeView(mListView.get(position));
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}
