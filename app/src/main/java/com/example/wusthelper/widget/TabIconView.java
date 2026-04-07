package com.example.wusthelper.widget;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.wusthelper.R;

import java.util.List;

public class TabIconView extends HorizontalScrollView {

    private LinearLayout mLinearLayout;
    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private int thisWeekId;

    public TabIconView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public TabIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {

        this.setOverScrollMode(OVER_SCROLL_NEVER);
        this.setHorizontalScrollBarEnabled(true);
        mLayoutInflater = LayoutInflater.from(mContext);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mLinearLayout = new LinearLayout(mContext);
        mLinearLayout.setPadding(1, 0, 1, 0);
        mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        setHorizontalScrollBarEnabled(false);
        addView(mLinearLayout, params);

    }

    public void addTab(SpannableStringBuilder week, List<Integer> classTable, int id, boolean isThisWeek, OnClickListener onClickListener) {

        View view = mLayoutInflater.inflate(R.layout.item_coursepage_week, mLinearLayout, false);
        view.setId(id);
        LinearLayout linearLayout = (LinearLayout)view.findViewById(R.id.ll_tab_background);
        TextView weekTextView = (TextView)view.findViewById(R.id.tv_week);
        weekTextView.setText(week);
        TabView tabView = (TabView) view.findViewById(R.id.tabView);
        tabView.setAlpha(0.9f);
        tabView.setColorPoint(classTable);
        TextView thisWeekTextView = (TextView)view.findViewById(R.id.tv_this_week);
        if (!isThisWeek) {
            thisWeekTextView.setVisibility(INVISIBLE);
        } else {
            thisWeekTextView.setVisibility(VISIBLE);
            thisWeekTextView.setText("(本周)");
            linearLayout.setBackground(mContext.getDrawable(R.drawable.shape_item_selected));
            thisWeekId = id;
        }
        view.setOnClickListener(onClickListener);
        mLinearLayout.addView(view, mLinearLayout.getChildCount());

    }

    public void setThisWeekTab(int id) {

        View beforeView = mLinearLayout.findViewById(thisWeekId);
        LinearLayout beforeLinearLayout = beforeView.findViewById(R.id.ll_tab_background);
        beforeLinearLayout.setBackground(mContext.getDrawable(R.drawable.selector_item));
        View view = mLinearLayout.findViewById(id);
        LinearLayout linearLayout = view.findViewById(R.id.ll_tab_background);
        linearLayout.setBackground(mContext.getDrawable(R.drawable.shape_this_week));

    }

    public void removeAllView() {

        mLinearLayout.removeAllViews();

    }

}