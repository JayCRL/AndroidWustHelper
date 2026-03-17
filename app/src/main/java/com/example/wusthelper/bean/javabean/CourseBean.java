package com.example.wusthelper.bean.javabean;

import com.google.gson.annotations.SerializedName;

import org.litepal.crud.LitePalSupport;

/**
 * CourseBean的子类
 * 同时也是关联了LitePal数据库的类
 * */
public class CourseBean extends LitePalSupport {

    public static final int END = 1;
    //默认为 非本地数据（即教务处接口网络数据）
    public static final int IS_DEFAULT = 0;
    //自己添加的数据
    public static final int IS_MYSELF = 1;

    //TYPE为普通，也就是通过接口请求到的正常课程数据,也包括本地添加的数据，也是普通类型
    public static final int TYPE_COMMON = 0;
    //TYPE为物理课，也就是请求到的物理课表内容
    public static final int TYPE_PHYSICAL = 1;
    //TYPE为情侣课表，也就是请求到的情侣课表内容
    public static final int TYPE_QR = 2;
    //TYPE为蹭课，也就是请求到的蹭课内容
    public static final int TYPE_SEARCH = 3;

    //是否是本地录入数据(比如通过请求课表接口获取到 教务处的课 就不是本地数据)
    //每次请求课表会把 非本地数据删除 本地数据就得到保留
    private int isDefault = IS_DEFAULT;

    //课的类型，默认为普通课
    private int classType = TYPE_COMMON;

    //LitePal数据库的主键，不可进行修改操作，实际作用也只是区分课程不同
    private long id;
    //学生的学号
    private String studentId;
    //课程的名称
    @SerializedName(value = "className", alternate = {"name", "courseName"})
    private String courseName;

    //教学班
    @SerializedName("teachClass")
    private String classNo;

    //课程的所在教室
    @SerializedName("classroom")
    private String classRoom;
    //教室名字
    @SerializedName("teacher")
    private String teacherName;

    //课程开始时间
    @SerializedName(value = "startWeek", alternate = {"start_week", "fromWeek", "from_week"})
    private int startWeek;
    //课程结束时间
    @SerializedName(value = "endWeek", alternate = {"end_week", "toWeek", "to_week"})
    private int endWeek;
    //课程在星期几上课
    @SerializedName(value = "weekDay", alternate = {"weekday", "week_day", "dayOfWeek", "day_of_week"})
    private int weekday;
    //课程上课时间的部分，如果startTime = 1 也就是代表是第一节课
    @SerializedName(value = "section", alternate = {"startSection", "start_section", "startTime", "start_time"})
    private int startTime;

    // 课程结束节次（用于新版 basic 后端：startSection/endSection）。
    // 老接口可能没有该字段，默认 0。
    @SerializedName(value = "endSection", alternate = {"end_section", "sectionEnd", "endTime", "end_time"})
    private int endTime;

    //课程所在的学期
    private String semester;

    //课程颜色，根据一个函数生成
    private int color;

    //课程是否处在上课时间，这个数据不是预设存在数据库里面的，而是按照开始周，和结束周，以及当前周 通过计算得出
    private boolean isInClass;

    public int getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(int isDefault) {
        this.isDefault = isDefault;
    }

    public int getClassType() {
        return classType;
    }

    public void setClassType(int classType) {
        this.classType = classType;
    }

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getClassNo() {
        return classNo;
    }

    public void setClassNo(String classNo) {
        this.classNo = classNo;
    }

    public String getClassRoom() {
        return classRoom;
    }

    public void setClassRoom(String classRoom) {
        this.classRoom = classRoom;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public int getStartWeek() {
        return startWeek;
    }

    public void setStartWeek(int startWeek) {
        this.startWeek = startWeek;
    }

    public int getEndWeek() {
        return endWeek;
    }

    public void setEndWeek(int endWeek) {
        this.endWeek = endWeek;
    }

    public int getWeekday() {
        return weekday;
    }

    public void setWeekday(int weekday) {
        this.weekday = weekday;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public boolean isInClass() {
        return isInClass;
    }

    public void setInClass(boolean inClass) {
        isInClass = inClass;
    }

    /**慎用这个空的构造函数，可能会出现未知的问题*/
    public CourseBean() {
    }

    public CourseBean(int isDefault, int classType, String studentId, String courseName, String classNo, String classRoom, String teacherName, int startWeek, int endWeek, int weekday, int startTime, String semester, int color) {
        this.isDefault = isDefault;
        this.classType = classType;
        this.studentId = studentId;
        this.courseName = courseName;
        this.classNo = classNo;
        this.classRoom = classRoom;
        this.teacherName = teacherName;
        this.startWeek = startWeek;
        this.endWeek = endWeek;
        this.weekday = weekday;
        this.startTime = startTime;
        this.semester = semester;
        this.color = color;
    }

    // 兼容旧代码：历史上部分代码用 getClassName() 作为课程名。
    // 注意：LitePal/反射相关框架可能会使用 getClassName() 语义做内部判断，
    // 这里保留方法，但改为 deprecated 并显式转调到 getCourseName()，避免被误用。
    @Deprecated
    public String getCourseClassName() {
        return getCourseName();
    }

    public String getCourseTitle() {
        return getCourseName();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CourseBean)) return false;
        CourseBean courseBean = (CourseBean) obj;
        return this.getCourseName() != null
                && this.getCourseName().equals(courseBean.courseName)
                && this.getStartTime() == courseBean.getStartTime()
                && this.classNo != null
                && this.classNo.equals(courseBean.classNo);
    }

    public static CourseBean makeNewCourse(CourseBean courseBean){
        CourseBean newCourse = new CourseBean();
        newCourse.setIsDefault(courseBean.getIsDefault());
        newCourse.setCourseName(courseBean.getCourseName());
        newCourse.setClassNo(courseBean.getClassNo());
        newCourse.setClassRoom(courseBean.getClassRoom());
        newCourse.setWeekday(courseBean.getWeekday());
        newCourse.setStudentId(courseBean.getStudentId());
        newCourse.setStartWeek(courseBean.getStartWeek());
        newCourse.setEndWeek(courseBean.getEndWeek());
        newCourse.setStartTime(courseBean.getStartTime());
        newCourse.setColor(courseBean.getColor());
        newCourse.setSemester(courseBean.getSemester());
        newCourse.setTeacherName(courseBean.getTeacherName());
        newCourse.setClassType(courseBean.getClassType());
        return newCourse;
    }


    @Override
    public String toString() {
        return "CourseBean{" +
                ", isDefault=" + isDefault +
                ", id='" + id + '\'' +
                ", studentId=" + studentId +
                ", courseName='" + courseName + '\'' +
                ", classRoom='" + classRoom + '\'' +
                ", teacherName='" + teacherName + '\'' +
                ", classNo='" + classNo + '\'' +
                ", startWeek=" + startWeek +
                ", endWeek=" + endWeek +
                ", weekday=" + weekday +
                ", color=" + color +
                ", startTime=" + startTime +
                ", isInClass=" + isInClass +
                ", semester='" + semester + '\'' +
                ", classType=" + classType +
                '}';
    }
}
