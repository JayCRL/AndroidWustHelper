package com.example.wusthelper.mvp.presenter;

import com.example.wusthelper.MyApplication;
import com.example.wusthelper.base.BasePresenter;
import com.example.wusthelper.bean.javabean.GradeBean;
import com.example.wusthelper.bean.javabean.ScholarshipBean;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.mvp.model.GradeModel;
import com.example.wusthelper.mvp.view.ScholarShipChooseView;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class ScholarShipChoosePresenter extends BasePresenter<ScholarShipChooseView> {

    private GradeModel gradeModel;

    public boolean isChooseAll = true;

    public List<ScholarshipBean> scholarshipBeanList;

    public String semester;

    public boolean isChanged = false;

    private int semesterNow = 0;

    public int semesterChange;

    public static final String[] SEMESTER = new String[]{"大一","大二","大三","大四"};

    public ScholarShipChoosePresenter() {
        gradeModel = new GradeModel();
    }

    @Override
    public void initPresenterData() {
        initDate();
    }

    public void initDate() {

        semester  = SharePreferenceLab.getInstance().getScholarshipSemesterSelected(MyApplication.getContext());
        scholarshipBeanList = new ArrayList<>();
        List<GradeBean> beans;
        beans = LitePal.findAll(GradeBean.class);
    }


    public void loadScholarship()
    {
//        Log.e(TAG, "loadScholarship: semester  == = "+semester );
        List<GradeBean> gradeBeans = LitePal.findAll(GradeBean.class);
        if(gradeBeans.isEmpty()){
            getView().ShowColorSnackBar();
            semester = SEMESTER[semesterNow];
            getView().setScholarshipChooseButton(false);
        }else{
            if(LitePal.where("semester = ?",semester).find(ScholarshipBean.class).isEmpty()&&getScholarshipBeanFromGrade(semester,
                    gradeBeans).isEmpty()){
                getView().ShowColorSnackBar();
                semester = SEMESTER[semesterNow];
                getView().setScholarshipChooseButton(false);
            }else if(!LitePal.where("semester=?",semester).find(ScholarshipBean.class).isEmpty()){
                if(isChanged){
                    saveScholarship(SEMESTER[semesterNow]);
                    isChanged = false;
                }
                updateListFromScholarshipDb();
//                semesterText.setText(semester);
//                Log.e(TAG, "loadScholarship:数据库中有内容   size =="+scholarshipBeanList.size());
                semesterNow = semesterChange;
                SharePreferenceLab.getInstance().setScholarshipSemesterSelected(MyApplication.getContext(),semester);
                getView().onGradeListShow();
                getView().setScholarshipChooseButton(true);
            }else if(!getScholarshipBeanFromGrade(semester, gradeBeans).isEmpty()){
                if(isChanged){
                    saveScholarship(SEMESTER[semesterNow]);
                    isChanged  = false;
                }
                updateListFromGradeDb();
                saveScholarship(semester);
//                semesterText.setText(semester);
                semesterNow = semesterChange;
                SharePreferenceLab.getInstance().setScholarshipSemesterSelected(MyApplication.getContext(),semester);
                getView().onGradeListShow();
                getView().setScholarshipChooseButton(true);
            }
        }
    }


    public void isChanged(){
        if(isChanged){
            saveScholarship(semester);
        }
    }

    public void chooseChange() {
        isChanged = true;
        isChooseAll = !isChooseAll;
        for (ScholarshipBean scholarshipBean : scholarshipBeanList) {
            scholarshipBean.setChecked(isChooseAll);
        }
        getView().onGradeListShow();
        getView().setScholarshipChooseAllImage(isChooseAll);
    }

    private List<ScholarshipBean> getScholarshipBeanFromGrade(String semester,List<GradeBean> gradeBeans)
    {
        List<ScholarshipBean> scholarshipBeans = new ArrayList<>();
        for(GradeBean gradeBean :gradeBeans) {
            if(getShowTermStr(gradeBean.getSchoolTerm()).equals(semester)){
//                Log.e(TAG, "getScholarshipBeanFromGrade: semester  ======"+semester);
                ScholarshipBean scholarshipBean = new ScholarshipBean();
                scholarshipBean.setGpa(gradeBean.getGradePoint());
                scholarshipBean.setCourseName(gradeBean.getCourseName());
//                Log.e(TAG, "getScholarshipBeanFromGrade: name ====="+scholarshipBean.getCourseName());
                scholarshipBean.setChecked(true);
                scholarshipBean.setSemester(semester);
                scholarshipBean.setWeight(1.0);
                scholarshipBean.setCredit(gradeBean.getCourseCredit());
                scholarshipBeans.add(scholarshipBean);
            }
        }
        return scholarshipBeans;
    }

    private String getShowTermStr(String d) {
        String nj = null;
        String[] xn = d.split("-");
        int schoolYear = Integer.parseInt(SharePreferenceLab.getInstance().getStudentId(MyApplication.getContext()).substring(0, 4));
        int k = Integer.parseInt(xn[0]) - schoolYear;
        switch (k) {
            case 0:
                nj = SEMESTER[0];
                break;
            case 1:
                nj = SEMESTER[1];
                break;
            case 2:
                nj = SEMESTER[2];
                break;
            case 3:
                nj = SEMESTER[3];
                break;
            default:
                break;
        }
        return nj;
    }

    private void saveScholarship(String sem)
    {
        if(scholarshipBeanList==null){
            return;
        }
//        Log.e(TAG, "saveScholarship: 保存" );
        LitePal.deleteAll(ScholarshipBean.class,"semester = ?",sem);
//        Log.e(TAG, "saveScholarship: 变化后的个数"+scholarshipBeanList.size() );
        for(ScholarshipBean scholarshipBean :scholarshipBeanList){
            scholarshipBean.assignBaseObjId(0);
            scholarshipBean.save();
//            Log.e(TAG, "saveScholarship: 保存" );
        }
    }


    private void updateListFromScholarshipDb()
    {
        scholarshipBeanList.clear();
        List<ScholarshipBean> beans = LitePal.where("semester = ?",semester).find(ScholarshipBean.class);
        scholarshipBeanList.addAll(beans);
    }

    private void updateListFromGradeDb()
    {
        scholarshipBeanList.clear();
        List<ScholarshipBean> beans = getScholarshipBeanFromGrade(semester,LitePal.findAll(GradeBean.class));
        scholarshipBeanList.addAll(beans);
    }

}
