package com.example.wusthelper.mvp.model;

import com.example.wusthelper.bean.javabean.CountDownBean;
import com.example.wusthelper.request.NewApiHelper;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;

import org.litepal.LitePal;

import java.util.List;

public class CountdownModel {

    public void getShareCountdown(String onlyId, DisposeDataListener listener){
        NewApiHelper.getShareCountdown(onlyId,listener);
    }
    public void getCountDownFormNet(DisposeDataListener listener){
        NewApiHelper.getCountDownFormNet(listener);
    }

    public List<CountDownBean> getCountDownForDB() {
        return LitePal.where(" isDelete==0 ").find(CountDownBean.class);
    }

    public void upload2Net(CountDownBean countDownBean, DisposeDataListener listener){
        NewApiHelper.uploadCountDownFromNet(listener,countDownBean);
    }

    public void changeCountDown(CountDownBean countDownBean , DisposeDataListener listener){
        NewApiHelper.changeCountDown(countDownBean,listener);
    }

    public void deleteCountDownFromNet(String onlyId,DisposeDataListener listener){
        NewApiHelper.deleteCountDownFromNet(listener,onlyId);
    }

    public void deleteCountdownFromDB(String onlyId){
        LitePal.deleteAll(CountDownBean.class,"onlyId == ?",onlyId);
    }

    public List<CountDownBean> getCountdownChangeFromDB(){
        return LitePal.where("isChange==1").find(CountDownBean.class);
    }
    public List<CountDownBean> getCountdownAddFromDB(){
        return LitePal.where("isOnNet==0").find(CountDownBean.class);
    }
    public List<CountDownBean> getCountdownDeleteFromDB(){
        return LitePal.where("isDelete==1").find(CountDownBean.class);
    }
}
