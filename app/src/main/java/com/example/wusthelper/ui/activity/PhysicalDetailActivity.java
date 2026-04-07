package com.example.wusthelper.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wusthelper.MyApplication;
import com.example.wusthelper.R;
import com.example.wusthelper.adapter.PhysicalAdapter;
import com.example.wusthelper.base.activity.BaseActivity;
import com.example.wusthelper.bean.javabean.CourseBean;
import com.example.wusthelper.databinding.ActivityPhysicaldetialBinding;
import com.example.wusthelper.dbhelper.CourseDB;
import com.example.wusthelper.helper.SharePreferenceLab;

import java.util.ArrayList;
import java.util.List;

public class PhysicalDetailActivity extends BaseActivity<ActivityPhysicaldetialBinding>
        implements View.OnClickListener{

    private static final String TAG = "PhysicalDetailActivity";
    private List<CourseBean> courseBeans= new ArrayList<>();
    private TextView physicalDetailID;
    private TextView physicalDetailNumber;
    private RelativeLayout ll_physical_login_out;
    private RecyclerView recyclerView;
    private PhysicalAdapter physicalAdapter;

    private Switch switch_status;


    public static Intent newInstance(Context context) {
        Intent intent = new Intent(context, PhysicalDetailActivity.class);
        return intent;

    }

    @Override
    public void initView() {
//        switch_status           = findViewById(R.id.physical_switch_status);
        recyclerView            = findViewById(R.id.physicalClassRecyclerView);
        physicalDetailID        = findViewById(R.id.physicalDetailID);
        physicalDetailNumber    = findViewById(R.id.physicalDetailNumber);
        ll_physical_login_out   = findViewById(R.id.ll_physical_login_out);

//        switch_status.setOnCheckedChangeListener(this);
//        switch_status.setChecked(SharePreferenceLab.getInstance().getIsPhysicalShow(MyApplication.getContext()));

        physicalDetailID.setText(SharePreferenceLab.getInstance().getStudentId(MyApplication.getContext()));
        initCourseData();
        initRecyclerView();
        setOnClickLister();
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this){
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        recyclerView.setLayoutManager(layoutManager);
        physicalAdapter = new PhysicalAdapter(courseBeans,this);
        recyclerView.setAdapter(physicalAdapter);
    }

    private void initCourseData() {

        String studentId = SharePreferenceLab.getStudentId();
        String selectSemester = SharePreferenceLab.getSelectSemester();

        courseBeans=CourseDB.getPhysicalCourse(studentId,selectSemester,CourseBean.TYPE_PHYSICAL);
        Log.d(TAG, "initCourseData: "+courseBeans.size());
        physicalDetailNumber.setText(courseBeans.size()+"");
    }

    private void setOnClickLister() {
        ll_physical_login_out.setOnClickListener(this);
        getBinding().ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ll_physical_login_out:
                String studentId = SharePreferenceLab.getStudentId();
                String semester = SharePreferenceLab.getSemester();
                CourseDB.removePhysicalData(studentId,CourseBean.TYPE_PHYSICAL,semester);
                SharePreferenceLab.getInstance().setIsPhysicalLogin(PhysicalDetailActivity.this,false);
                finish();
                break;
            default:
                break;
        }
    }

//    @Override
//    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
//        switch (compoundButton.getId()){
//            case R.id.physical_switch_status:
//                if(compoundButton.isChecked()) {
//                    SharePreferenceLab.getInstance().setIsPhysicalShow(MyApplication.getContext(),true);
//                }
//                else {
//                    SharePreferenceLab.getInstance().setIsPhysicalShow(MyApplication.getContext(),false);
//                }
//                break;
//        }
//    }
}