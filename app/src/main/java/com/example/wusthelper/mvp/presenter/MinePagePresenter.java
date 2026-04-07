package com.example.wusthelper.mvp.presenter;

import com.example.wusthelper.base.BasePresenter;
import com.example.wusthelper.bean.javabean.CountDownBean;
import com.example.wusthelper.bean.javabean.CourseBean;
import com.example.wusthelper.bean.javabean.ScholarshipBean;
import com.example.wusthelper.dbhelper.CourseDB;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.mvp.view.MineFragmentView;

import org.litepal.LitePal;

public class MinePagePresenter extends BasePresenter<MineFragmentView> {

    public MinePagePresenter() {
    }

    @Override
    public void initPresenterData() {
        initStudentIdData();
        initUserNameData();
        initHeadPath();
    }

    private void initStudentIdData() {
        getView().showStudentId(getStudentId());
    }

    private void initUserNameData() {
        getView().showName(getUserName());
    }

    private void initHeadPath() {
        getView().showUserImage(getHeadPath());
    }

    public String getStudentId() {
        return SharePreferenceLab.getStudentId();
    }

    public String getUserName() {
        return SharePreferenceLab.getDisplayName();
    }

    public String getHeadPath() {
        return SharePreferenceLab.getHeadPath();
    }

    /**
     * 退出登录，清除部分数据
     * */
    public void Logout() {
        SharePreferenceLab.setIsLogin(false);
        SharePreferenceLab.logout();
        CourseDB.logout();
        LitePal.deleteAll(CourseBean.class);
        LitePal.deleteAll(CountDownBean.class);
        LitePal.deleteAll(ScholarshipBean.class);

        SharePreferenceLab.setIsLibraryLogin(false);
        SharePreferenceLab.setRequestTime(0L);
    }
}
