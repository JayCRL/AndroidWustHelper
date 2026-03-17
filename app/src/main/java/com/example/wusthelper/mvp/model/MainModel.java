package com.example.wusthelper.mvp.model;

import com.example.wusthelper.MyApplication;
import com.example.wusthelper.bean.javabean.CourseBean;
import com.example.wusthelper.dbhelper.CourseDB;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.bean.javabean.data.ConfigData;
import com.example.wusthelper.bean.javabean.NoticeBean;
import com.example.wusthelper.request.NewApiHelper;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;
import com.example.wusthelper.helper.ConfigHelper;

import org.litepal.LitePal;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainModel {

    private static final String TAG = "MainModel";

    public void getConfig(DisposeDataListener listener) {
        NewApiHelper.getConfig(listener);
    }

    public void saveConfig(ConfigData configData) {
        //接下来储存学期信息
        SharePreferenceLab.setSemester(configData.getData().getCurrentTerm());
        //设置配置信息
        ConfigHelper.setConfigBean(configData);
    }

    public void getNotice(DisposeDataListener listener) {
        // 已切到新网关公告接口：返回 GatewayNoticeData
        NewApiHelper.getNotice(listener);
    }

    public void saveGatewayNoticeData(List<NoticeBean> beanList) {
        saveNoticeData(beanList);
    }

    public void getLostUnread(DisposeDataListener listener) {
        NewApiHelper.getLostUnread(listener);
    }

    public void saveNoticeData(List<NoticeBean> beanList) {

        //从数据库拿出的公告数据
        List<NoticeBean> mDBList = LitePal.findAll(NoticeBean.class);

        //拿到数据以后就清空数据库，后续再进行添加
        LitePal.deleteAll(NoticeBean.class);

        for(NoticeBean noticeBeanForSava : beanList){
            //遍历数据库的课程数据，对比网络获取的数据是否已经存储在数据库，如果储存在数据库（NewsId相同），则储存数据库的对应的NoticeBean的确认情况
            //但是如果对比下发现，网络获取的NoticeBean的时间改变了，证明NoticeBean有更新，要储存网络数据，不设置确认情况
            //如果数据库没有该公告，则直接储存网络数据
            for(NoticeBean noticeBean2 : mDBList){
                if(noticeBeanForSava.getNewsId()==noticeBean2.getNewsId()){

                    if(isNewTimeNoticeBean(noticeBeanForSava.getUpdateTime(),noticeBean2.getUpdateTime())){
                        //该公告修改过时间，证明发生了更改，证明要重新缓存，也就是对网络数据不做修改（因为网络数据就是新的）
                    }else {
                        noticeBeanForSava.setIf_confirm(noticeBean2.getIf_confirm());
                    }
                }
            }
            noticeBeanForSava.save();
        }
    }

    private boolean isNewTimeNoticeBean(String createTime, String createTime1) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//创建日期转换对象HH:mm:ss为时分秒，年月日为yyyy-MM-dd
        try {
            Date dt1 = df.parse(createTime);//将字符串转换为date类型
            Date dt2 = df.parse(createTime1);
            if(dt1.getTime()>dt2.getTime())//比较时间大小,dt1小于dt2
            {
                return true;
            }
            else
            {
                return false;
            }
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    public List<NoticeBean> getNoticeBeanForShow() {
        //从数据库拿出的公告数据
        List<NoticeBean> mDBList = LitePal.findAll(NoticeBean.class);

        List<NoticeBean> NoticeListForShow = new ArrayList<>();

        for(NoticeBean noticeBean1 : mDBList){
            //如果没有确认就添加到用于显示的NoticeBean
            if(!noticeBean1.getIf_confirm()){
                NoticeListForShow.add(noticeBean1);
            }
        }
        return NoticeListForShow;
    }

    private boolean compare_the_notice_date(String createTime, String createTime1) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//创建日期转换对象HH:mm:ss为时分秒，年月日为yyyy-MM-dd
        try {
            Date dt1 = df.parse(createTime);//将字符串转换为date类型
            Date dt2 = df.parse(createTime1);
            if(dt1.getTime()>dt2.getTime())//比较时间大小,dt1小于dt2
            {
                return true;
            }
            else
            {
                return false;
            }
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    public List<CourseBean> getCoursesByIDFormDB(long courseID) {
        return CourseDB.getCoursesByID(courseID);
    }
}
