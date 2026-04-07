package com.example.wusthelper.mvp.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.wusthelper.MyApplication;
import com.example.wusthelper.R;
import com.example.wusthelper.base.BasePresenter;
import com.example.wusthelper.bean.javabean.CycleImageBean;
import com.example.wusthelper.bean.javabean.CourseBean;
import com.example.wusthelper.bean.javabean.data.BaseData;
import com.example.wusthelper.bean.javabean.data.CourseData;
import com.example.wusthelper.bean.javabean.data.GatewayCarouselData;
import com.example.wusthelper.bean.javabean.data.GatewayNoticeData;
import com.example.wusthelper.dbhelper.CourseDB;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.mvp.model.HomePageModel;
import com.example.wusthelper.mvp.view.HomeFragmentView;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;
import com.example.wusthelper.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

public class HomePagePresenter extends BasePresenter<HomeFragmentView> {

    private final HomePageModel model;

    public HomePagePresenter(){
        model = new HomePageModel();
    }
    @Override
    public void initPresenterData() {
    }

    public void getCycleImageData(Context context) {
        model.getCycleImageDataFormNet(new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                GatewayCarouselData data = (GatewayCarouselData) responseObj;
                // Debug: 轮播接口是否返回了正确的 url
                try {
                    android.util.Log.e("HomePagePresenter", "carousel code=" + data.getCode() + ", msg=" + data.getMsg());
                    android.util.Log.e("HomePagePresenter", "carousel urls=" + data.getData());
                } catch (Exception ignore) {
                }

                List<CycleImageBean> list = convertCarouselUrls(data.getData());
                // 轮播图接口在部分环境下 code 解析可能异常，但只要拿到了 urls 就优先展示网络轮播
                if (list != null && !list.isEmpty()) {
                    getView().showCycleImageFromNet(list);
                } else {
                    getView().showCycleImageFromLocal(getLocalCycleImage(context));
                }
            }

            @Override
            public void onFailure(Object reasonObj) {
                getView().showCycleImageFromLocal(getLocalCycleImage(context));
            }
        });
        model.getAndroidNotice(new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                GatewayNoticeData data = (GatewayNoticeData) responseObj;
                if (data.isSuccess() && data.getData() != null && !data.getData().isEmpty()) {
                    getView().showHomeNotice(data.getData().get(0).getTitle());
                } else {
                    getView().hideHomeNotice();
                }
            }

            @Override
            public void onFailure(Object reasonObj) {
                getView().hideHomeNotice();
            }
        });
    }

    private List<CycleImageBean> convertCarouselUrls(List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return new ArrayList<>();
        }
        List<CycleImageBean> list = new ArrayList<>();
        for (String url : urls) {
            if (url == null || url.trim().isEmpty()) {
                continue;
            }
            CycleImageBean bean = new CycleImageBean();
            bean.setImgUrl(url.trim());
            bean.setTitle("");
            bean.setContent("");
            list.add(bean);
        }
        return list;
    }

    private List<Bitmap> getLocalCycleImage(Context context) {
        List<Bitmap> list = new ArrayList<>();
        Bitmap bitmap01 = BitmapFactory.decodeResource(context.getResources(), R.drawable.banner1);
        Bitmap bitmap02 = BitmapFactory.decodeResource(context.getResources(), R.drawable.banner2);
        list.add(bitmap01);
        list.add(bitmap02);
        return list;
    }

    public void LoginPhysical(String password) {
        getView().showLoadDialog();
        model.postLoginPhysical(password, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                BaseData data = (BaseData) responseObj;
                if(data.isSuccess()){
                    getPhysicalCourse();
                }else{
                    ToastUtil.showShortToastCenter(data.getMsg());
                }
                getView().cancelLoadDialog();
            }

            @Override
            public void onFailure(Object reasonObj) {
                ToastUtil.show("登录失败，可能是网络状态不佳或者物理实验官网崩溃");
                getView().cancelLoadDialog();
            }
        });
    }

    public void getPhysicalCourse(){
        model.getPhysicalCourse(new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                getView().cancelLoadDialog();
                CourseData data = (CourseData) responseObj;
                if(data.isSuccess()){
                    CourseDB.addAllCourseData(data.getData(),
                            SharePreferenceLab.getStudentId(),SharePreferenceLab.getSemester(),
                            CourseBean.TYPE_PHYSICAL);

                    SharePreferenceLab.getInstance().setIsPhysicalLogin(MyApplication.getContext(),
                            true);
                    getView().startPhysicalDetailActivity();
                }else {
                    ToastUtil.showShortToastCenter(data.getMsg());
                }

            }

            @Override
            public void onFailure(Object reasonObj) {
                ToastUtil.show("登录成功,但是物理实验课表请求失败");
                getView().cancelLoadDialog();
            }
        });
    }
}
