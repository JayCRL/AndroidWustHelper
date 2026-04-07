package com.example.wusthelper.mvp.presenter;

import android.annotation.SuppressLint;
import android.util.Log;

import com.airbnb.lottie.L;
import com.example.wusthelper.MyApplication;
import com.example.wusthelper.R;
import com.example.wusthelper.base.BasePresenter;
import com.example.wusthelper.bean.javabean.data.Test2;
import com.example.wusthelper.bean.javabean.data.CreditsData;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.helper.SnackBarHelper;
import com.example.wusthelper.mvp.model.CreditsStatisticsModel;
import com.example.wusthelper.mvp.view.CreditsStatisticsView;
import com.example.wusthelper.request.NewApiHelper;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;
import com.example.wusthelper.ui.activity.CreditsStatisticsActivity;
import com.example.wusthelper.utils.NetWorkUtils;
import com.example.wusthelper.utils.ToastUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class CreditsStatisticsPresenter extends BasePresenter<CreditsStatisticsView> {

    private static final String TAG = "CreditsStatisticsPresenter";

    private CreditsStatisticsModel model;

    //获取当前请求时间
    private Long requestTime;

    public CreditsStatisticsPresenter() {
        model = new CreditsStatisticsModel();
    }

    @Override
    public void initPresenterData() {

        getCreditStatistics();
    }

    @SuppressLint("LongLogTag")
    public void getCreditStatistics() {

        if (NetWorkUtils.isConnected(MyApplication.getContext())) {

            //进行网络请求
            requestTime = System.currentTimeMillis();
            // TODO 新接口返回的学分数据html显示混乱
//            model.getCreditFromNet(new DisposeDataListener() {
//                @SuppressLint("LongLogTag")
//                @Override
//                public void onSuccess(Object responseObj) {
////                    getView().cancelDialog();
//                    Log.e(TAG,"onSuccess");
//                    CreditsData data = (CreditsData) responseObj;
//                    Log.e(TAG,"data.getCode()" + data.getCode());
//                    if(data.getCode().equals("10000") || data.getCode().equals("11000")){
//                        Log.e(TAG,"onSuccess");
//                        String html = data.getData();
//                        Document doc = Jsoup.parse(html);
//                        if(doc.getElementsByAttributeValue("colspan","12").text().equals("未查询到数据")){
//                            Log.d(TAG,"未查询到数据");
//                            getCreditStatistics();
//                        }else {
//                            getView().showCreditsHtml(html);
//                        }
//                        Log.e(TAG,"html" +data.getData());
//
//                    }else{
//                        ToastUtil.show(data.getMsg());
//                    }
//                    if(!((requestTime-SharePreferenceLab.getSchemeRequestTime()) >= (30L *24*60*60*1000) || SharePreferenceLab.getSchemeHtml().equals(""))){
//                        getView().cancelDialog();
//                    }
//                }
//
//                @Override
//                public void onFailure(Object reasonObj) {
//                    ToastUtil.show("网络请求失败！");
//                    if(!((requestTime-SharePreferenceLab.getSchemeRequestTime()) >= (30L *24*60*60*1000) || SharePreferenceLab.getSchemeHtml().equals(""))){
//                        getView().cancelDialog();
//                    }
////                    getView().cancelDialog();
//                }
//            });


//            //进行网络请求
//            requestTime = System.currentTimeMillis();
            //为了缓解后台请求压力,隔一个月才能重新刷新数据(ms)
            if((requestTime-SharePreferenceLab.getSchemeRequestTime()) >= (30L *24*60*60*1000) || SharePreferenceLab.getSchemeHtml().equals("")){
                Log.d(TAG,"请求培养方案");
                model.getSchemeFromNet(new DisposeDataListener() {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void onSuccess(Object responseObj) {
                        getView().cancelDialog();
                        Log.e(TAG,"onSuccess");
                        CreditsData data = (CreditsData) responseObj;
                        if ("401".equals(data.getCode())) {
                            NewApiHelper.handleUnauthorized(getView().getContext(), data.getMsg());
                            return;
                        }

                        if(data.isSuccess()){
                            Log.e(TAG,"onSuccess");
                            String html = data.getData();
                            Log.e(TAG,"htmlScheme" +data.getData());
                            SharePreferenceLab.setSchemeHtml(html);
                            // 仅在获取成功后记录缓存时间，避免失败导致的长期缓存空状态
                            SharePreferenceLab.setSchemeRequestTime(requestTime);
                            getView().showCreditsHtml(html);
                        }else{
                            Log.d(TAG,data.getMsg());
                            ToastUtil.show(data.getMsg());
                        }
                    }

                    @Override
                    public void onFailure(Object reasonObj) {
                        ToastUtil.show("网络请求失败！");
                        getView().cancelDialog();
                    }
                });

            }else{
                getView().cancelDialog();
                getView().showCreditsHtml(SharePreferenceLab.getSchemeHtml());
            }

        } else {
            getView().cancelDialog();
            getView().showSnackBar("网络未连接,获取学分信息失败！");
        }

    }

}
