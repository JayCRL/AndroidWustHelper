package com.example.wusthelper.mvp.presenter;

import android.util.Log;
import android.view.View;

import androidx.fragment.app.FragmentManager;

import com.example.wusthelper.MyApplication;
import com.example.wusthelper.base.BasePresenter;
import com.example.wusthelper.bean.javabean.CourseBean;
import com.example.wusthelper.bean.javabean.data.ConfigData;
import com.example.wusthelper.bean.javabean.NoticeBean;
import com.example.wusthelper.bean.javabean.data.LostData;
import com.example.wusthelper.bean.javabean.data.NoticeData;
import com.example.wusthelper.helper.ConfigHelper;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.mvp.model.MainModel;
import com.example.wusthelper.mvp.view.MainView;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;
import com.example.wusthelper.ui.dialog.UpdateDialog;

import java.util.List;

public class MainPresenter extends BasePresenter<MainView> {

    private static final String TAG = "MainPresenter";
    
    private MainModel mainModel;

    public MainPresenter() {
        mainModel = new MainModel();
    }

    @Override
    public void initPresenterData() {
    }
    /**
     * 通过网络请求 向管理端请求配置信息
     * */
    public void getConfig() {
        mainModel.getConfig(new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                ConfigData configData = (ConfigData) responseObj;
                Log.e(TAG, "onSuccess: configData="+configData);
                if(configData.getCode().equals("0")&& configData.getMsg().equals("成功")){
                    mainModel.saveConfig(configData);
                    tryUpdate();
                }
            }

            @Override
            public void onFailure(Object reasonObj) {
                Log.d(TAG, "onFailure: "+ reasonObj.toString());
            }
        });
    }

    private void tryUpdate() {
        //如果有更新就弹窗更新公告
        if(ConfigHelper.getIfHasNewVersion()){
            getView().showUpdateDialog(ConfigHelper.getConfigBean().getData());
        }
    }

    public void getNotice() {
        mainModel.getNotice(new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                // 已切到新网关公告接口
                com.example.wusthelper.bean.javabean.data.GatewayNoticeData noticeData =
                        (com.example.wusthelper.bean.javabean.data.GatewayNoticeData) responseObj;
                Log.e(TAG, "onSuccess: " + noticeData.toString());
                if (noticeData.isSuccess()) {
                    mainModel.saveGatewayNoticeData(noticeData.getData());
                    showNotice();
                }
            }

            @Override
            public void onFailure(Object reasonObj) {
                Log.e(TAG, "getNotice onFailure: " + reasonObj);
            }
        });
    }

    private void showNotice() {
        List<NoticeBean> list = mainModel.getNoticeBeanForShow();
        if(list.size()>0)
        getView().showNoticeDialog(list);
    }

    public void getLost() {
        mainModel.getLostUnread(new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                Log.e(TAG, "onSuccess: "+ responseObj.toString());
                LostData lostData = (LostData) responseObj;
                Log.e(TAG, "onSuccess: "+lostData.toString() );
                if(lostData.getCode().equals("20000")){
                    if(lostData.getData()!=null){
                        getView().showLostDialog(lostData.getData());
                    }
                }
            }

            @Override
            public void onFailure(Object reasonObj) {

            }
        });
    }

    public List<CourseBean> getCoursesByID(long courseID) {

        return mainModel.getCoursesByIDFormDB(courseID);
    }

    public int getCurrentItem() {
        //根据不同底部导航栏情况，返回默认为课表index
        if (!SharePreferenceLab.getHomepageSettings()) {
            return 0;
        } else {
            if (SharePreferenceLab.getBarState() == SharePreferenceLab.BARSTATEDEFAULT
                    || SharePreferenceLab.getBarState() == SharePreferenceLab.BARSTATEDELETEALL
                    || SharePreferenceLab.getBarState() == SharePreferenceLab.BARSTATEDELETECON) {
                if (SharePreferenceLab.getIsGraduate()) {
                    return 1;
                } else {
                    return 2;
                }
            } else {
                return 1;
            }
        }

    }
}
