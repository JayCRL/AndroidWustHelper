package com.example.wusthelper.appwidget.factory;

//REFESH 函数
// 在本文件中有一个半违规操作 我们在导入CountDown的数组时将其Id进行更改为其在list中的位置
// 以便后期进行查询
// 如果有需要更改的地方
// 可以在更改的地方进行修正

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.wusthelper.R;
import com.example.wusthelper.bean.javabean.CountDownBean;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.ui.activity.CountdownActivity;
import com.example.wusthelper.utils.CountDownUtils;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class CountDownWidgetFactory implements RemoteViewsService.RemoteViewsFactory {
    private static final String TAG = "CountDownWidgetFactory";
    private static List<CountDownBean> mylist=new ArrayList<>() ;
    private static Context mContext;

    public CountDownWidgetFactory(Context mContext,List<CountDownBean> mylist) {
        Log.d(TAG, "CountDownWidgetFactory: ");
        this.mylist = mylist;
        this.mContext = mContext;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {

    }

    @Override
    public void onDestroy() {
        mylist.clear();
    }

    @Override
    public int getCount() {
        return mylist.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        Log.d(TAG, "getViewAt: ");
        CountDownBean countDown = mylist.get(position);

        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.item_countdown_widget);

        remoteViews.setTextViewText(R.id.tv_countdown_className, countDown.getName());
        remoteViews.setTextViewText(R.id.tv_countdown_widget_date, CountDownUtils.getShowTime(countDown.getTargetTime()));
        remoteViews.setTextViewText(R.id.tv_countdown_widget_remainedtime, CountDownUtils.getRemainDays(countDown.getTargetTime()) + "");
        remoteViews.setTextViewText(R.id.tv_countdown_widget_starttime, CountDownUtils.getStartDays(countDown.getCreateTime()) + "");
        setTextColor(remoteViews);
        Intent intent = CountdownActivity.newInstance(mContext);
        intent.putExtra("index", countDown.getId());
        intent.putExtra("kind", CountdownActivity.WIDGET);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
//        remoteViews.setOnClickFillInIntent(R.id.lv_countdown_widget, intent);
//        remoteViews.setOnClickFillInIntent(R.id.tv_countdown_widget_remainedtime, intent);
//        remoteViews.setOnClickFillInIntent(R.id.tv_countdown_widget_starttime, intent);
//        remoteViews.setOnClickFillInIntent(R.id.rl_countdown_all, intent);
        return remoteViews;

    }

    private void setTextColor(RemoteViews remoteViews) {

        String textColor= SharePreferenceLab.getInstance().getWidgetCountdownTextColor(mContext);
        if(!textColor.equals("")){
            int Color=java.lang.Integer.parseInt(textColor);
            remoteViews.setTextColor(R.id.tv_countdown_className,Color);
            remoteViews.setTextColor(R.id.tv_countdown_widget_date,Color);
            remoteViews.setTextColor(R.id.tv_countdown_widget_remainedtime,Color);
            remoteViews.setTextColor(R.id.tv_countdown_widget_starttime,Color);
        }else {
            remoteViews.setTextColor(R.id.tv_countdown_className, Color.BLACK);
            remoteViews.setTextColor(R.id.tv_countdown_widget_date,Color.BLACK);
            remoteViews.setTextColor(R.id.tv_countdown_widget_remainedtime,Color.BLACK);
            remoteViews.setTextColor(R.id.tv_countdown_widget_starttime,Color.BLACK);
        }
    }

    public static void ReFresh(){
        mylist = LitePal.where("isDelete == 0").find(CountDownBean.class);
        Iterator<CountDownBean> iterable =  mylist.iterator();

//        更改处
        for (int i=0;i<mylist.size();i++){
            mylist.get(i).setId(i);
        }
        while(iterable.hasNext()){
            CountDownBean countDown = iterable.next();
            if(!CountDownUtils.checkState(countDown.getTargetTime())){
                iterable.remove();
            }
        }

        Collections.sort(mylist, new Comparator<CountDownBean>() {
            @Override
            public int compare(CountDownBean o1, CountDownBean o2) {
                if (o1.getTargetTime() > o2.getTargetTime()) {
                    return 1;
                } else if (o1.getTargetTime() < o2.getTargetTime()) {
                    return -1;
                }
                return 0;
            }
        });
    }
    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return mylist.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }


}