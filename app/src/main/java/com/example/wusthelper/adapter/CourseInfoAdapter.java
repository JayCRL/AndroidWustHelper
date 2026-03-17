package com.example.wusthelper.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wusthelper.R;
import com.example.wusthelper.bean.javabean.CourseBean;
import com.example.wusthelper.bean.javabean.CourseNameBean;
import com.example.wusthelper.bean.javabean.SearchCourseBean;
import com.example.wusthelper.helper.MyDialogHelper;
import com.example.wusthelper.helper.SharePreferenceLab;
import com.example.wusthelper.utils.ToastUtil;

import java.util.List;
import java.util.Random;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class CourseInfoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<SearchCourseBean> searchCourseBeans;
    private Context mContext;
    private OnItemClickListener mOnItemClickListener;
    private SweetAlertDialog dialog;
    private String[] weekdays = {"","星期一","星期二","星期三","星期四","星期五","星期六","星期日"};

    public CourseInfoAdapter(Context context, List<SearchCourseBean> searchCourseBeans) {
        this.searchCourseBeans = searchCourseBeans;
        mContext = context;
    }

    public interface OnItemClickListener {

        void onItemClick(View view, int position);

    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {

        mOnItemClickListener = onItemClickListener;

    }

    public class CourseInfoViewHolder extends RecyclerView.ViewHolder {

        private TextView courseNameTV;
        private TextView teacherTV;
        private TextView classroomTV;
        private TextView classCampusTV;
        private TextView weekTV;
        private TextView weekdayTV;
        private TextView sectionTV;
        private Button addButton;
        private View view;

        public CourseInfoViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            courseNameTV = itemView.findViewById(R.id.courseName);
            teacherTV = itemView.findViewById(R.id.teacher);
            classroomTV = itemView.findViewById(R.id.classroom);
            classCampusTV = itemView.findViewById(R.id.classCampus);
            weekTV = itemView.findViewById(R.id.week);
            weekdayTV = itemView.findViewById(R.id.weekday);
            sectionTV = itemView.findViewById(R.id.section);
            addButton = itemView.findViewById(R.id.add_course);
        }

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_course_detail, parent, false);
        return new CourseInfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        CourseInfoViewHolder courseListViewHolder = (CourseInfoViewHolder)holder;
        courseListViewHolder.courseNameTV.setText(searchCourseBeans.get(position).getCourseName());
        courseListViewHolder.classCampusTV.setText(searchCourseBeans.get(position).getCampusName());
        courseListViewHolder.classroomTV.setText(searchCourseBeans.get(position).getClassroom());
        courseListViewHolder.sectionTV.setText("第"+searchCourseBeans.get(position).getStartSection()+"节~第"+searchCourseBeans.get(position).getEndSection()+"节");
        courseListViewHolder.weekTV.setText("第"+searchCourseBeans.get(position).getStartWeek()+"周~第"+searchCourseBeans.get(position).getEndWeek()+"周");
        courseListViewHolder.weekdayTV.setText(weekdays[Integer.parseInt((searchCourseBeans.get(position).getWeekDay()))]);
        courseListViewHolder.teacherTV.setText(searchCourseBeans.get(position).getTeacherName());
        courseListViewHolder.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog = MyDialogHelper.getCommonDialog(mContext, SweetAlertDialog.NORMAL_TYPE, "确认添加课程吗？", null, "确认","取消", new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        dialog.changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
                        Random random = new Random();
                        int color = random.nextInt(10);
                        for (int j = Integer.parseInt(searchCourseBeans.get(position).getStartSection()); j < Integer.parseInt(searchCourseBeans.get(position).getEndSection()); j += 2){
                            CourseBean courseBean = new CourseBean(1,CourseBean.TYPE_SEARCH,
                                    SharePreferenceLab.getStudentId(),searchCourseBeans.get(position).getCourseName()," ",
                                    searchCourseBeans.get(position).getClassroom()+" "+searchCourseBeans.get(position).getCampusName(),searchCourseBeans.get(position).getTeacherName(),
                                    Integer.parseInt(searchCourseBeans.get(position).getStartWeek()),
                                    Integer.parseInt(searchCourseBeans.get(position).getEndWeek()),
                                    Integer.parseInt(searchCourseBeans.get(position).getWeekDay()),(j+1)/2,SharePreferenceLab.getSemester(),color);

                            courseBean.save();
                        }
                        dialog.cancel();
                        SweetAlertDialog dialogSuccess = MyDialogHelper.getCommonDialog(mContext,SweetAlertDialog.SUCCESS_TYPE, "添加成功 \n",
                                null,
                                "确定", null);
                        dialogSuccess.show();
                    }
                });
                dialog.show();
//                ToastUtil.show("添加课程");
            }
        });

    }

    @Override
    public int getItemCount() {
        return searchCourseBeans.size();
    }

}
