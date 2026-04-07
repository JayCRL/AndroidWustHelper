package com.example.wusthelper.appwidget.service;

import android.content.Intent;
import android.widget.RemoteViewsService;

import com.example.wusthelper.appwidget.factory.CountDownWidgetFactory;
import com.example.wusthelper.bean.javabean.CountDownBean;
import com.example.wusthelper.utils.CountDownUtils;

import org.litepal.LitePal;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

// 在本文件中有一个半违规操作 我们在导入CountDown的数组时将其Id进行更改为其在list中的位置
// 以便后期进行查询
// 如果有需要更改的地方
// 可以在更改的地方进行修正
public class CountDownWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        List<CountDownBean> countDowns = LitePal.where("isDelete == 0").find(CountDownBean.class);

        //更改处
        for (int i=0;i<countDowns.size();i++){
            countDowns.get(i).setId(i);
        }
        Iterator<CountDownBean> iterable =  countDowns.iterator();
        while(iterable.hasNext()){
            CountDownBean countDown = iterable.next();
            if(!CountDownUtils.checkState(countDown.getTargetTime())){
                iterable.remove();
            }
        }
        Collections.sort(countDowns, new Comparator<CountDownBean>() {
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
        return new CountDownWidgetFactory(getApplicationContext(),countDowns);
    }
}