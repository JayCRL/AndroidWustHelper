package com.example.wusthelper.mvp.presenter;

import android.util.Log;

import com.example.wusthelper.base.BasePresenter;
import com.example.wusthelper.bean.javabean.GradeBean;
import com.example.wusthelper.bean.javabean.GraduateGradeBean;
import com.example.wusthelper.bean.javabean.TermKV;
import com.example.wusthelper.bean.javabean.data.GradeData;
import com.example.wusthelper.bean.javabean.data.GraduateGradeData;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.mvp.model.GradeModel;
import com.example.wusthelper.mvp.view.GradeView;
import com.example.wusthelper.request.okhttp.exception.OkHttpException;
import com.example.wusthelper.request.okhttp.listener.DisposeDataListener;
import com.example.wusthelper.utils.ToastUtil;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class GradePresenter extends BasePresenter<GradeView> {

    private static final String TAG = "GradePresenter";

    private GradeModel gradeModel;

    final String[] items = {"全部成绩", "大四下学期", "大四上学期", "大三下学期", "大三上学期"
            , "大二下学期", "大二上学期", "大一下学期", "大一上学期"};

    private List<TermKV> mTermKVList = new ArrayList<>();

    //获取当前请求时间
    private Long requestTime;



    public GradePresenter() {
        gradeModel = new GradeModel();
    }

    @Override
    public void initPresenterData() {
        initGradeItemForShow();
    }

    private void initGradeItemForShow() {
        getView().showLoading("正在拼命加载中");
        //进行网络请求
        requestTime = System.currentTimeMillis();
        //为了缓解后台请求压力,隔20分钟才能重新刷新数据(ms)
        if((requestTime-SharePreferenceLab.getRequestTime()) >= 1200000){
            SharePreferenceLab.setRequestTime(requestTime);
            getGrade();
            //初始化 数据列表，但是数据为零，证明数据库没有课程数据，于是向后台进行网络请求
        }else{
            getView().showSuccessColorSnackBar("暂无最新成绩！");
            getView().cancelDialog();
            if(SharePreferenceLab.getIsGraduate()){
                getView().onGraduateListShow(getGraduateGradeList());
            }else{
                getView().onGradeListShow(getGradeList());
            }

        }

    }

    private void getGrade() {
        if(SharePreferenceLab.getIsGraduate()) {
            gradeModel.getGraduateGradeFromNet(new DisposeDataListener() {
                @Override
                public void onSuccess(Object responseObj) {
                    Log.e(TAG, "onSuccess: "+responseObj.toString() );
                    GraduateGradeData graduateGradeData = (GraduateGradeData) responseObj;
                    if(graduateGradeData.getCode().equals("10000")){
                        //如果接口获取的数据大小不为零（我们认为这就是代表获取到了最新数据），就用接口数据覆盖数据库
                        // 大小为零，则后台可能出现了问题，就不保存接口数据，从数据库取数据
                        if(graduateGradeData.getData().size()>0){
                            //如果接口获取的数据和本地数据库不一致，则成绩有了改变，获取到最新的成绩
                            if(gradeModel.isGraduateGradeUpdate(graduateGradeData)){
                                getView().showSuccessColorSnackBar("有新的成绩，已经同步！");
                            }else{
                                getView().showSuccessColorSnackBar("暂无最新成绩！");
                            }
                            gradeModel.saveGraduateGradeListToDB(graduateGradeData);

                        }else{
                            getView().showFailureColorSnackBar("获取的成绩列表为空,可能因为没有成绩,可能是教务处崩溃");
                        }
                        getView().cancelDialog();
                        getView().onGraduateListShow(getGraduateGradeList());
                    }else{
                        getView().cancelDialog();
                        getView().showFailureColorSnackBar("Token过期或服务异常");
                    }
                }

                @Override
                public void onFailure(Object reasonObj) {
                    //接口请求失败，则从数据库取出数据
                    OkHttpException e = (OkHttpException) reasonObj;
                    String msg = e.getEmsg().toString();
                    Log.e(TAG, "onFailure: "+msg);
                    getView().showFailureColorSnackBar("网络请求失败,显示本地数据");
                    getView().cancelDialog();
                    getView().onGraduateListShow(getGraduateGradeList());

                }
            });
        } else {
            gradeModel.getGradeFromNet(new DisposeDataListener() {
                @Override
                public void onSuccess(Object responseObj) {
                    Log.e(TAG, "onSuccess: "+responseObj.toString() );
                    GradeData gradeData = (GradeData) responseObj;
                    if(gradeData.isSuccess()){
                        //如果接口获取的数据大小不为零（我们认为这就是代表获取到了最新数据），就用接口数据覆盖数据库
                        // 大小为零，则后台可能出现了问题，就不保存接口数据，从数据库取数据
                        if(gradeData.getData() != null && gradeData.getData().size()>0){
                            //如果接口获取的数据和本地数据库不一致，则成绩有了改变，获取到最新的成绩
                            if(gradeModel.isGradeUpdate(gradeData)){
                                getView().showSuccessColorSnackBar("有新的成绩，已经同步！");
                            }else{
                                getView().showSuccessColorSnackBar("暂无最新成绩！");
                            }
                            gradeModel.saveGradeListToDB(gradeData);

                        }else{
                            getView().showFailureColorSnackBar("获取的成绩列表为空,可能因为没有成绩");
                        }

                        getView().cancelDialog();
                        getView().onGradeListShow(getGradeList());
                    }else{
                        getView().cancelDialog();
                        getView().showFailureColorSnackBar(gradeData.getMsg());
                    }
                }

                @Override
                public void onFailure(Object reasonObj) {
                    //接口请求失败，则从数据库取出数据
                    OkHttpException e = (OkHttpException) reasonObj;
                    String msg = e.getEmsg().toString();
                    Log.e(TAG, "onFailure: "+msg);
                    getView().showFailureColorSnackBar("网络请求失败,显示本地数据");
                    getView().cancelDialog();
                    getView().onGradeListShow(getGradeList());

                }
            });
        }

    }

    public List<GradeBean> getGradeList() {
        return gradeModel.getGradeItemForShowFromDB();
    }

    public List<GraduateGradeBean> getGraduateGradeList() {
        return gradeModel.getGraduateGradeItemForShowFromDB();
    }

    public void initTermList() {
        List<TermKV> termList = new ArrayList<>();
        List<GradeBean> gradeBeanList = LitePal.order("schoolTerm desc,courseName desc").find(GradeBean.class);

        if (gradeBeanList.size() == 0) {
            return;
        }
        List<String> tempList = new ArrayList<>();
        // 去重算法
        tempList.add(gradeBeanList.get(0).getSchoolTerm());
        for (int i = 0; i < gradeBeanList.size(); ++i) {
            if (!tempList.get(tempList.size() - 1).equals(gradeBeanList.get(i).getSchoolTerm())) {
                tempList.add(gradeBeanList.get(i).getSchoolTerm());
            }
        }

        for(int i=0;i<tempList.size();i++){
            termList.add(new TermKV(tempList.get(i), getShowTermStr(tempList.get(i),tempList.size()-i-1)));
        }
//        for (String a : tempList) {
//            termList.add(new TermKV(a, getShowTermStr(a)));
//        }
        termList.add(0, new TermKV("", "全部成绩"));
        mTermKVList.clear();
        mTermKVList.addAll(termList);
        Log.d(TAG, "initTermList: "+mTermKVList);
    }

    public void changeTerm(int which) {
        getView().onGradeListShow(
                gradeModel.queryGradeWithTermFromDB(mTermKVList.get(which).getTrueTerm()));
    }

    public String[] getTermItems(){
        List<String> list = new ArrayList();
        for (TermKV termKV : mTermKVList){
            list.add(termKV.getShowTerm()+termKV.getTrueTerm());

        }
        return list.toArray(new String[0]);
    }

    public List<TermKV> getTermList(){
        return mTermKVList;
    }


    private String getShowTermStr(String d,int position) {
        String xq = "", nj = "";
        String[] xn = d.split("-");
        if (xn[xn.length - 1].equals("1")) {
            xq = "上学期";
        } else if (xn[xn.length - 1].equals("2")) {
            xq = "下学期";
        }

        switch (position) {
            case 0:
            case 1:
                nj = "大一";
                break;
            case 2:
            case 3:
                nj = "大二";
                break;
            case 4:
            case 5:
                nj = "大三";
                break;
            case 6:
            case 7:
                nj = "大四";
                break;
            case 8:
            case 9:
                nj = "大五";
                break;
            case 10:
            case 11:
                nj = "大六";
                break;
            default:
                nj = "大六";
                break;
        }
//        int schoolYear = Integer.parseInt(SharePreferenceLab.getStudentId().substring(0, 4));
//        int k = Integer.parseInt(xn[0]) - schoolYear;
//        switch (k) {
//            case 0:
//                nj = "大一";
//                break;
//            case 1:
//                nj = "大二";
//                break;
//            case 2:
//                nj = "大三";
//                break;
//            case 3:
//                nj = "大四";
//                break;
//            default:
//                break;
//        }
        return nj + xq;
    }

    public void queryGradeWithText(String name) {
        if(SharePreferenceLab.getIsGraduate()) {
            getView().onGraduateListShow(gradeModel.queryGraduateGradeWithText(name));
        }else {
            getView().onGradeListShow(gradeModel.queryGradeWithText(name));
        }

    }
}
