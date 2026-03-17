package com.example.wusthelper.adapter;

import android.util.SparseArray;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.example.wusthelper.MyApplication;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Gong Yunhao
 * @version: V1.0
 * @date: 2018/9/25
 * @github https://github.com/Roman-Gong
 * @blog https://www.jianshu.com/u/52a8fa1f29fb
 */
public class ViewPagerAdapter extends SmartFragmentStatePagerAdapter {
    public List<Fragment> list;
    private FragmentManager fragmentManager;

    public void setList(List<Fragment> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
        this.fragmentManager = fm;
    }


    @Override
    public Fragment getItem(int position) {
        return list.get(position);
    }

    @Override
    public int getCount() {
        return list != null ? list.size() : 0;
    }

}